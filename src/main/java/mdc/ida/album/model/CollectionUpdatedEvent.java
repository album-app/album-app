package mdc.ida.album.model;

import org.scijava.event.SciJavaEvent;

public class CollectionUpdatedEvent extends SciJavaEvent {
	private final SolutionCollection collection;

	public CollectionUpdatedEvent(SolutionCollection collection) {
		this.collection = collection;
	}

	public SolutionCollection getCollection() {
		return collection;
	}
}
