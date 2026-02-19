package ui;

import db.GuestDAO;
import logic.Booking;
import logic.BookingStatus;
import db.BookingDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
    private JButton checkInButton;
    private JButton checkOutButton;

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
        setupCheckButtons();
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

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use yyyy-mm-dd.");
        }
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Booking b : bookings) {
            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getGuest().getFullName(),
                    b.getRoom().getRoomNumber(),
                    b.getCheckInDate().format(DATE_FORMAT),
                    b.getCheckOutDate().format(DATE_FORMAT),
                    b.getNumberOfGuests(),
                    b.getTotalPrice(),
                    b.getStatus().getDbValue(),
                    b.isPaid() ? "Yes" : "No"
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
    
    
    private void setupCheckButtons() {
        checkInButton = new JButton("Check In");
        checkOutButton = new JButton("Check Out");
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);

        rightPanel.add(checkInButton);
        rightPanel.add(checkOutButton);

        checkInButton.addActionListener(e -> performCheckIn());
        checkOutButton.addActionListener(e -> performCheckOut());

        // Enable/disable based on selection
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonState();
            }
        });
    }

    private void updateButtonState() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || bookings.isEmpty()) {
            checkInButton.setEnabled(false);
            checkOutButton.setEnabled(false);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Booking booking = bookings.get(modelRow);
        checkInButton.setEnabled(booking.getStatus() == BookingStatus.CONFIRMED);
        checkOutButton.setEnabled(booking.getStatus() == BookingStatus.CHECKED_IN);
    }

    private void performCheckIn() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Booking booking = bookings.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Check in guest " + booking.getGuest().getFullName() + "?",
                "Confirm Check In", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingDAO.checkIn(booking.getBookingId());
                return null;
            }
            @Override
            protected void done() {
                try {
                    get(); // check for exceptions
                    refreshFromDatabase();
                    JOptionPane.showMessageDialog(BookingsPanel.this, "Check-in successful.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookingsPanel.this,
                            "Check-in failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void performCheckOut() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Booking booking = bookings.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Check out guest " + booking.getGuest().getFullName() + "?",
                "Confirm Check Out", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingDAO.checkOut(booking.getBookingId());
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    refreshFromDatabase();
                    JOptionPane.showMessageDialog(BookingsPanel.this, "Check-out successful.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookingsPanel.this,
                            "Check-out failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    @Override
    protected void initComponents() {
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Guest", "Room", "Check-in", "Check-out", "Guests", "Total", "Status", "Paid"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;   // ID
                    case 1: return String.class;    // Guest name
                    case 2: return Integer.class;   // Room number
                    case 3: return String.class;    // Check-in (formatted date)
                    case 4: return String.class;    // Check-out
                    case 5: return Integer.class;   // Number of guests
                    case 6: return Double.class;    // Total price
                    case 7: return String.class;    // Status
                    case 8: return String.class;    // Paid
                    default: return Object.class;
                }
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Double) {
                    setText(String.format("%.2f", (Double) value));
                } else {
                    super.setValue(value);
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    if (modelRow >= 0 && modelRow < bookings.size()) {
                        Booking b = bookings.get(modelRow);
                        showDetails(formatBookingDetails(b));
                    }
                }
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