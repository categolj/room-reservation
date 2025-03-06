CREATE TABLE IF NOT EXISTS event_publication (
    id UUID NOT NULL,
    listener_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date ON event_publication(completion_date);

CREATE TABLE IF NOT EXISTS event_publication_archive (
    id UUID NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    event_type TEXT NOT NULL,
    listener_id TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    serialized_event TEXT NOT NULL,
    PRIMARY KEY (ID)
);
CREATE INDEX IF NOT EXISTS event_publication_archive_by_listener_id_and_serialized_event ON EVENT_PUBLICATION_ARCHIVE(LISTENER_ID, SERIALIZED_EVENT);
CREATE INDEX IF NOT EXISTS event_publication_archive_by_completion_date ON EVENT_PUBLICATION_ARCHIVE(COMPLETION_DATE);
