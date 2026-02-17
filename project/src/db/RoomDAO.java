package db;
import logic.Room;
import logic.RoomType;
import logic.RoomStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public void addRoom(Room room)  throws SQLException{
        String sql = "INSERT INTO rooms (room_number,room_type, price_per_night, " +
                "max_occupancy, has_balcony, amenities, is_available, status) " +
                "VALUES (?, CAST(? AS room_type), ?, ?, ?, ?, ?, CAST(? AS room_status))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType().name());
            pstmt.setDouble(3, room.getRoomPricePerNight());
            pstmt.setInt(4, room.getMaxOccupancy());
            pstmt.setBoolean(5, room.hasBalcony());   // add getter if missing
            // Convert amenities list to comma-separated string
            String amenitiesStr = String.join(",", room.getAmenities());
            pstmt.setString(6, amenitiesStr);
            pstmt.setBoolean(7, room.isAvailable());
            pstmt.setString(8, room.getStatus().getDbValue());

            pstmt.executeUpdate();
            System.out.println("Room added: " + room.getRoomNumber());

        }
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = extractRoomFromResultSet(rs);
                rooms.add(room);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    // SELECT room by number
    public Room getRoomByNumber(int roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractRoomFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_type=CAST(? AS room_type), price_per_night=?, max_occupancy=?, " +
                "has_balcony=?, amenities=?, is_available=?, status=CAST(? AS room_status) WHERE room_number=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomType().name());
            pstmt.setDouble(2, room.getRoomPricePerNight());
            pstmt.setInt(3, room.getMaxOccupancy());
            pstmt.setBoolean(4, room.hasBalcony());
            String amenitiesStr = String.join(",", room.getAmenities());
            pstmt.setString(5, amenitiesStr);
            pstmt.setBoolean(6, room.isAvailable());
            pstmt.setString(7, room.getStatus().getDbValue());
            pstmt.setInt(8, room.getRoomNumber());
            pstmt.executeUpdate();
            System.out.println("Room updated successfully: " + room.getRoomNumber());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
// UPDATE availability
public void updateAvailability(int roomNumber, boolean isAvailable) {
    String sql = "UPDATE rooms SET is_available = ? WHERE room_number = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setBoolean(1, isAvailable);
        pstmt.setInt(2, roomNumber);
        pstmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

// DELETE room
public void deleteRoom(int roomNumber) {
    String sql = "DELETE FROM rooms WHERE room_number = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, roomNumber);
        pstmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
    int roomNumber = rs.getInt("room_number");
    RoomType roomType = RoomType.valueOf(rs.getString("room_type"));
    double price = rs.getDouble("price_per_night");
    int maxOccupancy = rs.getInt("max_occupancy");
    boolean hasBalcony = rs.getBoolean("has_balcony");
    boolean isAvailable = rs.getBoolean("is_available");
    RoomStatus status = RoomStatus.fromDbValue(rs.getString("status"));

    Room room = new Room(roomNumber, roomType, price, maxOccupancy, hasBalcony, isAvailable);
    room.setStatus(status);

    // Parse amenities
    String amenitiesStr = rs.getString("amenities");
    if (amenitiesStr != null && !amenitiesStr.isEmpty()) {
        String[] items = amenitiesStr.split(", ");
        for (String item : items) {
            room.addAmenity(item.trim());
        }
    }

    return room;
}

public List<Room> findRoomsByType(RoomType type) {
    List<Room> rooms = new ArrayList<>();
    if (type == null) return rooms;

    String sql = "SELECT * FROM rooms WHERE room_type = CAST(? AS room_type) ORDER BY room_number";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, type.name());

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return rooms;
}

public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
    List<Room> availableRooms = new ArrayList<>();
    if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
        return availableRooms; // empty list for invalid input
    }

        String sql = "SELECT * FROM rooms r WHERE NOT EXISTS " +
            "(SELECT 1 FROM bookings b WHERE b.room_room_number = r.room_number " +
            "AND b.status NOT IN ('Cancelled') AND b.check_in_date < ? AND b.check_out_date > ?)" +
                "ORDER BY r.room_number";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setDate(1, Date.valueOf(checkOut));
        pstmt.setDate(2, Date.valueOf(checkIn));

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                availableRooms.add(extractRoomFromResultSet(rs));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return availableRooms;
}

public List<Room> findRoomsByPriceRange(double min,  double max) {
    List<Room> rooms = new ArrayList<>();
    if (min > max) {
        double temp = min;
        min = max;
        max = temp;
    }
    String sql = "SELECT * FROM rooms WHERE price_per_night BETWEEN ? AND ? ORDER BY price_per_night";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setDouble(1, min);
        pstmt.setDouble(2, max);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return rooms;
}

public void addRoomsBatch(List<Room> rooms) {
    String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, " +
            "max_occupancy, has_balcony, amenities, is_available, status) " +
            "VALUES (?, CAST(? AS room_type), ?, ?, ?, ?, ?, CAST(? AS room_status))";
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
        conn = DatabaseConnection.getConnection();
        pstmt = conn.prepareStatement(sql);
        conn.setAutoCommit(false); // disable auto-commit for batch
        int count = 0;
        int batchSize = 1000;
        for (Room room : rooms) {
            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType().name());
            pstmt.setDouble(3, room.getRoomPricePerNight());
            pstmt.setInt(4, room.getMaxOccupancy());
            pstmt.setBoolean(5, room.hasBalcony());
            String amenitiesStr = String.join(", ", room.getAmenities());
            pstmt.setString(6, amenitiesStr);
            pstmt.setBoolean(7, room.isAvailable());
            pstmt.setString(8, room.getStatus().getDbValue());
            pstmt.addBatch();
            // Execute every 1000 rows
            count++;
            if (count % batchSize == 0) {
                pstmt.executeBatch();
            }
        }
        pstmt.executeBatch(); // final batch
        conn.commit(); // commit transaction

    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        e.printStackTrace();
    } finally {
        // close resources
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
}


