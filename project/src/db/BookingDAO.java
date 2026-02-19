package db;
import logic.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Inserts a new booking into the database.
    public void insertBooking(Booking booking) throws RoomNotAvailableException, SQLException {
        // Validating availability
        if (!isRoomAvailable(booking.getRoom().getRoomNumber(), booking.getCheckInDate(), booking.getCheckOutDate(), null)) {
            throw new RoomNotAvailableException("Room " + booking.getRoom().getRoomNumber() +" is not available from " + booking.getCheckInDate() + " to " + booking.getCheckOutDate());
        }

        booking.recalculateTotalPrice();

        String sql = "INSERT INTO bookings (guests_guest_id, room_room_number, check_in_date, check_out_date, " +
                "number_of_guests, total_price, status, is_paid, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS booking_status), ?, ?) RETURNING booking_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, booking.getGuest().getId());
            pstmt.setInt(2, booking.getRoom().getRoomNumber());
            pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setInt(5, booking.getNumberOfGuests());
            pstmt.setDouble(6, booking.getTotalPrice());
            pstmt.setString(7, booking.getStatus().getDbValue());
            pstmt.setBoolean(8, booking.isPaid());
            pstmt.setString(9, booking.getPaymentMethod());

            // Executing the query and get the returned ID
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt("booking_id");
                    booking.setBookingId(generatedId);
                    System.out.println("Booking inserted with ID: " + generatedId);
                } else {
                    throw new SQLException("Creating booking failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            // Rethrow foreign key violations with clearer message
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Referenced guest or room does not exist.", e);
            }
            throw e;
        }
    }

    public void insertBookingWithoutCheck(Booking booking) throws SQLException {
        booking.recalculateTotalPrice();

        String sql = "INSERT INTO bookings (guests_guest_id, room_room_number, check_in_date, check_out_date, " +
                "number_of_guests, total_price, status, is_paid, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS booking_status), ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, booking.getGuest().getId());
            pstmt.setInt(2, booking.getRoom().getRoomNumber());
            pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setInt(5, booking.getNumberOfGuests());
            pstmt.setDouble(6, booking.getTotalPrice());
            pstmt.setString(7, booking.getStatus().getDbValue());
            pstmt.setBoolean(8, booking.isPaid());
            pstmt.setString(9, booking.getPaymentMethod());

            pstmt.executeUpdate();
            // Optionally retrieve generated ID if needed, but we skip for speed
            System.out.println("Booking inserted for room " + booking.getRoom().getRoomNumber());
        }
    }

    /**
     * Retrieves a booking by its ID, including the associated Guest and Room objects.
     * Uses a JOIN to fetch all data in one query.
     */
    public Booking getBookingById(int id) throws SQLException {
        String sql = "SELECT b.*, g.*, " +
                "r.room_number, r.room_type, r.price_per_night, r.max_occupancy, r.has_balcony, " +
                "r.amenities, r.is_available, r.status AS room_status " +
                "FROM bookings b " +
                "JOIN guests g ON b.guests_guest_id = g.guest_id " +
                "JOIN rooms r ON b.room_room_number = r.room_number " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookingFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Retrieves all bookings from the database, ordered by booking ID.
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, g.*, " +
                "r.room_number, r.room_type, r.price_per_night, r.max_occupancy, r.has_balcony, " +
                "r.amenities, r.is_available, r.status AS room_status " +
                "FROM bookings b " +
                "JOIN guests g ON b.guests_guest_id = g.guest_id " +
                "JOIN rooms r ON b.room_room_number = r.room_number " +
                "ORDER BY b.booking_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Updates an existing booking. If dates have changed, it rechecks availability (excluding the current booking)
    public void updateBooking(Booking booking) throws RoomNotAvailableException, SQLException {
        // Fetching the current booking from DB to compare dates
        Booking current = getBookingById(booking.getBookingId());
        if (current == null) {
            throw new SQLException("Booking with ID " + booking.getBookingId() + " not found.");
        }

        boolean datesChanged = !current.getCheckInDate().equals(booking.getCheckInDate()) || !current.getCheckOutDate().equals(booking.getCheckOutDate());
        boolean roomChanged = current.getRoom().getRoomNumber() != booking.getRoom().getRoomNumber();

        if (datesChanged || roomChanged) {
            if (!isRoomAvailable(booking.getRoom().getRoomNumber(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getBookingId())) {
                throw new RoomNotAvailableException("Room " + booking.getRoom().getRoomNumber() +
                        " is not available for the new dates.");
            }
            booking.recalculateTotalPrice();
        }

        String sql = "UPDATE bookings SET check_in_date = ?, check_out_date = ?, number_of_guests = ?, " +
                "total_price = ?, status = CAST(? AS booking_status), is_paid = ?, payment_method = ? " +
                "WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(2, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setInt(3, booking.getNumberOfGuests());
            pstmt.setDouble(4, booking.getTotalPrice());
            pstmt.setString(5, booking.getStatus().getDbValue());
            pstmt.setBoolean(6, booking.isPaid());
            pstmt.setString(7, booking.getPaymentMethod());
            pstmt.setInt(8, booking.getBookingId());

            pstmt.executeUpdate();
        }
    }

    // Deletes a booking by its ID
    public void deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            pstmt.executeUpdate();
        }
    }

    // Checks a guest into the hotel.
    public void checkIn(int bookingId) throws SQLException, IllegalStateException {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be checked in.");
        }
        if (LocalDate.now().isBefore(booking.getCheckInDate())) {
            throw new IllegalStateException("Cannot check in before the check-in date.");
        }

        // Updating booking status
        String sql = "UPDATE bookings SET status = CAST(? AS booking_status) WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, BookingStatus.CHECKED_IN.getDbValue());
            pstmt.setInt(2, bookingId);
            pstmt.executeUpdate();
        }

        // Updating room availability
        updateRoomAvailability(booking.getRoom().getRoomNumber(), false);
    }

    // Checks a guest out of the hotel.
    public void checkOut(int bookingId) throws SQLException, IllegalStateException {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found.");
        }
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Only checked-in bookings can be checked out.");
        }

        String sql = "UPDATE bookings SET status = CAST(? AS booking_status) WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, BookingStatus.CHECKED_OUT.getDbValue());
            pstmt.setInt(2, bookingId);
            pstmt.executeUpdate();
        }

        updateRoomAvailability(booking.getRoom().getRoomNumber(), true);
    }

    // Cancels a booking.
    public void cancelBooking(int bookingId) throws SQLException, IllegalStateException {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found.");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN ||
                booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cannot cancel a booking that is already checked in or out.");
        }

        String sql = "UPDATE bookings SET status = CAST(? AS booking_status) WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, BookingStatus.CANCELLED.getDbValue());
            pstmt.setInt(2, bookingId);
            pstmt.executeUpdate();
        }

        updateRoomAvailability(booking.getRoom().getRoomNumber(), true);
    }

    // Updates the payment information for a booking.
    public void updatePayment(int bookingId, String method) throws SQLException {
        String sql = "UPDATE bookings SET is_paid = ?, payment_method = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, true);
            pstmt.setString(2, method);
            pstmt.setInt(3, bookingId);
            pstmt.executeUpdate();
        }
    }

    // Checks if a room is available for a given date range. Excludes a specific booking ID
    private boolean isRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut,
                                    Integer excludeBookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE room_room_number = ? " +
                "AND status NOT IN ('Cancelled') " +
                "AND check_in_date < ? " +
                "AND check_out_date > ?";

        if (excludeBookingId != null) {
            sql += " AND booking_id != ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setDate(2, Date.valueOf(checkOut));
            pstmt.setDate(3, Date.valueOf(checkIn));

            if (excludeBookingId != null) {
                pstmt.setInt(4, excludeBookingId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                    // Gets the value from the first column (the count) as an integer, and returns true if the count is zero (no conflicting bookings = room available).
                }
            }
        }
        return false;
    }

    //Helper to update the availability of a room
    private void updateRoomAvailability(int roomNumber, boolean available) throws SQLException {
        String sql = "UPDATE rooms SET is_available = ? WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, available);
            pstmt.setInt(2, roomNumber);
            pstmt.executeUpdate();
        }
    }

    public List<Booking> findByDateRange(LocalDate start, LocalDate end) {
        List<Booking> bookings = new ArrayList<>();
        if (start == null || end == null || !end.isAfter(start)) {
            return bookings;
        }

        String sql = """
        SELECT b.*, g.*,
            r.room_number, r.room_type, r.price_per_night, r.max_occupancy, r.has_balcony,
            r.amenities, r.is_available, r.status AS room_status
        FROM bookings b
        JOIN guests g ON b.guests_guest_id = g.guest_id
        JOIN rooms r ON b.room_room_number = r.room_number
        WHERE b.check_in_date < ? AND b.check_out_date > ?
                ORDER BY b.check_in_date
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(end));
            pstmt.setDate(2, Date.valueOf(start));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;

    }

    public List<Booking> findByStatus(BookingStatus status) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
        SELECT b.*, g.*,
               r.room_number, r.room_type, r.price_per_night, r.max_occupancy, r.has_balcony,
               r.amenities, r.is_available, r.status AS room_status
        FROM bookings b
        JOIN guests g ON b.guests_guest_id = g.guest_id
        JOIN rooms r ON b.room_room_number = r.room_number
        WHERE b.status = CAST(? AS booking_status)
        ORDER BY b.check_in_date
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.getDbValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> findByGuest(int guestId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
        SELECT b.*, g.*,
               r.room_number, r.room_type, r.price_per_night, r.max_occupancy, r.has_balcony,
               r.amenities, r.is_available, r.status AS room_status
        FROM bookings b
        JOIN guests g ON b.guests_guest_id = g.guest_id
        JOIN rooms r ON b.room_room_number = r.room_number
        WHERE b.guests_guest_id = ?
        ORDER BY b.check_in_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, guestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

     // Extracts a Booking object from a ResultSet that contains joined data from bookings, guests, and rooms.
    private Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Guest guest = new Guest(
                rs.getInt("guest_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getInt("loyalty_points"),
                rs.getString("nationality")
        );
        RoomType roomType = RoomType.valueOf(rs.getString("room_type"));
        Room room = new Room(
                rs.getInt("room_number"),
                roomType,
                rs.getDouble("price_per_night"),
                rs.getInt("max_occupancy"),
                rs.getBoolean("has_balcony"),
                rs.getBoolean("is_available")
        );
        room.setStatus(RoomStatus.fromDbValue(rs.getString("room_status")));

        String amenitiesStr = rs.getString("amenities");
        if (amenitiesStr != null && !amenitiesStr.isEmpty()) {
            String[] items = amenitiesStr.split(",");
            for (String item : items) {
                room.addAmenity(item.trim());
            }
        }

        Booking booking = new Booking(
                rs.getInt("booking_id"),
                guest,
                room,
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("number_of_guests")
        );
        booking.setTotalPrice(rs.getDouble("total_price"));
        booking.setStatus(BookingStatus.fromDbValue(rs.getString("status")));
        booking.setIsPaid(rs.getBoolean("is_paid"));
        booking.setPaymentMethod(rs.getString("payment_method"));

        return booking;
    }

    public List<Booking> addBookingsBatch(List<Booking> bookings) throws SQLException {
        String sql = "INSERT INTO bookings (guests_guest_id, room_room_number, check_in_date, check_out_date, " +
                "number_of_guests, total_price, status, is_paid, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS booking_status), ?, ?) RETURNING booking_id";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            int count = 0;
            int batchSize = 1000;

            for (Booking b : bookings) {
                pstmt.setInt(1, b.getGuest().getId());
                pstmt.setInt(2, b.getRoom().getRoomNumber());
                pstmt.setDate(3, Date.valueOf(b.getCheckInDate()));
                pstmt.setDate(4, Date.valueOf(b.getCheckOutDate()));
                pstmt.setInt(5, b.getNumberOfGuests());
                pstmt.setDouble(6, b.getTotalPrice());
                pstmt.setString(7, b.getStatus().getDbValue());
                pstmt.setBoolean(8, b.isPaid());
                pstmt.setString(9, b.getPaymentMethod());
                pstmt.addBatch();

                count++;
                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                }
            }

            pstmt.executeBatch(); // final batch

            // Retrieve all generated booking IDs
            rs = pstmt.getGeneratedKeys();
            int index = 0;
            while (rs.next() && index < bookings.size()) {
                int generatedId = rs.getInt(1);
                bookings.get(index).setBookingId(generatedId);
                index++;
            }

            conn.commit();
            System.out.println("✅ Batch inserted " + bookings.size() + " bookings.");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return bookings;
    }
}
