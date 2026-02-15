package db;
import logic.Guest;
import logic.Room;
import logic.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public boolean isRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut, Integer excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_number = ? " +
                "AND status NOT IN ('CANCELLED') " +
                "AND check_in_date < ? AND check_out_date > ?";
        if (excludeBookingId != null) {
            sql += " AND booking_id != ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, checkIn.toString());
            pstmt.setString(3, checkOut.toString());
            pstmt.setInt(4, excludeBookingId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void addBooking(Booking booking) {
        String sql = "INSERT INTO bookings(guest_id, room_id) VALUES(?, ?)";
        if (isRoomAvailable(booking.getRoom().getRoomNumber(), booking.getCheckInDate(),  booking.getCheckOutDate(), booking.getRoom().getRoomNumber())
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, booking.getGuest().getId());   // guest ID
            pstmt.setInt(2, booking.getRoom().getRoomNumber()); // room number
        } catch (SQLException e) {
            if (e.getSQLState().equals("23503")) {
                System.err.println("The chosen guest might not exist " + booking.getGuest().getId());
                System.err.println("The chosen room might not exist " + booking.getRoom().getRoomNumber());
            } else {
                e.printStackTrace(); // Other SQL error
            }
        }
    }
}
