

import db.*;
import logic.*;
import ui.HotelManagementGUI; // assuming you place the GUI in ui package

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Main test class to verify database connectivity, DAO operations,
 * and to launch the hotel management GUI.
 */
public class HotelApp {

    public static void main(String[] args) {
        try {
            // 1. Test database connection
            System.out.println("Testing database connection...");
            try (Connection conn = DatabaseConnection.getConnection()) {
                System.out.println("✅ Connected to database successfully.");
            }

            clearDatabase();

            // 3. Insert sample data using DAOs
            insertSampleRooms();
            insertSampleGuests();
            insertSampleBookings();

            // 4. Launch the Swing GUI on the Event Dispatch Thread
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    new HotelManagementGUI().setVisible(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error during application startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recreates all tables (drops and creates) – use only in development!
     */
    private static void recreateTables() {
        // This requires a DatabaseSetup class or direct SQL execution.
        // For simplicity, we'll assume you have a separate script.
        // You could run your schema.sql here.
        System.out.println("Recreating tables (if needed)...");
        // Example: execute SQL statements from a file or directly.
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop tables in correct order (child first)
            stmt.execute("DROP TABLE IF EXISTS bookings CASCADE");
            stmt.execute("DROP TABLE IF EXISTS rooms CASCADE");
            stmt.execute("DROP TABLE IF EXISTS guests CASCADE");

            // Re-run your schema creation – you may have a method for that.
            // For now, just a placeholder.
            System.out.println("Tables dropped.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a few rooms for testing.
     */
    private static void insertSampleRooms() {
        RoomDAO roomDAO = new RoomDAO();
        List<Room> rooms = new ArrayList<>();

        rooms.add(new Room(101, RoomType.SINGLE, 80.0, 1, false, true));
        rooms.get(0).setStatus(RoomStatus.CLEAN);
        rooms.get(0).addAmenity("WiFi");
        rooms.get(0).addAmenity("TV");

        rooms.add(new Room(102, RoomType.DOUBLE, 120.0, 2, true, true));
        rooms.get(1).setStatus(RoomStatus.CLEAN);
        rooms.get(1).addAmenity("WiFi");
        rooms.get(1).addAmenity("Minibar");

        rooms.add(new Room(201, RoomType.DELUXE, 200.0, 3, true, true));
        rooms.get(2).setStatus(RoomStatus.CLEAN);
        rooms.get(2).addAmenity("WiFi");
        rooms.get(2).addAmenity("Jacuzzi");

        for (Room r : rooms) {
            try {
                roomDAO.addRoom(r);
                System.out.println("Inserted room: " + r.getRoomNumber());
            } catch (SQLException e) {
                System.err.println("Failed to insert room " + r.getRoomNumber() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Insert a few guests for testing.
     */
    private static List<Guest> insertedGuests = new ArrayList<>();

    private static void insertSampleGuests() {
        GuestDAO guestDAO = new GuestDAO();

        Guest g1 = new Guest(1, "John", "Doe", "john.doe@email.com", "123-456-7890", 150, "USA");
        Guest g2 = new Guest(2, "Jane", "Smith", "jane.smith@email.com", "098-765-4321", 200, "Canada");
        Guest g3 = new Guest(3, "Alice", "Brown", "alice.brown@email.com", "555-123-4567", 50, "UK");

        List<Guest> guests = List.of(g1, g2, g3);
        for (Guest g : guests) {
            guestDAO.addGuest(g);
            if (g.getId() != 0) {  // only add if insert succeeded
                insertedGuests.add(g);
            }
        }
    }

    /**
     * Insert a few bookings for testing.
     */
    private static void insertSampleBookings() {
        BookingDAO bookingDAO = new BookingDAO();
        RoomDAO roomDAO = new RoomDAO();
        if (insertedGuests.size() < 3) {
            System.err.println("Not enough guests inserted");
            return;
        }
        Guest g1 = insertedGuests.get(0);
        Guest g2 = insertedGuests.get(1);
        Guest g3 = insertedGuests.get(2);

        Room r101 = roomDAO.getRoomByNumber(101);
        Room r102 = roomDAO.getRoomByNumber(102);
        Room r201 = roomDAO.getRoomByNumber(201);

        // Create bookings (ID 0 = auto-generated)
        Booking b1 = new Booking(0, g1, r101, LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 1);
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setPaymentMethod("Credit Card");

        Booking b2 = new Booking(0, g2, r102, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4), 2);
        b2.setStatus(BookingStatus.CONFIRMED);
        b2.setPaymentMethod("Cash");

        Booking b3 = new Booking(0, g3, r201, LocalDate.now().plusDays(7), LocalDate.now().plusDays(10), 2);
        b3.setStatus(BookingStatus.CONFIRMED);
        b3.setPaymentMethod("Credit Card");

        List<Booking> bookings = List.of(b1, b2, b3);
        for (Booking b : bookings) {
            try {
                bookingDAO.insertBooking(b);
                System.out.println("Inserted booking for guest " + b.getGuest().getFullName() +
                        " in room " + b.getRoom().getRoomNumber());
            } catch (Exception e) {
                System.err.println("Failed to insert booking: " + e.getMessage());
            }
        }
    }

    private static void clearDatabase() {
        String[] tables = {"bookings", "rooms", "guests"};
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                stmt.executeUpdate("DELETE FROM " + table);
            }
            System.out.println("Database cleared.");
        } catch (SQLException e) {
            System.err.println("Failed to clear database: " + e.getMessage());
        }
    }
}
