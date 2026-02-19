package utils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import logic.*;

public class ExcelImporter {
    // Import Rooms from Excel
    public static List<Room> importRooms(String filePath) throws Exception {
        List<Room> rooms = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int roomNumber = (int) getNumericValue(row.getCell(0));
                String roomTypeStr = getCellValue(row.getCell(1));
                double price = getNumericValue(row.getCell(2));
                int maxOcc = (int) getNumericValue(row.getCell(3));
                boolean hasBalcony = getNumericValue(row.getCell(4)) > 0;
                String amenities = getCellValue(row.getCell(5));
                boolean isAvailable = getNumericValue(row.getCell(6)) > 0;
                String statusStr = getCellValue(row.getCell(7));

                RoomType type = RoomType.valueOf(roomTypeStr.toUpperCase());
                Room room = new Room(roomNumber, type, price, maxOcc, hasBalcony, isAvailable);

                if (!amenities.isEmpty()) {
                    for (String amenity : amenities.split(",")) {
                        room.addAmenity(amenity.trim());
                    }
                }

                if (!statusStr.isEmpty()) {
                    room.setStatus(RoomStatus.valueOf(statusStr));
                }

                rooms.add(room);
            }
        }
        return rooms;
    }

    // Import Guests from Excel
    public static List<Guest> importGuests(String filePath) throws Exception {
        List<Guest> guests = new ArrayList<>();
        try(FileInputStream fis = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int id = (int) getNumericValue(row.getCell(0));
                String firstName = getCellValue(row.getCell(1));
                String lastName = getCellValue(row.getCell(2));
                String email = getCellValue(row.getCell(3));
                String phone = getCellValue(row.getCell(4));
                int loyaltyPoints = (int) getNumericValue(row.getCell(5));
                String nationality = getCellValue(row.getCell(6));

                Guest guest = new Guest(id, firstName, lastName, email, phone, loyaltyPoints, nationality);
                guests.add(guest);
            }
        }
        return guests;
    }


    // Import Bookings from Excel
    public static List<Booking> importBookings(String filePath) throws Exception {
        List<Booking> bookings = new ArrayList<>();
        try(FileInputStream fis = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int bookingId = (int) getNumericValue(row.getCell(0));
                int guestId = (int) getNumericValue(row.getCell(1));
                int roomNumber = (int) getNumericValue(row.getCell(2));
                LocalDate checkInDate = parseDate(row.getCell(3));
                LocalDate checkOutDate = parseDate(row.getCell(4));
                int numberOfGuests = (int) getNumericValue(row.getCell(5));
                double totalPrice = getNumericValue(row.getCell(6));
                String status = getCellValue(row.getCell(7));

                Guest guest = new Guest(guestId, "", "", "", "", 0, "");
                Room room = new Room(roomNumber, RoomType.SINGLE, 0.0, 1, false, true);

                Booking booking = new Booking(bookingId, guest, room, checkInDate, checkOutDate, numberOfGuests);

                if (totalPrice > 0) {
                    booking.setTotalPrice(totalPrice);
                }

                if (!status.isEmpty()) {
                    booking.setStatus(BookingStatus.valueOf(status.toUpperCase()));
                }

                bookings.add(booking);
            }
        }
        return bookings;
    }

    // Helper methods
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private static double getNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    private static LocalDate parseDate(Cell cell) {
        if (cell == null) return LocalDate.now();
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            try {
                return LocalDate.parse(cell.getStringCellValue());
            } catch (Exception e) {
                return LocalDate.now();
            }
        }
    }
}
