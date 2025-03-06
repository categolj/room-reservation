CREATE TYPE EVENT_TYPE AS ENUM ('RESERVATION_CONFIRMED', 'RESERVATION_CANCELLED', 'ROOM_CREATED', 'AVAILABILITY_CREATED', 'USER_CREATED');

CREATE TABLE event_store (
    event_id UUID PRIMARY KEY NOT NULL,
    event_type EVENT_TYPE NOT NULL,
    payload JSONB NOT NULL
);
CREATE INDEX idx_event_store_event_type ON event_store(event_type);

CREATE TABLE room_view (
    room_id UUID PRIMARY KEY NOT NULL,
    room_name VARCHAR(64) NOT NULL
);

CREATE TABLE room_availability_view (
    room_id UUID NOT NULL REFERENCES room_view(room_id) ON DELETE CASCADE,
    date DATE NOT NULL,
    PRIMARY KEY (room_id, date)
);

CREATE TABLE user_view (
    user_id UUID PRIMARY KEY NOT NULL,
    email VARCHAR(256) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL
);

CREATE TABLE reservation_view (
    reservation_id UUID PRIMARY KEY NOT NULL,
    room_id UUID NOT NULL REFERENCES room_view(room_id) ON DELETE CASCADE,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    purpose VARCHAR(256) NOT NULL,
    user_id UUID NOT NULL REFERENCES user_view(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_event_store_reservation_id ON event_store((payload ->> 'reservationId'));
CREATE INDEX idx_event_store_room_id_date ON event_store((payload ->> 'roomId'), (payload ->> 'date'));
CREATE INDEX idx_event_store_user_id ON event_store((payload ->> 'userId'));

CREATE INDEX idx_reservation_view_room_id_date ON reservation_view(room_id, date);