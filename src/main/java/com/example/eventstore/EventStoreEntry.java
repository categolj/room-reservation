package com.example.eventstore;

import com.example.event.EventType;
import java.util.UUID;
import org.jilt.Builder;
import org.jilt.BuilderStyle;

@Builder(style = BuilderStyle.STAGED)
public record EventStoreEntry(UUID eventId, EventType eventType, String payload) {

}