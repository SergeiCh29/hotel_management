package db;

import logic.Guest;
import logic.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Searches for guests whose first name or last name contains the given text (case‑insensitive).
    public List<Guest> searchByName(String namePart) {
        List<Guest> result = new ArrayList<>();

        // If the search string is null or empty, return all guests
        if (namePart == null || namePart.trim().isEmpty()) {
            return getAllGuests();
        }

        String searchPattern = "%" + namePart.trim().toLowerCase() + "%";
        String sql = "SELECT * FROM guests WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(extractGuestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Finds all guests who are VIP (loyalty points > 1000 OR more than 5 bookings)
    public List<Guest> findVIPGuests() {
        List<Guest> vipGuests = new ArrayList<>();

        String sql = """
        SELECT * FROM guests g
        WHERE g.loyalty_points > 1000
           OR (SELECT COUNT(*) FROM bookings b WHERE b.guests_guest_id = g.guest_id) > 5
        ORDER BY g.loyalty_points DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Guest guest = extractGuestFromResultSet(rs);
                vipGuests.add(guest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vipGuests;
    }

    public Guest findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM guests WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractGuestFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

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

    public Map<Integer, Guest> addGuestsBatch(List<Guest> excelGuests) throws SQLException {
        String sql = "INSERT INTO guests (first_name, last_name, email, phone, loyalty_points, nationality) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        Map<Integer, Guest> guestMap = new HashMap<>();

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            conn.setAutoCommit(false);

            for (Guest original : excelGuests) {
                pstmt.setString(1, original.getFirstName());
                pstmt.setString(2, original.getLastName());
                pstmt.setString(3, original.getEmail());
                pstmt.setString(4, original.getPhone());
                pstmt.setInt(5, original.getLoyaltyPoints());
                pstmt.setString(6, original.getNationality());
                pstmt.addBatch();
            }

            pstmt.executeBatch(); // single batch

            generatedKeys = pstmt.getGeneratedKeys();
            int index = 0;
            while (generatedKeys.next() && index < excelGuests.size()) {
                int newId = generatedKeys.getInt(1);
                Guest original = excelGuests.get(index++);
                Guest newGuest = new Guest(
                        newId,
                        original.getFirstName(),
                        original.getLastName(),
                        original.getEmail(),
                        original.getPhone(),
                        original.getLoyaltyPoints(),
                        original.getNationality()
                );
                guestMap.put(original.getId(), newGuest);
            }

            conn.commit();
            System.out.println("Batch inserted " + excelGuests.size() + " guests.");
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        return guestMap;
    }
}

