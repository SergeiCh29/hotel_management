public class Guest extends Person{
    private String LoyaltyLevel;
    private int TotalBookings;

    public Guest(int ID, String Firstname, String Lastname, String Email, String PhoneNumber, String Address, String LoyaltyLevel, int TotalBookings) {
        super(ID, Firstname, Lastname, Email, PhoneNumber, Address);
        this.LoyaltyLevel = LoyaltyLevel;
        this.TotalBookings = TotalBookings;
        }

    public String getLoyaltyLevel() { return LoyaltyLevel; }
    public void setLoyaltyLevel(String LoyaltyLevel) { this.LoyaltyLevel = LoyaltyLevel; }

    public int getTotalBookings() { return TotalBookings; }
    public void setTotalBookings(int TotalBookings) { this.TotalBookings = TotalBookings; }

    @Override
    public String toString(){
        return super.toString() + ", Loyalty Level: " + LoyaltyLevel + ", Total Bookings: " + TotalBookings;
    }
}
