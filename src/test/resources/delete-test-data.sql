TRUNCATE reservation_view;
TRUNCATE user_view CASCADE;

DELETE
FROM event_store
WHERE event_type IN ('USER_CREATED', 'RESERVATION_CONFIRMED', 'RESERVATION_CANCELLED');