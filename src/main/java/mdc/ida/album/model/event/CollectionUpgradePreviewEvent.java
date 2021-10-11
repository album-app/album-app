package mdc.ida.album.model.event;

import mdc.ida.album.model.CollectionUpdates;

public class CollectionUpgradePreviewEvent extends CollectionUpgradeEvent {
	public CollectionUpgradePreviewEvent(CollectionUpdates updates) {
		super(updates);
	}
}
