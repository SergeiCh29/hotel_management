package ui;

import logic.*;
import utils.ExcelImporter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PerformancePanel extends JPanel {
    private HotelManager manager;
    private JTextArea resultArea;
    private JButton loadButton;
    private JButton binarySearchButton;
    private JButton priceRangeButton;
    private JButton bubbleSortButton;
    private JButton quickSortButton;
    private JButton filterRoomsTypeButton;
    private JButton filterRoomsStatusButton;
    private JButton filterBookingsStatusButton;
    private JButton filterBookingsDateButton;

    public PerformancePanel() {
        manager = new HotelManager();
        setLayout(new BorderLayout());

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        loadButton = new JButton("Load Data");
        binarySearchButton = new JButton("Binary Search Guest ID 1");
        priceRangeButton = new JButton("Price Range (80-150)");
        bubbleSortButton = new JButton("Bubble Sort Rooms");
        quickSortButton = new JButton("Quick Sort Rooms");
        filterRoomsTypeButton = new JButton("Filter Rooms Type SINGLE");
        filterRoomsStatusButton = new JButton("Filter Rooms Status CLEAN");
        filterBookingsStatusButton = new JButton("Filter Bookings CONFIRMED");
        filterBookingsDateButton = new JButton("Filter Bookings Next 7 Days");

        buttonPanel.add(loadButton);
        buttonPanel.add(binarySearchButton);
        buttonPanel.add(priceRangeButton);
        buttonPanel.add(bubbleSortButton);
        buttonPanel.add(quickSortButton);
        buttonPanel.add(filterRoomsTypeButton);
        buttonPanel.add(filterRoomsStatusButton);
        buttonPanel.add(filterBookingsStatusButton);
        buttonPanel.add(filterBookingsDateButton);

        // Result area
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Attach actions
        loadButton.addActionListener(e -> loadData());
        binarySearchButton.addActionListener(e -> runBinarySearch());
        priceRangeButton.addActionListener(e -> runPriceRangeSearch());
        bubbleSortButton.addActionListener(e -> runBubbleSort());
        quickSortButton.addActionListener(e -> runQuickSort());
        filterRoomsTypeButton.addActionListener(e -> runFilterRoomsByType());
        filterRoomsStatusButton.addActionListener(e -> runFilterRoomsByStatus());
        filterBookingsStatusButton.addActionListener(e -> runFilterBookingsByStatus());
        filterBookingsDateButton.addActionListener(e -> runFilterBookingsByDate());
    }

    private void loadData() {
        String roomsFile = "data/rooms.xlsx";
        String guestsFile = "data/guests.xlsx";
        String bookingsFile = "data/bookings.xlsx";

        // Quick check if files exist
        if (!new File(roomsFile).exists()) {
            resultArea.append("❌ rooms.xlsx not found in data/ folder.\n");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                resultArea.append("Loading data...\n");
                manager.loadData(roomsFile, guestsFile, bookingsFile);
                return null;
            }
            @Override
            protected void done() {
                resultArea.append("✅ Data loaded.\n");
            }
        };
        worker.execute();
    }

    private void runBinarySearch() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long start = System.nanoTime();
                Guest g = manager.searchGuestById(1);
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Binary search guest ID 1: %s (%.3f ms)\n",
                        g == null ? "not found" : g.getFullName(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runPriceRangeSearch() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long start = System.nanoTime();
                List<Room> results = manager.searchRoomsByPriceRange(80, 150);
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Price range 80-150: %d rooms (%.3f ms)\n",
                        results.size(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runBubbleSort() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long start = System.nanoTime();
                List<Room> sorted = manager.sortRoomsByPriceBubble();
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Bubble sort: %d rooms (%.3f ms)\n",
                        sorted.size(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runQuickSort() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // Use Collections.sort as a quick sort stand‑in
                List<Room> roomsCopy = new ArrayList<>(manager.getRooms());
                long start = System.nanoTime();
                roomsCopy.sort(Comparator.comparing(Room::getRoomPricePerNight));
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Quick sort (Collections.sort): %d rooms (%.3f ms)\n",
                        roomsCopy.size(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runFilterRoomsByType() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long start = System.nanoTime();
                List<Room> results = manager.filterRoomsByType(RoomType.SINGLE);
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Filter rooms by type SINGLE: %d rooms (%.3f ms)\n",
                        results.size(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runFilterRoomsByStatus() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long start = System.nanoTime();
                List<Room> results = manager.filterRoomsByStatus(RoomStatus.CLEAN);
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Filter rooms by status CLEAN: %d rooms (%.3f ms)\n",
                        results.size(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runFilterBookingsByStatus() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long start = System.nanoTime();
                List<Booking> results = manager.filterBookingsByStatus(BookingStatus.CONFIRMED);
                long end = System.nanoTime();
                double ms = (end - start) / 1_000_000.0;
                resultArea.append(String.format("Filter bookings by status CONFIRMED: %d bookings (%.3f ms)\n",
                        results.size(), ms));
                return null;
            }
        };
        worker.execute();
    }

    private void runFilterBookingsByDate() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                LocalDate start = LocalDate.now();
                LocalDate end = start.plusDays(7);
                long startTime = System.nanoTime();
                List<Booking> results = manager.filterBookingsByDateRange(start, end);
                long endTime = System.nanoTime();
                double ms = (endTime - startTime) / 1_000_000.0;
                resultArea.append(String.format("Filter bookings by date range next 7 days: %d bookings (%.3f ms)\n",
                        results.size(), ms));
                return null;
            }
        };
        worker.execute();
    }
}
