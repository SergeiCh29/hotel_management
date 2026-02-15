package logic;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private int roomNumber;
    private RoomType roomType;
    private double roomPricePerNight;
    private int maxOccupancy;
    private boolean hasBalcony;
    private List<String> amenities = new ArrayList<>();
    private boolean isAvailable;
    private RoomStatus status;



    public Room(int roomNumber, RoomType RoomType, double RoomPricePerNight, int maxOccupancy, boolean hasBalcony, boolean IsAvailable) {
        this.roomNumber = roomNumber;
        this.roomType = RoomType;
        this.roomPricePerNight = RoomPricePerNight;
        this.maxOccupancy = maxOccupancy;
        this.hasBalcony = hasBalcony;
//        for (String amenity : amenities) {this.amenities.add(amenity);}
        this.isAvailable = IsAvailable;
        this.status = RoomStatus.CLEAN;
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

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setIsAvailable( boolean IsAvailable ) {
        this.isAvailable = IsAvailable;
    }

    public List<String> getAmenities() {return amenities;}
    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }
    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public boolean hasBalcony() {return hasBalcony;}
    public void setHasBalcony(boolean hasBalcony) {
        this.hasBalcony = hasBalcony;
    }

    @Override
    public String toString(){
        return "logic.Room: " + roomNumber + ", logic.Room type: " + roomType + ", logic.Room price: €" + roomPricePerNight + ", Maximum occupancy: " + maxOccupancy + ", Availability: " + isAvailable + "" + ", Status: " + status;
    }

    public void addAmenity(String item) {
        amenities.add(item);
    }
    public double calculatePriceForStay(int nights) {
        return roomPricePerNight * nights;
    }

    public boolean canAccommodate(int guests) {
        if (guests > maxOccupancy) {
            return false;
        }
        return true;
    }
}

