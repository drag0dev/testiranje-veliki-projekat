-- Password for every dummy user -> password123
INSERT INTO users (email, password_hash, first_name, last_name, address, phone_number, role, is_activated) VALUES
    ('admin@rideapp.com',    '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Ana',    'Admin',    'Bulevar Oslobođenja 1, Novi Sad', '0601234567', 'ADMIN', TRUE),
    ('alice@example.com',    '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Alice',  'Johnson',  'Zmaj Jovina 5, Novi Sad',         '0601111111', 'PASSENGER', TRUE),
    ('bob@example.com',      '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Bob',    'Smith',    'Futoška 20, Novi Sad',            '0602222222', 'PASSENGER', TRUE),
    ('carol@example.com',    '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Carol',  'Williams', 'Bulevar Evrope 10, Novi Sad',     '0603333333', 'PASSENGER', TRUE),
    ('driver1@rideapp.com',  '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Marko',  'Petrović', 'Temerinska 15, Novi Sad',         '0604444444', 'DRIVER', TRUE),
    ('driver2@rideapp.com',  '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Jovana', 'Nikolić',  'Rumenačka 30, Novi Sad',          '0605555555', 'DRIVER', TRUE),
    ('driver3@rideapp.com',  '$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS', 'Stefan', 'Ilić',     'Kisačka 8, Novi Sad',             '0606666666', 'DRIVER', TRUE);

INSERT INTO driver_details (user_id, status) VALUES
    ((SELECT id FROM users WHERE email = 'driver1@rideapp.com'), 'AVAILABLE'),
    ((SELECT id FROM users WHERE email = 'driver2@rideapp.com'), 'AVAILABLE'),
    ((SELECT id FROM users WHERE email = 'driver3@rideapp.com'), 'UNAVAILABLE');

INSERT INTO vehicles (driver_id, model, type, license_plate, seat_count, baby_transport, pet_transport) VALUES
    ((SELECT id FROM users WHERE email = 'driver1@rideapp.com'), 'Toyota Corolla',  'STANDARD', 'NS-123-AB', 4, TRUE,  FALSE),
    ((SELECT id FROM users WHERE email = 'driver2@rideapp.com'), 'Mercedes E-Class','LUXURY',   'NS-456-CD', 4, FALSE, FALSE),
    ((SELECT id FROM users WHERE email = 'driver3@rideapp.com'), 'VW Transporter',  'VAN',      'NS-789-EF', 7, TRUE,  TRUE);

INSERT INTO vehicle_pricing (vehicle_type, base_price, price_per_km) VALUES
    ('STANDARD', 150, 120),
    ('LUXURY',   300, 120),
    ('VAN',      250, 120);

INSERT INTO rides (passenger_id, driver_id, vehicle_type, status, start_address, start_lat, start_lng, end_address, end_lat, end_lng, distance_km, price, start_time, end_time, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'alice@example.com'),
     (SELECT id FROM users WHERE email = 'driver1@rideapp.com'),
     'STANDARD', 'FINISHED',
     'Zmaj Jovina 5, Novi Sad', 45.2551, 19.8452,
     'Spens, Novi Sad', 45.2481, 19.8320,
     3.2, 534.00,
     now() - interval '2 days', now() - interval '2 days' + interval '15 minutes',
     now() - interval '2 days');

INSERT INTO ratings (ride_id, passenger_id, driver_rating, vehicle_rating, comment) VALUES
    ((SELECT id FROM rides ORDER BY id LIMIT 1), (SELECT id FROM users WHERE email = 'alice@example.com'), 5, 4, 'Great driver, smooth ride.');

INSERT INTO rides (passenger_id, driver_id, vehicle_type, status, start_address, start_lat, start_lng, end_address, end_lat, end_lng, distance_km, price, start_time, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'bob@example.com'),
     (SELECT id FROM users WHERE email = 'driver2@rideapp.com'),
     'LUXURY', 'ACTIVE',
     'Futoška 20, Novi Sad', 45.2496, 19.8286,
     'Aerodrom Novi Sad', 45.1968, 19.8355,
     8.5, 1320.00,
     now() - interval '5 minutes', now() - interval '5 minutes');

