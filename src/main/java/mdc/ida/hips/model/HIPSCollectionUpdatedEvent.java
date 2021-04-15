package mdc.ida.hips.model;

import org.scijava.event.SciJavaEvent;

public class HIPSCollectionUpdatedEvent extends SciJavaEvent {
	private final HIPSCollection collection;

	public HIPSCollectionUpdatedEvent(HIPSCollection collection) {
		this.collection = collection;
	}

	public HIPSCollection getCollection() {
		return collection;
	}
}
