package ui;

import logic.Guest;
import logic.Room;
import logic.RoomStatus;
import logic.RoomType;
import db.RoomDAO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class RoomsPanel extends HotelDataPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Room> rooms;
    private RoomDAO roomDAO;

    public RoomsPanel(List<Room> rooms, RoomDAO roomDAO) {
        super("Rooms");
        this.rooms = rooms;
        this.roomDAO = roomDAO;
        initComponents();
        populateTable();
        setupFilter();
        refreshButton.addActionListener(e -> refreshFromDatabase());
        setupSearchMenu();
    }

    private void setupSearchMenu() {
        JPopupMenu searchMenu = new JPopupMenu();

        JMenuItem typeItem = new JMenuItem("By Type");
        JMenuItem priceItem = new JMenuItem("By Price Range");
        JMenuItem availableItem = new JMenuItem("Available Rooms");

        typeItem.addActionListener(e -> searchByType());
        priceItem.addActionListener(e -> searchByPriceRange());
        availableItem.addActionListener(e -> searchByAvailable());

        searchMenu.add(typeItem);
        searchMenu.add(priceItem);
        searchMenu.add(availableItem);

        setSearchMenu(searchMenu);
    }

    private void searchByType() {
        RoomType[] types = RoomType.values();
        RoomType selected = (RoomType) JOptionPane.showInputDialog(this,
                "Select room type:", "Search by Type",
                JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (selected == null) return;

        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() {
                return roomDAO.findRoomsByType(selected);
            }
            @Override
            protected void done() {
                try {
                    rooms = get();
                    populateTable();
                    if (rooms.isEmpty()) {
                        JOptionPane.showMessageDialog(RoomsPanel.this, "No rooms of type " + selected + " found.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RoomsPanel.this, "Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchByPriceRange() {
        // Creating input fields
        JTextField minField = new JTextField(10);
        JTextField maxField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Minimum price:"));
        panel.add(minField);
        panel.add(new JLabel("Maximum price:"));
        panel.add(maxField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Enter Price Range", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double min = Double.parseDouble(minField.getText().trim());
                double max = Double.parseDouble(maxField.getText().trim());

                if (min < 0 || max < 0 || min > max) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid range. Please ensure min ≤ max and both are non‑negative.");
                    return;
                }

                SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
                    @Override
                    protected List<Room> doInBackground() {
                        return roomDAO.findRoomsByPriceRange(min, max);
                    }
                    @Override
                    protected void done() {
                        try {
                            rooms = get();
                            populateTable();
                            if (rooms.isEmpty()) {
                                JOptionPane.showMessageDialog(RoomsPanel.this,
                                        "No rooms found in that price range.");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(RoomsPanel.this,
                                    "Search error: " + ex.getMessage());
                        }
                    }
                };
                worker.execute();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers.");
            }
        }
    }


    private void searchByAvailable() {
        JTextField checkInField = new JTextField(10);
        JTextField checkOutField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Check-in (yyyy-mm-dd):"));
        panel.add(checkInField);
        panel.add(new JLabel("Check-out (yyyy-mm-dd):"));
        panel.add(checkOutField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Dates", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                LocalDate checkIn = LocalDate.parse(checkInField.getText().trim());
                LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim());

                if (!checkOut.isAfter(checkIn)) {
                    JOptionPane.showMessageDialog(this,
                            "Check-out must be after check-in.");
                    return;
                }

                SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
                    @Override
                    protected List<Room> doInBackground() {
                        return roomDAO.findAvailableRooms(checkIn, checkOut);
                    }
                    @Override
                    protected void done() {
                        try {
                            rooms = get();
                            populateTable();
                            if (rooms.isEmpty()) {
                                JOptionPane.showMessageDialog(RoomsPanel.this,
                                        "No rooms available for those dates.");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(RoomsPanel.this,
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
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomNumber(),
                    r.getRoomType(),
                    r.getRoomPricePerNight(), // Format price
                    r.getMaxOccupancy(),
                    r.hasBalcony() ? "Yes" : "No",                   // Boolean to readable
                    String.join(", ", r.getAmenities()),             // List to string
                    r.isAvailable() ? "Available" : "Booked",        // Boolean to status
                    r.getStatus().name()
            });
        }
    }

    private void refreshFromDatabase() {
        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() throws Exception {
                return roomDAO.getAllRooms();
            }

            @Override
            protected void done() {
                try {
                    rooms = get();
                    populateTable();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RoomsPanel.this,
                            "Error refreshing:" + e.getMessage());
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
                    // Case‑insensitive filter on columns 0 (number), 1 (type), 5 (amenities), 7 (status)
                    RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + text, 0, 1, 5, 7);
                    sorter.setRowFilter(rf);
                }
            }
        });
    }

    @Override
    protected void initComponents() {
        tableModel = new DefaultTableModel(new String[]{
                "Number", "Type", "Price", "Max",
                "Balcony", "Amenities", "Availability", "Status"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;   // ID
                    case 1: return String.class;  // type
                    case 2: return Double.class;   // price
                    case 3: return Integer.class;   // max
                    case 4: return String.class;    // balcony
                    case 5: return String.class;   // amenities
                    case 6: return String.class;    // available
                    case 7: return String.class;    // Status
                    default: return Object.class;
                }
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // enable sorting

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
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
                    if (modelRow >= 0 && modelRow < rooms.size()) {
                        Room r = rooms.get(modelRow);
                        showDetails(formatRoomDetails(r));
                    }
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    if (row >= 0) {
                        Room r = rooms.get(row);
                        JOptionPane.showMessageDialog(RoomsPanel.this,
                                "Room " + r.getRoomNumber() + "\nType: " + r.getRoomType() +
                                        "\nPrice: €" + r.getRoomPricePerNight() +
                                        "\nAvailable: " + (r.isAvailable() ? "Yes" : "No"),
                                "Room Details",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        JSplitPane split = createMainWithDetails(new JScrollPane(table));
        add(split, BorderLayout.CENTER);
    }

    private String formatRoomDetails(Room r) {
        return String.format("RoomNumber: %d\nType: %s\nPrice per night: €%.2f\nMax Occupancy:%d\n" +
                "Balcony: %s\nAmenities: %s\nAvailable: %s\nStatus: %s",
                r.getRoomNumber(), r.getRoomType(), r.getRoomPricePerNight(),
                r.getMaxOccupancy(), r.hasBalcony() ? "Yes" : "No",
                String.join(", ", r.getAmenities()),
                r.isAvailable() ? "Yes" : "No", r.getStatus()
        );
    }
}
