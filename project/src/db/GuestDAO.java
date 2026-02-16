package db;

import logic.Guest;
import logic.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {
    public void addGuest(Guest guest) {
        String sql = "INSERT INTO guests (first_name, " +
                "last_name, email, phone, loyalty_points, nationality) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING guest_id";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getEmail());
            pstmt.setString(4, guest.getPhone());
            pstmt.setInt(5, guest.getLoyaltyPoints());
            pstmt.setString(6, guest.getNationality());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                guest.setId(generatedId);  // you need a setId method in Guest (inherited from Person)
            }
            System.out.println("Guest added: " + guest.getFullName() + " (ID: " + guest.getId() + ")");

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.err.println("Email already exists: " + guest.getEmail());
            } else {
                e.printStackTrace();
            }
        }
    }

    public List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guests ORDER BY guest_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Guest guest = extractGuestFromResultSet(rs);
                guests.add(guest);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guests;
    }

    public Guest getGuest(int guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, guestId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractGuestFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateGuest(Guest guest) {
        String sql = "UPDATE guests SET first_name = ?, last_name = ?, email = ?, phone = ?, loyalty_points = ?, nationality = ? WHERE guest_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getEmail());
            pstmt.setString(4, guest.getPhone());
            pstmt.setInt(5, guest.getLoyaltyPoints());
            pstmt.setString(6, guest.getNationality());
            pstmt.setInt(7, guest.getId());
            pstmt.executeUpdate();
            System.out.println("Guest updated successfully" + guest.getId());

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.err.println("Email already exists: " + guest.getEmail());
            } else {
                e.printStackTrace(); // Other SQL error
            }
        }
    }

    public void deleteGuest(int guestId) {
        String sql = "DELETE FROM guests WHERE guest_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, guestId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Guest findByEmail(String email) {
        Guest guest = null;
        return guest;
    }

    public List<Guest> searchGuestsByName(String name) { return List.of(); }

    private Guest extractGuestFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("guest_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        int loyaltyPoints = rs.getInt("loyalty_points");
        String nationality = rs.getString("nationality");

        Guest guest = new Guest(id, firstName, lastName, email, phone, loyaltyPoints, nationality);
       return guest;
    }


}
