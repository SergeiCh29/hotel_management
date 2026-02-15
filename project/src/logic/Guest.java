package logic;

import java.util.ArrayList;
import java.util.List;

public class Guest extends Person{
    private int loyaltyPoints;
    private String nationality;
    private List<Booking> bookingHistory = new ArrayList<>();

    public Guest(int ID, String Firstname, String Lastname, String Email, String PhoneNumber, int loyaltyPoints, String nationality) {
        super(ID, Firstname, Lastname, Email, PhoneNumber);
        this.loyaltyPoints = loyaltyPoints;
        this.nationality = nationality;
    }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public List<Booking> getBookingHistory() { return bookingHistory; }
    public void setBookingHistory(List<Booking> bookingHistory) { this.bookingHistory = bookingHistory; }

    @Override
    public String toString(){
        return super.toString() + ", Nationality: " + nationality + ", Loyalty points: " + loyaltyPoints + ", Total Bookings: " + bookingHistory.size();
    }

    public int getTotalNightsStayed() {
        int totalNights = 0;
        for (Booking booking : bookingHistory) {
            totalNights += booking.getNumberOfNights();
        }
        return totalNights;
    }

    public void addBooking(Booking booking) {
        bookingHistory.add(booking);
    }

    public int addLoyaltyPoints (int points) {
        return this.loyaltyPoints = loyaltyPoints+points;
    }

    public boolean isVIP() {
        if (loyaltyPoints > 1000 || bookingHistory.size() > 5) {
            return true;
        }
        return false;
    }
}
