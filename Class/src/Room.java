public class Room {
    private int RoomID;
    private String RoomNumber;
    private String RoomType;
    private double RoomPrice;
    private boolean IsAvailable = true;

    public Room(int RoomID, String RoomNumber, String RoomType, double RoomPrice, boolean IsAvailable) {
        this.RoomID = RoomID;
        this.RoomNumber = RoomNumber;
        this.RoomType = RoomType;
        this.RoomPrice = RoomPrice;
        this.IsAvailable = IsAvailable;
    }

    public int getRoomID() { return RoomID; }
    public void setRoomID(int RoomID) { this.RoomID = RoomID; }

    public String getRoomNumber() { return RoomNumber; }
    public void setRoomNumber(String RoomNumber) { this.RoomNumber = RoomNumber; }

    public String getRoomType() { return RoomType; }
    public void setRoomType(String RoomType) { this.RoomType = RoomType; }

    public double getRoomPrice() { return RoomPrice; }
    public void setRoomPrice(double RoomPrice) { this.RoomPrice = RoomPrice; }

    public boolean getIsAvailable() { return IsAvailable; }
    public void setIsAvailable(boolean IsAvailable) { this.IsAvailable = IsAvailable; }

    @Override
    public String toString(){
        return "Room: " + RoomNumber + ", Room type: " + RoomType + ", Room price: " + RoomPrice + ", Availability: " + IsAvailable;
    }
}

