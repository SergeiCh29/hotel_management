package logic;

public enum RoomStatus {
    CLEAN("Clean"),
    DIRTY("Dirty"),
    MAINTENANCE("Maintenance"),
    OCCUPIED("Occupied");

    private final String dbValue;

    RoomStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static RoomStatus fromDbValue(String dbValue) {
        for (RoomStatus status : values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        return CLEAN; // default
    }
}
