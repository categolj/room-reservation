-- USER_CREATED event
INSERT INTO event_store (event_id, event_type, payload)
VALUES ('018422b2-4843-7a62-935b-b4e65649de58', 'USER_CREATED',
        '{"userId": "018422b2-4843-7a62-935b-b4e65649de46", "email": "tanaka@example.com", "password": "{noop}password123", "timestamp": "2025-02-01T10:00:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de59', 'USER_CREATED',
     '{"userId": "018422b2-4843-7a62-935b-b4e65649de47", "email": "yamada@example.com", "password": "{noop}securepass", "timestamp": "2025-02-01T10:15:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de5a', 'USER_CREATED',
     '{"userId": "018422b2-4843-7a62-935b-b4e65649de48", "email": "suzuki@example.com", "password": "{noop}suzuki2023", "timestamp": "2025-02-01T10:30:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de5b', 'USER_CREATED',
     '{"userId": "018422b2-4843-7a62-935b-b4e65649de49", "email": "sato@example.com", "password": "{noop}sato1234", "timestamp": "2025-02-01T10:45:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de5c', 'USER_CREATED',
     '{"userId": "018422b2-4843-7a62-935b-b4e65649de4a", "email": "kobayashi@example.com", "password": "{noop}koba5678", "timestamp": "2025-02-01T11:00:00Z"}');

-- RESERVATION_CONFIRMED event
INSERT INTO event_store (event_id, event_type, payload)
VALUES ('018422b2-4843-7a62-935b-b4e65649de5d', 'RESERVATION_CONFIRMED',
        '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de4b", "roomId": "018422b2-4843-7a62-935b-b4e65649de3e", "userId": "018422b2-4843-7a62-935b-b4e65649de46", "date": "2025-03-04", "startTime": "09:00:00", "endTime": "10:30:00", "purpose": "Weekly Project Meeting", "timestamp": "2025-03-01T09:00:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de5e', 'RESERVATION_CONFIRMED',
     '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de4c", "roomId": "018422b2-4843-7a62-935b-b4e65649de3f", "userId": "018422b2-4843-7a62-935b-b4e65649de47", "date": "2025-03-04", "startTime": "13:00:00", "endTime": "14:30:00", "purpose": "Client Meeting", "timestamp": "2025-03-01T10:15:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de5f', 'RESERVATION_CONFIRMED',
     '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de4d", "roomId": "018422b2-4843-7a62-935b-b4e65649de40", "userId": "018422b2-4843-7a62-935b-b4e65649de48", "date": "2025-03-05", "startTime": "10:00:00", "endTime": "11:00:00", "purpose": "1-on-1 Meeting", "timestamp": "2025-03-01T11:30:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de60', 'RESERVATION_CONFIRMED',
     '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de4e", "roomId": "018422b2-4843-7a62-935b-b4e65649de41", "userId": "018422b2-4843-7a62-935b-b4e65649de49", "date": "2025-03-05", "startTime": "15:00:00", "endTime": "16:30:00", "purpose": "Brainstorming Session", "timestamp": "2025-03-01T13:45:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649de61', 'RESERVATION_CONFIRMED',
     '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de4f", "roomId": "018422b2-4843-7a62-935b-b4e65649de42", "userId": "018422b2-4843-7a62-935b-b4e65649de4a", "date": "2025-03-06", "startTime": "09:30:00", "endTime": "11:00:00", "purpose": "New Product Planning Meeting", "timestamp": "2025-03-02T09:20:00Z"}'),
    ('018422b2-4843-7a62-935b-b4e65649f121', 'RESERVATION_CONFIRMED',
     '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de50", "roomId": "018422b2-4843-7a62-935b-b4e65649de3e", "userId": "018422b2-4843-7a62-935b-b4e65649de4a", "date": "2025-03-04", "startTime": "11:00:00", "endTime": "11:30:00", "purpose": "1-on-1 Meeting", "timestamp": "2025-03-01T09:00:00Z"}');

-- RESERVATION_CANCELLED event
INSERT INTO event_store (event_id, event_type, payload)
VALUES ('018422b2-4843-7a62-935b-b4e65649de62', 'RESERVATION_CANCELLED',
        '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de4c", "roomId": "018422b2-4843-7a62-935b-b4e65649de3f", "userId": "018422b2-4843-7a62-935b-b4e65649de47", "date": "2025-03-04", "timestamp": "2025-03-03T08:30:00Z", "reason": "Postponed due to client schedule"}');

-- Reservation after cancellation above
INSERT INTO event_store (event_id, event_type, payload)
VALUES ('018422b2-4843-7a62-935b-b4e65649de63', 'RESERVATION_CONFIRMED',
        '{"reservationId": "018422b2-4843-7a62-935b-b4e65649de64", "roomId": "018422b2-4843-7a62-935b-b4e65649de3f", "userId": "018422b2-4843-7a62-935b-b4e65649de47", "date": "2025-03-07", "startTime": "10:00:00", "endTime": "11:30:00", "purpose": "Client Meeting (Rescheduled)", "timestamp": "2025-03-03T09:00:00Z"}');

-- Insert into user_view
INSERT INTO user_view (user_id, email, password)
VALUES ('018422b2-4843-7a62-935b-b4e65649de46', 'tanaka@example.com', '{noop}password123'),
    ('018422b2-4843-7a62-935b-b4e65649de47', 'yamada@example.com', '{noop}securepass'),
    ('018422b2-4843-7a62-935b-b4e65649de48', 'suzuki@example.com', '{noop}suzuki2023'),
    ('018422b2-4843-7a62-935b-b4e65649de49', 'sato@example.com', '{noop}sato1234'),
    ('018422b2-4843-7a62-935b-b4e65649de4a', 'kobayashi@example.com', '{noop}koba5678');

-- Insert into reservation_view (excluding cancelled reservations)
INSERT INTO reservation_view (reservation_id, room_id, date, start_time, end_time, purpose, user_id)
VALUES ('018422b2-4843-7a62-935b-b4e65649de4b', '018422b2-4843-7a62-935b-b4e65649de3e',
        '2025-03-04', '09:00:00', '10:30:00', 'Weekly Project Meeting',
        '018422b2-4843-7a62-935b-b4e65649de46'),
    ('018422b2-4843-7a62-935b-b4e65649de4d', '018422b2-4843-7a62-935b-b4e65649de40', '2025-03-05',
     '10:00:00', '11:00:00', '1-on-1 Meeting', '018422b2-4843-7a62-935b-b4e65649de48'),
    ('018422b2-4843-7a62-935b-b4e65649de4e', '018422b2-4843-7a62-935b-b4e65649de41', '2025-03-05',
     '15:00:00', '16:30:00', 'Brainstorming Session',
     '018422b2-4843-7a62-935b-b4e65649de49'),
    ('018422b2-4843-7a62-935b-b4e65649de4f', '018422b2-4843-7a62-935b-b4e65649de42', '2025-03-06',
     '09:30:00', '11:00:00', 'New Product Planning Meeting',
     '018422b2-4843-7a62-935b-b4e65649de4a'),
    ('018422b2-4843-7a62-935b-b4e65649de64', '018422b2-4843-7a62-935b-b4e65649de3f', '2025-03-07',
     '10:00:00', '11:30:00', 'Client Meeting (Rescheduled)',
     '018422b2-4843-7a62-935b-b4e65649de47'),
    ('018422b2-4843-7a62-935b-b4e65649de50', '018422b2-4843-7a62-935b-b4e65649de3e', '2025-03-04',
     '11:00:00', '11:30:00', '1-on-1 Meeting',
     '018422b2-4843-7a62-935b-b4e65649de4a');
