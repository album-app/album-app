package mdc.ida.album.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class Catalog extends ArrayList<Solution> {

	private final AlbumInstallation parent;
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty path = new SimpleStringProperty();
	private final StringProperty src = new SimpleStringProperty();
	private final BooleanProperty isLocal = new SimpleBooleanProperty();

	public Catalog(AlbumInstallation parent) {
		this.parent = parent;
	}

	public boolean isIsLocal() {
		return isLocal.get();
	}

	public BooleanProperty isLocalProperty() {
		return isLocal;
	}

	public void setIsLocal(boolean isLocal) {
		this.isLocal.set(isLocal);
	}

	public String getName() {
		return name.get();
	}

	public StringProperty nameProperty() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public AlbumInstallation getParent() {
		return parent;
	}

	public void setSrc(String src) {
		this.src.set(src);
	}

	public void setPath(String path) {
		this.path.set(path);
	}

	public String getPath() {
		return path.get();
	}
}
