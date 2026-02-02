import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {
    private int bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate, checkOutDate;
    private int numberOfGuests;
    private double totalPrice;
    private String status;
    public enum BookingStatus {
        CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    }
//    private String paymentMethod;
//    private boolean isPAid;

    public Booking(int bookingId, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests, String status) {
        this.bookingId = bookingId;
        this.guest = guest;
        guest.addBooking(this);
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalPrice = room.getRoomPricePerNight() * guest.getTotalNightsStayed();
        this.status = status;
    }

    public long getNumberOfNights(){
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int BookingID) { this.bookingId = BookingID; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate CheckInDate) {
        if (CheckInDate != null)
            this.checkInDate = CheckInDate;
    }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate CheckOutDate) {
        if (CheckOutDate != null && CheckOutDate.isAfter(checkInDate))
            this.checkOutDate = CheckOutDate;
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double TotalPrice) { this.totalPrice = TotalPrice; }

    public int getNumberOfGuests() { return numberOfGuests; }

    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    @Override
    public String toString(){
        return "Booking id: " + bookingId + ", Guest id: " + guest.getId() + ", Room number: " + room.getRoomNumber() + ", Check-in date: " + checkInDate + ", Check-out date: " + checkOutDate + ", Number of guests: " + numberOfGuests + ", Total price: " + totalPrice + ", Status: " + status;
    }

    public double calculateTotalPrice(int nights) {
        return this.totalPrice = room.calculatePriceForStay(nights);
    }

    public boolean isActive() {
        LocalDate currentDate = LocalDate.now();
        if ((currentDate.isAfter(checkInDate) || currentDate.isEqual(checkInDate)) && currentDate.isBefore(checkOutDate) && status.equals("Checked-in")) {
            return true;
        }
        return false;
    }

    public boolean isUpcoming() {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(checkInDate) && status.equals(BookingStatus.CHECKED_IN)) {
            return true;
        }
        return false;
    }

    public boolean isCompleted() {
        LocalDate currentDate = LocalDate.now();
        if ((currentDate.isAfter(checkOutDate) || currentDate.isEqual(checkOutDate)) && status.equals(BookingStatus.CHECKED_OUT)) {
            return true;
        }
        return false;
    }

    public boolean canCheckIn() {
        LocalDate currentDate = LocalDate.now();
        if ((currentDate.isEqual(checkInDate) || currentDate.isAfter(checkInDate)) && status.equals(BookingStatus.CONFIRMED)) {return true;}
        return false;
    }
}
