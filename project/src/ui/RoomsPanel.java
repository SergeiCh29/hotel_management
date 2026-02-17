package ui;

import logic.Room;
import db.RoomDAO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
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
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomNumber(),
                    r.getRoomType(),
                    String.format("%.2f", r.getRoomPricePerNight()), // Format price
                    r.getMaxOccupancy(),
                    r.hasBalcony() ? "Yes" : "No",                   // Boolean to readable
                    String.join(", ", r.getAmenities()),             // List to string
                    r.isAvailable() ? "Available" : "Booked",        // Boolean to status
                    r.getStatus()
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
                "Balcony", "Amenities", "Availability", "Status"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // enable sorting

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            if (row >= 0 && row < rooms.size()) {
                Room r = rooms.get(row);
                showDetails(formatRoomDetails(r));
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
