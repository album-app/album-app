package mdc.ida.hips.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class HIPSCatalog extends ArrayList<HIPSolution> {

	private final HIPSInstallation parent;
	private final StringProperty name = new SimpleStringProperty();
	private final BooleanProperty isLocal = new SimpleBooleanProperty();

	public HIPSCatalog(HIPSInstallation parent) {
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

	public HIPSInstallation getParent() {
		return parent;
	}
}
