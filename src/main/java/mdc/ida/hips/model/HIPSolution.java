package mdc.ida.hips.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class HIPSolution {
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty group = new SimpleStringProperty();
	private final StringProperty version = new SimpleStringProperty();
	private final StringProperty title = new SimpleStringProperty();
	private final StringProperty description = new SimpleStringProperty();
	private List<SolutionArgument> args;

	public String getTitle() {
		return title.get();
	}

	public StringProperty titleProperty() {
		return title;
	}

	public void setTitle(String title) {
		this.title.set(title);
	}

	public String getDescription() {
		return description.get();
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public void setDescription(String description) {
		this.description.set(description);
	}

	public List<SolutionArgument> getArgs() {
		return args;
	}

	public void setArgs(List<SolutionArgument> args) {
		this.args = args;
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

	public String getGroup() {
		return group.get();
	}

	public StringProperty groupProperty() {
		return group;
	}

	public void setGroup(String group) {
		this.group.set(group);
	}

	public String getVersion() {
		return version.get();
	}

	public StringProperty versionProperty() {
		return version;
	}

	public void setVersion(String version) {
		this.version.set(version);
	}
}
