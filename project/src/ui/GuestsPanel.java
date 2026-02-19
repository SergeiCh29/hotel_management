package ui;

import logic.Guest;
import db.GuestDAO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class GuestsPanel extends HotelDataPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Guest> guests;
    private GuestDAO guestDAO;

    public GuestsPanel(List<Guest> guests, GuestDAO dao) {
        super("Guests");
        this.guests = guests;
        this.guestDAO = dao;
        initComponents();
        populateTable();
        setupFilter();
        refreshButton.addActionListener(e -> refreshFromDatabase());
        setupSearchMenu();
    }

    private void setupSearchMenu() {
        JPopupMenu searchMenu = new JPopupMenu();

        JMenuItem nameItem = new JMenuItem("By Name");
        JMenuItem emailItem = new JMenuItem("By Email");
        JMenuItem vipItem = new JMenuItem("By VIP Status");

        nameItem.addActionListener(e -> searchByName());
        emailItem.addActionListener(e -> searchByEmail());
        vipItem.addActionListener(e -> searchVIP());

        searchMenu.add(nameItem);
        searchMenu.add(emailItem);
        searchMenu.add(vipItem);

        setSearchMenu(searchMenu);  // attach to the base search button
    }

    private void searchByName() {
        String name = JOptionPane.showInputDialog(this, "Enter name to search:", "Search by Name", JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        SwingWorker<List<Guest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Guest> doInBackground() {
                return guestDAO.searchByName(name.trim());
            }
            @Override
            protected void done() {
                try {
                    guests = get();
                    populateTable();
                    if (guests.isEmpty()) {
                        JOptionPane.showMessageDialog(GuestsPanel.this, "No guests found with that name.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GuestsPanel.this, "Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchByEmail() {
        String email = JOptionPane.showInputDialog(this, "Enter email address:", "Search by Email", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.trim().isEmpty()) return;

        SwingWorker<Guest, Void> worker = new SwingWorker<>() {
            @Override
            protected Guest doInBackground() {
                return guestDAO.findByEmail(email.trim());
            }
            @Override
            protected void done() {
                try {
                    Guest g = get();
                    if (g != null) {
                        guests = List.of(g);
                        populateTable();
                    } else {
                        JOptionPane.showMessageDialog(GuestsPanel.this, "No guest found with that email.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GuestsPanel.this, "Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchVIP() {
        SwingWorker<List<Guest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Guest> doInBackground() {
                return guestDAO.findVIPGuests();
            }
            @Override
            protected void done() {
                try {
                    guests = get();
                    populateTable();
                    if (guests.isEmpty()) {
                        JOptionPane.showMessageDialog(GuestsPanel.this, "No VIP guests found.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GuestsPanel.this, "Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Guest g : guests) {
            tableModel.addRow(new Object[]{
                    g.getId(),
                    g.getFullName(),
                    g.getEmail(),
                    g.getPhone(),
                    g.getNationality(),
                    g.getLoyaltyPoints(),
                    g.isVIP() ? "VIP" : ""
            });
        }
    }

    private void refreshFromDatabase() {
        // SwingWorker to avoid blocking EDT
        SwingWorker<List<Guest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Guest> doInBackground() throws Exception {
                return guestDAO.getAllGuests();
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
                    // Case‑insensitive filter on columns 1 (full name), 2 (email), 4 (Nationality), 6 (VIP status)
                    RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + text, 1, 2, 4, 6);
                    sorter.setRowFilter(rf);
                }
            }
        });
    }

    @Override
    protected void initComponents() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Nationality", "Points","VIP"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;   // ID
                    case 1: return String.class;    // name
                    case 2: return String.class;    // email
                    case 3: return String.class;    // phone
                    case 4: return String.class;    // nationality
                    case 5: return Integer.class;   // points
                    case 6: return String.class;    // vip
                    default: return Object.class;
                }
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // enable sorting

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    if (modelRow >= 0 && modelRow < guests.size()) {
                        Guest g = guests.get(modelRow);
                        showDetails(formatGuestDetails(g));
                    }
                }
            }
        });

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
        return String.format("ID: %d\nName: %s %s\nEmail: %s\nPhone: %s\nNationality: %s\nLoyalty: %d\nVIP: %s\nBookings: %d",
                g.getId(), g.getFirstName(), g.getLastName(),
                g.getEmail(), g.getPhone(), g.getNationality(),
                g.getLoyaltyPoints(), g.isVIP() ? "Yes" : "No",
                g.getBookingHistory().size());
    }
}
