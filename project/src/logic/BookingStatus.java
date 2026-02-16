package logic;

public enum BookingStatus {
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked-in"),
    CHECKED_OUT("Checked-out"),
    CANCELLED("Cancelled");

    private final String dbValue;

    BookingStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static BookingStatus fromDbValue(String dbValue) {
        if (dbValue == null) return CONFIRMED;

        for (BookingStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue) ||
                    status.name().equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        return CONFIRMED; // default fallback
    }
}
