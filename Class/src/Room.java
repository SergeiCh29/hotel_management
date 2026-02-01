import java.util.ArrayList;
import java.util.List;

public class Room {
    enum RoomType {SINGLE, DOUBLE, DELUXE, SUITE}

    private int roomNumber;
    private RoomType roomType;
    private double roomPricePerNight;
    private boolean isAvailable;
    private List<String> amenities = new ArrayList<>();
    private String status;
    private int maxOccupancy;

    public Room(int RoomNumber, RoomType RoomType, double RoomPricePerNight, boolean IsAvailable, int maxOccupancy) {
        this.roomNumber = RoomNumber;
        this.roomType = RoomType;
        this.roomPricePerNight = RoomPricePerNight;
        this.maxOccupancy = maxOccupancy;
        this.isAvailable = IsAvailable;
        this.status = "Clean";
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int RoomNumber) {
        this.roomNumber = RoomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType RoomType ) {
        this.roomType = RoomType;
    }

    public double getRoomPricePerNight() {
        return roomPricePerNight;
    }

    public void setRoomPricePerNight( double RoomPricePerNight ) {
        this.roomPricePerNight = RoomPricePerNight;
    }

    public boolean IsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable( boolean IsAvailable ) {
        this.isAvailable = IsAvailable;
    }

    public List<String> getAmenities() {return amenities;}
    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    @Override
    public String toString(){
        return "Room: " + roomNumber + ", Room type: " + roomType + ", Room price: €" + roomPricePerNight + ", Maximum occupancy: " + maxOccupancy + ", Availability: " + isAvailable + "" + ", Status: " + status;
    }

    public double calculatePriceForStay(int nights) {
        return roomPricePerNight *nights;
    }

    public boolean canAccommodate(int guests) {
        if (guests > maxOccupancy) {
            return false;
        }
        return true;
    }
}

