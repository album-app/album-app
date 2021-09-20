package mdc.ida.album.model;

import org.scijava.event.SciJavaEvent;

/**
 * In contrast to {@link CollectionUpdatedEvent}, this event will only share a collection of catalogs NOT populated with solutions
 * (in order to be able to quickly deal the list of catalogs without handling all solutions)
 */
public class CatalogListUpdatedEvent extends SciJavaEvent {
	private final SolutionCollection collection;

	public CatalogListUpdatedEvent(SolutionCollection collection) {
		this.collection = collection;
	}

	public SolutionCollection getCollection() {
		return collection;
	}
}
