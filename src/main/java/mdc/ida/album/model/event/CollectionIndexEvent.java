package mdc.ida.album.model.event;

import mdc.ida.album.model.SolutionCollection;
import org.scijava.event.SciJavaEvent;

public class CollectionIndexEvent extends SciJavaEvent {
	private final SolutionCollection collection;

	public CollectionIndexEvent(SolutionCollection collection) {
		this.collection = collection;
	}

	public SolutionCollection getCollection() {
		return collection;
	}
}
