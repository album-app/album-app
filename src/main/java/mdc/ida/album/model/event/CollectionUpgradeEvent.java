package mdc.ida.album.model.event;

import mdc.ida.album.model.CollectionUpdates;
import org.scijava.event.SciJavaEvent;

public class CollectionUpgradeEvent extends SciJavaEvent {
	private final CollectionUpdates udpates;

	public CollectionUpgradeEvent(CollectionUpdates updates) {
		this.udpates = updates;
	}

	public CollectionUpdates getUdpates() {
		return udpates;
	}
}
