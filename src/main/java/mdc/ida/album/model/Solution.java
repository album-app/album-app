package mdc.ida.album.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.List;

public class Solution {
	private final StringProperty catalogName = new SimpleStringProperty();
	private final IntegerProperty catalogId = new SimpleIntegerProperty();
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty group = new SimpleStringProperty();
	private final StringProperty version = new SimpleStringProperty();
	private final StringProperty title = new SimpleStringProperty();
	private final StringProperty description = new SimpleStringProperty();
	private final StringProperty documentation = new SimpleStringProperty();
	private final StringProperty repo = new SimpleStringProperty();
	private final StringProperty license = new SimpleStringProperty();
	private final StringProperty author = new SimpleStringProperty();
	private final StringProperty cite = new SimpleStringProperty();
	private final ListProperty<String> tags = new SimpleListProperty<>();
	private List<SolutionArgument> args;
	private AlbumInstallation installation;

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

	public String getCatalogName() {
		return catalogName.get();
	}

	public StringProperty catalogNameProperty() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName.set(catalogName);
	}

	public String getDocumentation() {
		return documentation.get();
	}

	public StringProperty documentationProperty() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation.set(documentation);
	}

	public String getRepo() {
		return repo.get();
	}

	public StringProperty repoProperty() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo.set(repo);
	}

	public String getLicense() {
		return license.get();
	}

	public StringProperty licenseProperty() {
		return license;
	}

	public void setLicense(String license) {
		this.license.set(license);
	}

	public String getAuthors() {
		return author.get();
	}

	public StringProperty authorProperty() {
		return author;
	}

	public void setAuthor(String author) {
		this.author.set(author);
	}

	public ObservableList<String> getTags() {
		return tags.get();
	}

	public ListProperty<String> tagsProperty() {
		return tags;
	}

	public String getCite() {
		return cite.get();
	}

	public StringProperty citeProperty() {
		return cite;
	}

	public void setCite(String cite) {
		this.cite.set(cite);
	}

	public void setTags(ObservableList<String> tags) {
		this.tags.set(tags);
	}

	public AlbumInstallation getInstallation() {
		return installation;
	}

	public void setInstallation(AlbumInstallation installation) {
		this.installation = installation;
	}

	public int getCatalogId() {
		return catalogId.get();
	}

	public IntegerProperty catalogIdProperty() {
		return catalogId;
	}

	public void setCatalogId(int catalogId) {
		this.catalogId.set(catalogId);
	}
}
