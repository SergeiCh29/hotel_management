CREATE TYPE room_type AS ENUM ('Single', 'Double', 'Deluxe', 'Suite');
CREATE TYPE room_status AS ENUM ('Clean', 'Dirty', 'Maintenance', 'Occupied');
CREATE TYPE booking_status AS ENUM ('Confirmed', 'Checked-in', 'Checked-out', 'Cancelled', 'No-show');

CREATE TABLE guests (
    guest_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phobe VARCHAR(50),

    loyalty_points INTEGER DEFAULT 0,
    nationality VARCHAR(50)
);

CREATE TABLE rooms (
    room_number INTEGER PRIMARY KEY,
    room_type room_type NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    amenities TEXT,
    status room_status DEFAULT 'Clean',
    max_occupancy INTEGER NOT NULL,
    has_balcony BOOLEAN DEFAULT FALSE
);

CREATE TABLE  bookings (
    booking_id SERIAL PRIMARY KEY,
    guest_id INTEGER NOT NULL REFERENCES guests(guest_id) ON DELETE RESTRICT,
    room_number INTEGER NOT NULL REFERENCES rooms(room_number) ON DELETE RESTRICT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INTEGER NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status booking_status DEFAULT 'Confirmed',
    is_paid BOOLEAN DEFAULT FALSE,
    payment_method VARCHAR(30),
    CONSTRAINT check_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT check_guests CHECK (number_of_guests > 0)
);

CREATE INDEX idx_guests_email ON guests(email);
CREATE INDEX idx_guests_name ON guests(last_name, first_name);
CREATE INDEX idx_rooms_type ON rooms(room_type);
CREATE INDEX idx_rooms_price ON rooms(price_per_night);
CREATE INDEX idx_rooms_available ON rooms(is_available);
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_guest ON bookings(guest_id);
CREATE INDEX idx_bookings_status ON bookings(status);