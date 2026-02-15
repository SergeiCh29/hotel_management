package logic;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {

    private int bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate, checkOutDate;
    private int numberOfGuests;
    private double totalPrice;
    private BookingStatus status;
    private boolean isPaid = false;
    private String paymentMethod;

//    private String paymentMethod;
//    private boolean isPAid;

    public Booking(int bookingId, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        this.bookingId = bookingId;
        this.guest = guest;
        guest.addBooking(this);
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalPrice = room.getRoomPricePerNight() * guest.getTotalNightsStayed();
        this.status = BookingStatus.CONFIRMED;
        this.isPaid = false;
        this.paymentMethod = "";
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

    public BookingStatus getStatus() {return status;}
    public void setStatus(BookingStatus status) {this.status = status;}

    public boolean isPaid() { return isPaid; }
    public void setIsPaid(boolean isPaid) { this.isPaid = isPaid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String PaymentMethod) { this.paymentMethod = PaymentMethod; }


    @Override
    public String toString(){
        return "logic.Booking id: " + bookingId + ", logic.Guest id: " + guest.getId() + ", logic.Room number: " + room.getRoomNumber() + ", Check-in date: " + checkInDate + ", Check-out date: " + checkOutDate + ", Number of guests: " + numberOfGuests + ", Total price: " + totalPrice + ", Status: " + status;
    }

    public double calculateTotalPrice(int nights) {
        return this.totalPrice = room.calculatePriceForStay(nights);
    }

    public boolean isActive() {
        LocalDate currentDate = LocalDate.now();
        if ((currentDate.isAfter(checkInDate) || currentDate.isEqual(checkInDate)) && currentDate.isBefore(checkOutDate) && status == BookingStatus.CHECKED_IN) {
            return true;
        }
        return false;
    }

    public boolean isUpcoming() {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(checkInDate) && status == BookingStatus.CONFIRMED) {
            return true;
        }
        return false;
    }

    public boolean isCompleted() {
        LocalDate currentDate = LocalDate.now();
        if ((currentDate.isAfter(checkOutDate) || currentDate.isEqual(checkOutDate)) && status ==  BookingStatus.CHECKED_OUT) {
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
