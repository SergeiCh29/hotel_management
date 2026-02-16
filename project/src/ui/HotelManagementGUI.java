package ui;
import db.*;
import logic.Booking;
import logic.Guest;
import logic.Room;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class HotelManagementGUI extends JFrame {
    private GuestDAO guestDAO;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;

    public HotelManagementGUI() throws SQLException {
        setTitle("Hotel Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        guestDAO = new GuestDAO();
        roomDAO = new RoomDAO();
        bookingDAO = new BookingDAO();

        // Load initial data (could be done in background)
        List<Guest> guests = guestDAO.getAllGuests();
        List<Room> rooms = roomDAO.getAllRooms();
        List<Booking> bookings = bookingDAO.getAllBookings();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Guests", new GuestsPanel(guests, guestDAO));
//        tabbedPane.addTab("Rooms", new RoomsPanel(rooms, roomDAO));
//        tabbedPane.addTab("Bookings", new BookingsPanel(bookings, bookingDAO));

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new HotelManagementGUI().setVisible(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
