package mdc.ida.album.model.event;

import mdc.ida.album.model.SolutionCollection;
import org.scijava.event.SciJavaEvent;

/**
 * In contrast to {@link CollectionIndexEvent}, this event will only share a collection of catalogs, they will not be populated with solutions
 * (in order to be able to quickly deal the list of catalogs without handling all solutions)
 */
public class CatalogListEvent extends SciJavaEvent {
	private final SolutionCollection collection;

	public CatalogListEvent(SolutionCollection collection) {
		this.collection = collection;
	}

	public SolutionCollection getCollection() {
		return collection;
	}
}
