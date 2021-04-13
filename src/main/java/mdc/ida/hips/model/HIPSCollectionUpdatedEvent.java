package mdc.ida.hips.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.scijava.event.SciJavaEvent;

public class HIPSCollectionUpdatedEvent extends SciJavaEvent {
	private final JsonNode collection;

	public HIPSCollectionUpdatedEvent(JsonNode collection) {
		this.collection = collection;
	}

	public JsonNode getCollection() {
		return collection;
	}
}
