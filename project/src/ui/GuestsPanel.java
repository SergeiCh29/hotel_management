package ui;

import logic.Guest;
import db.GuestDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class GuestsPanel extends HotelDataPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Guest> guests;
    private GuestDAO guestDAO; // or DatabaseConnector-like class

    public GuestsPanel(List<Guest> guests, GuestDAO dao) {
        super("Guests");
        this.guests = guests;
        this.guestDAO = dao;
        initComponents();
        populateTable();
        setupFilter(); // optional
        refreshButton.addActionListener(e -> refreshFromDatabase());
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Guest g : guests) {
            tableModel.addRow(new Object[]{
                    g.getId(),
                    g.getFullName(),
                    g.getEmail(),
                    g.getLoyaltyPoints(),
                    g.isVIP() ? "VIP" : ""
            });
        }
    }

    private void refreshFromDatabase() {
        // Use SwingWorker to avoid blocking EDT
        SwingWorker<List<Guest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Guest> doInBackground() throws Exception {
                return guestDAO.getAllGuests(); // your DAO method
            }
            @Override
            protected void done() {
                try {
                    guests = get();
                    populateTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GuestsPanel.this,
                            "Error refreshing: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setupFilter() {
        // Add a document listener to searchField that applies a RowFilter to the table
        // This is an enhancement beyond teacher's example
    }

    @Override
    protected void initComponents() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Points", "VIP"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // enable sorting

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            if (row >= 0 && row < guests.size()) {
                Guest g = guests.get(row);
                showDetails(formatGuestDetails(g));
            }
        });

        // Double-click to show full details
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    if (row >= 0) {
                        Guest g = guests.get(row);
                        JOptionPane.showMessageDialog(GuestsPanel.this,
                                "Guest: " + g.getFullName() + "\nLoyalty: " + g.getLoyaltyPoints(),
                                "Guest Details",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        JSplitPane split = createMainWithDetails(new JScrollPane(table));
        add(split, BorderLayout.CENTER);
    }

    private String formatGuestDetails(Guest g) {
        return String.format("ID: %d\nName: %s %s\nEmail: %s\nPhone: %s\nLoyalty: %d\nVIP: %s\nBookings: %d",
                g.getId(), g.getFirstName(), g.getLastName(),
                g.getEmail(), g.getPhone(), g.getLoyaltyPoints(),
                g.isVIP() ? "Yes" : "No",
                g.getBookingHistory().size());
    }
}
