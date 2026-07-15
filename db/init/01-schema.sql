CREATE TYPE user_role AS ENUM ('PASSENGER', 'DRIVER', 'ADMIN');
CREATE TYPE driver_status AS ENUM ('AVAILABLE', 'UNAVAILABLE', 'DRIVING');
CREATE TYPE vehicle_type AS ENUM ('STANDARD', 'LUXURY', 'VAN');
CREATE TYPE ride_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'ACTIVE', 'FINISHED', 'CANCELLED');
CREATE TYPE change_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE notification_type AS ENUM ('RIDE_UPDATE', 'PANIC', 'CHAT', 'REMINDER', 'SYSTEM');
CREATE TYPE chat_sender AS ENUM ('USER', 'ADMIN');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(30),
    profile_picture_url VARCHAR(255) DEFAULT '/images/default-profile.png',
    role user_role NOT NULL,
    is_activated BOOLEAN NOT NULL DEFAULT FALSE,
    activation_token UUID,
    activation_token_expiry TIMESTAMP,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    block_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE driver_details (
    user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    status driver_status NOT NULL DEFAULT 'UNAVAILABLE'
);

CREATE TABLE vehicles (
    id SERIAL PRIMARY KEY,
    driver_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    model VARCHAR(100) NOT NULL,
    type vehicle_type NOT NULL,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    seat_count INTEGER NOT NULL,
    baby_transport BOOLEAN NOT NULL DEFAULT FALSE,
    pet_transport BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE profile_change_requests (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requested_data JSONB NOT NULL,
    status change_request_status NOT NULL DEFAULT 'PENDING',
    reviewed_by INTEGER REFERENCES users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE vehicle_pricing (
    vehicle_type vehicle_type PRIMARY KEY,
    base_price NUMERIC(10,2) NOT NULL,
    price_per_km NUMERIC(10,2) NOT NULL DEFAULT 120
);

CREATE TABLE rides (
    id SERIAL PRIMARY KEY,
    passenger_id INTEGER NOT NULL REFERENCES users(id),
    driver_id INTEGER REFERENCES users(id),
    vehicle_type vehicle_type NOT NULL,
    status ride_status NOT NULL DEFAULT 'PENDING',
    start_address VARCHAR(255) NOT NULL,
    start_lat NUMERIC(9,6),
    start_lng NUMERIC(9,6),
    end_address VARCHAR(255) NOT NULL,
    end_lat NUMERIC(9,6),
    end_lng NUMERIC(9,6),
    distance_km NUMERIC(6,2),
    price NUMERIC(10,2),
    baby_transport BOOLEAN NOT NULL DEFAULT FALSE,
    pet_transport BOOLEAN NOT NULL DEFAULT FALSE,
    scheduled_time TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    cancel_reason TEXT,
    cancelled_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ride_stops (
    id SERIAL PRIMARY KEY,
    ride_id INTEGER NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    address VARCHAR(255) NOT NULL,
    lat NUMERIC(9,6),
    lng NUMERIC(9,6),
    stop_order INTEGER NOT NULL
);

CREATE TABLE ride_passengers (
    id SERIAL PRIMARY KEY,
    ride_id INTEGER NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id),
    email VARCHAR(150) NOT NULL
);

CREATE TABLE ratings (
    id SERIAL PRIMARY KEY,
    ride_id INTEGER NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    passenger_id INTEGER NOT NULL REFERENCES users(id),
    driver_rating SMALLINT CHECK (driver_rating BETWEEN 1 AND 5),
    vehicle_rating SMALLINT CHECK (vehicle_rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE favorite_routes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    start_address VARCHAR(255) NOT NULL,
    start_lat NUMERIC(9,6),
    start_lng NUMERIC(9,6),
    end_address VARCHAR(255) NOT NULL,
    end_lat NUMERIC(9,6),
    end_lng NUMERIC(9,6),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE favorite_route_stops (
    id SERIAL PRIMARY KEY,
    favorite_route_id INTEGER NOT NULL REFERENCES favorite_routes(id) ON DELETE CASCADE,
    address VARCHAR(255) NOT NULL,
    lat NUMERIC(9,6),
    lng NUMERIC(9,6),
    stop_order INTEGER NOT NULL
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_ride_id INTEGER REFERENCES rides(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE panic_reports (
    id SERIAL PRIMARY KEY,
    ride_id INTEGER NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    reason TEXT,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE inconsistency_reports (
    id SERIAL PRIMARY KEY,
    ride_id INTEGER NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chat_messages (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender chat_sender NOT NULL,
    admin_id INTEGER REFERENCES users(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_rides_passenger ON rides(passenger_id);
CREATE INDEX idx_rides_driver ON rides(driver_id);
CREATE INDEX idx_rides_status ON rides(status);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX idx_chat_user ON chat_messages(user_id);
