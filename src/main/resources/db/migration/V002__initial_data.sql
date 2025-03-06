INSERT INTO event_store (event_id, event_type, payload)
VALUES ('018422b2-4843-7a62-935b-b4e65649de50', 'ROOM_CREATED',
        '{"roomId": "018422b2-4843-7a62-935b-b4e65649de3e", "roomName": "Large Conference Room A", "timestamp": "2025-01-15T09:00:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de51', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de3f", "roomName": "Medium Conference Room B", "timestamp": "2025-01-15T09:05:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de52', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de40", "roomName": "Small Conference Room C", "timestamp": "2025-01-15T09:10:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de53', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de41", "roomName": "Meeting Room 1", "timestamp": "2025-01-15T09:15:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de54', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de42", "roomName": "Meeting Room 2", "timestamp": "2025-01-15T09:20:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de55', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de43", "roomName": "Brainstorming Room", "timestamp": "2025-01-15T09:25:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de56', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de44", "roomName": "Project Room", "timestamp": "2025-01-15T09:30:00Z", "createdBy": "admin"}'),
    ('018422b2-4843-7a62-935b-b4e65649de57', 'ROOM_CREATED',
     '{"roomId": "018422b2-4843-7a62-935b-b4e65649de45", "roomName": "Remote Conference Room", "timestamp": "2025-01-15T09:35:00Z", "createdBy": "admin"}');

INSERT INTO room_view (room_id, room_name)
VALUES ('018422b2-4843-7a62-935b-b4e65649de3e', 'Large Conference Room A'),
    ('018422b2-4843-7a62-935b-b4e65649de3f', 'Medium Conference Room B'),
    ('018422b2-4843-7a62-935b-b4e65649de40', 'Small Conference Room C'),
    ('018422b2-4843-7a62-935b-b4e65649de41', 'Meeting Room 1'),
    ('018422b2-4843-7a62-935b-b4e65649de42', 'Meeting Room 2'),
    ('018422b2-4843-7a62-935b-b4e65649de43', 'Brainstorming Room'),
    ('018422b2-4843-7a62-935b-b4e65649de44', 'Project Room'),
    ('018422b2-4843-7a62-935b-b4e65649de45', 'Remote Conference Room');

INSERT INTO room_availability_view (room_id, date)
SELECT r.room_id, CURRENT_DATE + (n || ' days')::INTERVAL AS DATE
FROM room_view r
         CROSS JOIN generate_series(0, 364) AS n;

-- Insert availability data into event_store with modified UUIDs as event_ids
INSERT INTO event_store (event_id, event_type, payload)
SELECT
    -- Use the modified room_id (with '935' replaced by days elapsed) as event_id
    CAST(REPLACE(CAST(ra.room_id AS TEXT), '935',
                 LPAD(CAST((ra.date - CURRENT_DATE) AS TEXT), 3, '0')) AS UUID) AS event_id,
    'AVAILABILITY_CREATED'::EVENT_TYPE AS event_type,
    jsonb_build_object(
            'roomId', CAST(ra.room_id AS TEXT),
            'date', TO_CHAR(ra.date, 'YYYY-MM-DD')
    ) AS payload
FROM room_availability_view ra;
