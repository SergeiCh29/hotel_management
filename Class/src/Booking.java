import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {
    private int BookingID;
    private int GuestID;
    private int RoomID;
    private LocalDate CheckInDate;
    private LocalDate CheckOutDate;
    private double TotalPrice;
    private String Status;

    public Booking(int BookingID, int GuestID, int RoomID, LocalDate CheckInDate, LocalDate CheckOutDate, double TotalPrice, String Status) {
        this.BookingID = BookingID;
        this.GuestID = GuestID;
        this.RoomID = RoomID;
        this.CheckInDate = CheckInDate;
        this.CheckOutDate = CheckOutDate;
        this.TotalPrice = TotalPrice;
        this.Status = Status;
    }

    public long getNumberOfNights(){
        return ChronoUnit.DAYS.between(CheckInDate, CheckOutDate);
    }

    public int getBookingID() { return BookingID; }
    public void setBookingID(int BookingID) { this.BookingID = BookingID; }

    public int getGuestID() { return GuestID; }
    public void setGuestID(int GuestID) { this.GuestID = GuestID; }

    public int getRoomID() { return RoomID; }
    public void setRoomID(int RoomID) { this.RoomID = RoomID; }

    public LocalDate getCheckInDate() { return CheckInDate; }
    public void setCheckInDate(LocalDate CheckInDate) { this.CheckInDate = CheckInDate; }

    public LocalDate getCheckOutDate() { return CheckOutDate; }
    public void setCheckOutDate(LocalDate CheckOutDate) { this.CheckOutDate = CheckOutDate; }

    public double getTotalPrice() { return TotalPrice; }
    public void setTotalPrice(double TotalPrice) { this.TotalPrice = TotalPrice; }

    public String getStatus() { return Status; }
    public void setStatus(String Status) { this.Status = Status; }

    @Override
    public String toString(){
        return "BookingID: " + BookingID + "GuestID: " + GuestID + "RoomID: " + RoomID + "CheckInDate: " + CheckInDate + "CheckOutDate: " + CheckOutDate + "TotalPrice: " + TotalPrice + "Status: " + Status;
    }
}
