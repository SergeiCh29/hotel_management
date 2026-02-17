package ui;

import db.GuestDAO;
import logic.Booking;
import logic.BookingStatus;
import db.BookingDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingsPanel extends HotelDataPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Booking> bookings;
    private BookingDAO bookingDAO;
    private GuestDAO guestDAO;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingsPanel(List<Booking> bookings, BookingDAO bookingDAO, GuestDAO guestDAO) {
        super("Bookings");
        this.bookings = bookings;
        this.bookingDAO = bookingDAO;
        this.guestDAO = new GuestDAO();
        initComponents();
        populateTable();
        setupFilter();
        refreshButton.addActionListener(e -> refreshFromDatabase());
        setupSearchMenu();
    }

    private void setupSearchMenu() {
        JPopupMenu searchMenu = new JPopupMenu();

        JMenuItem guestItem = new JMenuItem("By Guest ID");
        JMenuItem statusItem = new JMenuItem("By Status");
        JMenuItem datesItem = new JMenuItem("By Date Range");

        guestItem.addActionListener(e -> searchByGuest());
        statusItem.addActionListener(e -> searchByStatus());
        datesItem.addActionListener(e -> searchByDates());

        searchMenu.add(guestItem);
        searchMenu.add(statusItem);
        searchMenu.add(datesItem);

        setSearchMenu(searchMenu);
    }

    private void searchByGuest() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Guest ID:", "Search by Guest ID", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int guestId = Integer.parseInt(input.trim());
            SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Booking> doInBackground() throws Exception {
                    return bookingDAO.findByGuest(guestId);
                }
                @Override
                protected void done() {
                    try {
                        bookings = get();
                        populateTable();
                        if (bookings.isEmpty()) {
                            JOptionPane.showMessageDialog(BookingsPanel.this,
                                    "No bookings found for guest ID " + guestId);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BookingsPanel.this,
                                "Search error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid guest ID. Please enter a number.");
        }
    }

    private void searchByDates() {
        JTextField startField = new JTextField(10);
        JTextField endField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Start date (yyyy-mm-dd):"));
        panel.add(startField);
        panel.add(new JLabel("End date (yyyy-mm-dd):"));
        panel.add(endField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Search by dates range", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            LocalDate start = LocalDate.parse(startField.getText().trim());
            LocalDate end = LocalDate.parse(endField.getText().trim());

            if (!end.isAfter(start)) {
                JOptionPane.showMessageDialog(this, "Start date must be before the end date");
                return;
            }

            SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Booking> doInBackground() {
                    return bookingDAO.findByDateRange(start, end);
                }

                @Override
                protected void done() {
                    try {
                        bookings = get();
                        populateTable();
                        if (bookings.isEmpty()) {
                            JOptionPane.showMessageDialog(BookingsPanel.this,
                                    "No bookings present between " + start + " and " + end);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BookingsPanel.this,
                                "Search error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();

        } catch (HeadlessException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use yyyy-mm-dd.");
        }
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Booking b : bookings) {
            tableModel.addRow(new Object[]{
                    b.getBookingId(), b.getGuest().getFullName(), b.getRoom().getRoomNumber(),
                    b.getCheckInDate().format(DATE_FORMAT), b.getCheckOutDate().format(DATE_FORMAT),
                    b.getNumberOfGuests(), String.format("%.2f", b.getTotalPrice()),
                    b.getStatus().getDbValue(), b.isPaid() ? "Yes" : "No"
            });
        }
    }

    private void searchByStatus() {
        BookingStatus[] statuses = BookingStatus.values();
        BookingStatus selected = (BookingStatus) JOptionPane.showInputDialog(this,
                "Select status:", "Search by Status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
        if (selected == null) return;

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingDAO.findByStatus(selected);
            }
            @Override
            protected void done() {
                try {
                    bookings = get();
                    populateTable();
                    if (bookings.isEmpty()) {
                        JOptionPane.showMessageDialog(BookingsPanel.this,
                                "No bookings with status " + selected.getDbValue());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookingsPanel.this,
                            "Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshFromDatabase() {
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingDAO.getAllBookings();
            }

            @Override
            protected void done() {
                try {
                    bookings = get();
                    populateTable();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookingsPanel.this,
                            "Error refreshing: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setupFilter() {
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // Search in columns: 0 (ID), 1 (guest name), 2 (room), 7 (status), 8 (paid)
                    RowFilter<DefaultTableModel, Object> rf =
                            RowFilter.regexFilter("(?i)" + text, 0, 1, 2, 7, 8);
                    sorter.setRowFilter(rf);
                }
            }
        });
    }

    @Override
    protected void initComponents() {
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Guest", "Room", "Check-in", "Check-out", "Guests", "Total", "Status", "Paid"
        }, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            if (row >= 0 && row < bookings.size()) {
                Booking b = bookings.get(row);
                showDetails(formatBookingDetails(b));
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    if (row >= 0) {
                        Booking b = bookings.get(row);
                        JOptionPane.showMessageDialog(BookingsPanel.this,
                                "Booking ID: " + b.getBookingId() + "\nGuest: " + b.getGuest().getFullName() +
                                        "\nRoom: " + b.getRoom().getRoomNumber() + "\nDates: " + b.getCheckInDate() + " to " + b.getCheckOutDate() +
                                        "\nTotal: €" + String.format("%.2f", b.getTotalPrice()) + "\nStatus: " + b.getStatus().getDbValue() +
                                        "\nPaid: " + (b.isPaid() ? "Yes" : "No"),
                                "Booking Details",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        JSplitPane split = createMainWithDetails(new JScrollPane(table));
        add(split, BorderLayout.CENTER);
    }

    private String formatBookingDetails(Booking b) {
        return String.format(
                "Booking ID: %d\nGuest: %s (ID: %d)\nRoom: %d (%s)\nCheck-in: %s\n" +
                "Check-out: %s\nNights: %d\nGuests: %d\nTotal Price: €%.2f\n" +
                "Status: %s\nPaid: %s\nPayment Method: %s",
                b.getBookingId(), b.getGuest().getFullName(), b.getGuest().getId(),
                b.getRoom().getRoomNumber(), b.getRoom().getRoomType(),
                b.getCheckInDate().format(DATE_FORMAT), b.getCheckOutDate().format(DATE_FORMAT),
                b.getNumberOfNights(), b.getNumberOfGuests(), b.getTotalPrice(),
                b.getStatus().getDbValue(), b.isPaid() ? "Yes" : "No",
                b.getPaymentMethod().isEmpty() ? "Not specified" : b.getPaymentMethod()
        );
    }
}