import db.*;
import logic.*;
import ui.HotelManagementGUI;
import utils.ExcelImporter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelApp {

    public static void main(String[] args) {
        try {
            System.out.println("Testing database connection...");
            try (Connection conn = DatabaseConnection.getConnection()) {
                System.out.println("Connected to database successfully.");
            }

            clearDatabase();
            importRoomsBatch();
            Map<Integer, Guest> guestMap = importGuestsFromExcel();
            System.out.println("Guest map size: " + guestMap.size());
            importBookingsBatch(guestMap);

            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    new HotelManagementGUI().setVisible(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception e) {
            System.err.println("Error during application startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void clearDatabase() {
        String[] tables = {"bookings", "rooms", "guests"};
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Disable foreign key checks temporarily
            stmt.execute("SET CONSTRAINTS ALL DEFERRED");

            for (String table : tables) {
                // TRUNCATE with RESTART IDENTITY resets the sequence
                stmt.executeUpdate("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
            }

            // Re-enable foreign key checks
            stmt.execute("SET CONSTRAINTS ALL IMMEDIATE");

            System.out.println("Database cleared and sequences reset.");
        } catch (SQLException e) {
            System.err.println("Failed to clear database: " + e.getMessage());
        }
    }

    private static void importRoomsBatch() {
        try {
            List<Room> rooms = ExcelImporter.importRooms("data/rooms.xlsx");
            RoomDAO roomDAO = new RoomDAO();
            roomDAO.addRoomsBatch(rooms); // batch insert
            System.out.println("Imported " + rooms.size() + " rooms.");
        } catch (Exception e) {
            System.err.println("Error importing rooms: " + e.getMessage());
        }
    }

    /**
     * Imports guests from Excel and returns a map.
     * Key: the original guest ID from the Excel file
     * Value: the Guest object with the new database-generated ID
     */
    private static Map<Integer, Guest> importGuestsFromExcel() {
        try {
            List<Guest> excelGuests = ExcelImporter.importGuests("data/guests.xlsx");
            GuestDAO guestDAO = new GuestDAO();
            Map<Integer, Guest> guestMap = guestDAO.addGuestsBatch(excelGuests);
            System.out.println("Imported " + guestMap.size() + " guests with auto-generated IDs.");
            return guestMap;
        } catch (Exception e) {
            System.err.println("Error importing guests: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // Imports bookings from Excel in batch using the guest map to resolve guest IDs.
    private static void importBookingsBatch(Map<Integer, Guest> guestMap) {
        try {
            List<Booking> bookings = ExcelImporter.importBookings("data/bookings.xlsx");

            List<Booking> bookingsToInsert = new ArrayList<>();
            RoomDAO roomDAO = new RoomDAO();

            for (Booking b : bookings) {
                int originalGuestId = b.getGuest().getId();
                Guest actualGuest = guestMap.get(originalGuestId);
                Room actualRoom = roomDAO.getRoomByNumber(b.getRoom().getRoomNumber());

                Booking bookingToInsert = new Booking(
                        0,
                        actualGuest,
                        actualRoom,
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getNumberOfGuests()
                );
                bookingToInsert.setStatus(b.getStatus());
                bookingToInsert.setIsPaid(b.isPaid());
                bookingToInsert.setPaymentMethod(b.getPaymentMethod());

                bookingsToInsert.add(bookingToInsert);
            }

            System.out.println("Bookings to insert: " + bookingsToInsert.size());

            if (!bookingsToInsert.isEmpty()) {
                BookingDAO bookingDAO = new BookingDAO();
                bookingDAO.addBookingsBatch(bookingsToInsert);
                System.out.println("Imported " + bookingsToInsert.size() + " bookings.");
            } else {
                System.out.println("No bookings to insert.");
            }

        } catch (Exception e) {
            System.err.println("Error importing bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}