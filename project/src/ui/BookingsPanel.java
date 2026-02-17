package ui;

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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingsPanel(List<Booking> bookings, BookingDAO bookingDAO) {
        super("Bookings");
        this.bookings = bookings;
        this.bookingDAO = bookingDAO;
        initComponents();
        populateTable();
        setupFilter();
        refreshButton.addActionListener(e -> refreshFromDatabase());
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