INSERT INTO rides (passenger_id, driver_id, vehicle_type, status, start_address, start_lat, start_lng, end_address, end_lat, end_lng, distance_km, price, start_time, end_time, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'bob@example.com'),
     (SELECT id FROM users WHERE email = 'driver1@rideapp.com'),
     'STANDARD', 'FINISHED',
     'Futoška 20, Novi Sad', 45.2496, 19.8286,
     'Petrovaradinska tvrđava, Novi Sad', 45.2517, 19.8628,
     4.8, 726.00,
     now() - interval '1 day', now() - interval '1 day' + interval '20 minutes',
     now() - interval '1 day');

INSERT INTO rides (passenger_id, driver_id, vehicle_type, status, start_address, start_lat, start_lng, end_address, end_lat, end_lng, distance_km, price, start_time, end_time, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'carol@example.com'),
     (SELECT id FROM users WHERE email = 'driver2@rideapp.com'),
     'LUXURY', 'FINISHED',
     'Bulevar Evrope 10, Novi Sad', 45.2465, 19.8501,
     'Sajmište, Novi Sad', 45.2398, 19.8395,
     3.6, 582.00,
     now() - interval '6 hours', now() - interval '6 hours' + interval '18 minutes',
     now() - interval '6 hours');

INSERT INTO rides (passenger_id, vehicle_type, status, start_address, end_address, distance_km, price, scheduled_time, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'carol@example.com'),
     'VAN', 'PENDING',
     'Bulevar Evrope 10, Novi Sad', 'Železnička stanica, Novi Sad',
     2.1, 502.00,
     now() + interval '3 hours', now());

INSERT INTO rides (passenger_id, driver_id, vehicle_type, status, start_address, end_address, distance_km, price, cancel_reason, cancelled_by, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'alice@example.com'),
     (SELECT id FROM users WHERE email = 'driver3@rideapp.com'),
     'VAN', 'CANCELLED',
     'Kisačka 8, Novi Sad', 'Liman, Novi Sad',
     4.0, 730.00,
     'Passenger did not show up at pickup location',
     (SELECT id FROM users WHERE email = 'driver3@rideapp.com'),
     now() - interval '1 day');

INSERT INTO favorite_routes (user_id, name, start_address, end_address) VALUES
    ((SELECT id FROM users WHERE email = 'alice@example.com'), 'Home to Work', 'Zmaj Jovina 5, Novi Sad', 'Bulevar Mihajla Pupina 6, Novi Sad'),
    ((SELECT id FROM users WHERE email = 'bob@example.com'),   'Weekend trip', 'Futoška 20, Novi Sad', 'Petrovaradinska tvrđava, Novi Sad');

INSERT INTO notifications (user_id, type, message, is_read) VALUES
    ((SELECT id FROM users WHERE email = 'alice@example.com'), 'RIDE_UPDATE', 'Your ride has been accepted by Marko Petrović.', TRUE),
    ((SELECT id FROM users WHERE email = 'bob@example.com'),   'RIDE_UPDATE', 'Your ride is currently in progress.', FALSE),
    ((SELECT id FROM users WHERE email = 'carol@example.com'), 'REMINDER',    'Your scheduled ride starts in 15 minutes.', FALSE);

INSERT INTO chat_messages (user_id, sender, admin_id, message) VALUES
    ((SELECT id FROM users WHERE email = 'alice@example.com'), 'USER',  NULL, 'Hi, I was overcharged for my last ride.'),
    ((SELECT id FROM users WHERE email = 'alice@example.com'), 'ADMIN', (SELECT id FROM users WHERE email = 'admin@rideapp.com'), 'Hi Alice, let me check that for you.');

INSERT INTO inconsistency_reports (ride_id, user_id, message) VALUES
    ((SELECT id FROM rides WHERE status = 'FINISHED' LIMIT 1), (SELECT id FROM users WHERE email = 'alice@example.com'), 'Driver took a longer route than necessary.');
