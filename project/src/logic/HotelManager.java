package logic;

import utils.ExcelImporter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

public class HotelManager {
    private List<Room> rooms;
    private List<Guest> guests;
    private List<Booking> bookings;

    public HotelManager() {
        this.rooms = new ArrayList<>();
        this.guests = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    public void loadData(String roomsFile, String guestsFile, String bookingsFile) {
        try{
            this.rooms = ExcelImporter.importRooms(roomsFile);
            this.guests = ExcelImporter.importGuests(guestsFile);
            this.bookings = ExcelImporter.importBookings(bookingsFile);
            System.out.println("Data loaded successfully");
        } catch (Exception e){
            System.err.println("Error loading data" + e.getMessage());
        }
    }

    public Guest searchGuestById(int guestId) {
        long startTime = System.nanoTime();
        List<Guest> sortedGuests = new ArrayList<>(guests);
        sortedGuests.sort(Comparator.comparing(Guest::getId));

        Guest result = binarySearchGuest(sortedGuests, guestId);
        long endTime = System.nanoTime();
        System.out.println("Binary Search took: " + (endTime - startTime) / 1000000.0 + " ms");
        return result;
    }

    public List<Room> searchRoomsByPriceRange(double minPrice, double maxPrice) {
        long startTime = System.nanoTime();
        List<Room> results = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getRoomPricePerNight() >= minPrice && room.getRoomPricePerNight() <= maxPrice) {
                results.add(room);
            }
        }
        long endTime = System.nanoTime();
        System.out.println("Price Range Search took: " + (endTime - startTime) / 1000000.0 + " ms");
        return results;
    }

    public List<Room> sortRoomsByPriceBubble() {
        long startTime = System.nanoTime();
        List<Room> sorted = new ArrayList<>(rooms);
        int n = sorted.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (sorted.get(j).getRoomPricePerNight() > sorted.get(j + 1).getRoomPricePerNight()) {
                    Room temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                }
            }
        }
        long endTime = System.nanoTime();
        System.out.println("Bubble Sort took: " + (endTime - startTime) / 1000000.0 + " ms");
        return sorted;
    }

    public List<Room> filterRoomsByType(RoomType roomType) {
        return rooms.stream()
                .filter(room -> room.getRoomType() == roomType)
                .collect(Collectors.toList());
    }

    public List<Room> filterRoomsByStatus(RoomStatus roomStatus) {
        return rooms.stream()
                .filter(room -> room.getStatus() == roomStatus)
                .collect(Collectors.toList());
    }

    public List<Booking> filterBookingsByStatus(BookingStatus bookingStatus) {
        return bookings.stream()
                .filter(booking -> booking.getStatus() == bookingStatus)
                .collect(Collectors.toList());
    }

    public List<Booking> filterBookingsByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return bookings.stream()
                .filter(booking -> !booking.getCheckInDate().isBefore(startDate) &&
                        !booking.getCheckInDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    private  Guest binarySearchGuest(List<Guest> sortedList, int guestId) {
        int left = 0, right = sortedList.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int midId = sortedList.get(mid).getId();
            if (midId == guestId) return sortedList.get(mid);
            if (midId < guestId) left = mid + 1;
            else right = mid - 1;
        }
        return null;
    }
}


