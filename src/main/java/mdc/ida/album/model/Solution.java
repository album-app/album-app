package mdc.ida.album.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.List;

public class Solution {
	public static class Citation {
		final String text;
		final String doi;
		public Citation(String text, String doi) {
			this.text = text;
			this.doi = doi;
		}

		public String getDOI() {
			return doi;
		}

		public String getText() {
			return text;
		}
	}

	public static class Cover {
		final String src;
		final String description;
		public Cover(String src, String description) {
			this.src = src;
			this.description = description;
		}

		public String getSrc() {
			return src;
		}

		public String getDescription() {
			return description;
		}
	}

	public Solution(AlbumInstallation installation) {
		this.installation = installation;
	}

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
	private final ListProperty<String> authors = new SimpleListProperty<>();
	private final ListProperty<Citation> cite = new SimpleListProperty<>();
	private final BooleanProperty installed = new SimpleBooleanProperty();
	private final BooleanProperty blocked = new SimpleBooleanProperty();
	private final StringProperty blockedMessage = new SimpleStringProperty();
	private final ListProperty<String> tags = new SimpleListProperty<>();
	private final ListProperty<Cover> covers = new SimpleListProperty<>();
	private List<SolutionArgument> args;
	private final AlbumInstallation installation;

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

	public ObservableList<Cover> getCovers() {
		return covers.get();
	}

	public ListProperty<Cover> coversProperty() {
		return covers;
	}

	public void setCovers(ObservableList<Cover> covers) {
		this.covers.set(covers);
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

	public ObservableList<String> getTags() {
		return tags.get();
	}

	public ListProperty<String> tagsProperty() {
		return tags;
	}

	public void setTags(ObservableList<String> tags) {
		this.tags.set(tags);
	}

	public ObservableList<String> getAuthors() {
		return authors.get();
	}

	public ListProperty<String> authorsProperty() {
		return authors;
	}

	public void setAuthors(ObservableList<String> authors) {
		this.authors.set(authors);
	}

	public ObservableList<Citation> getCite() {
		return cite.get();
	}

	public ListProperty<Citation> citeProperty() {
		return cite;
	}

	public void setCite(ObservableList<Citation> cite) {
		this.cite.set(cite);
	}

	public AlbumInstallation getInstallation() {
		return installation;
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

	public boolean isInstalled() {
		return installed.get();
	}

	public BooleanProperty installedProperty() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed.set(installed);
	}

	public boolean isBlocked() {
		return blocked.get();
	}

	public BooleanProperty blockedProperty() {
		return blocked;
	}

	public String getBlockedMessage() {
		return blockedMessage.get();
	}

	public StringProperty blockedMessageProperty() {
		return blockedMessage;
	}

	public void block(String message) {
		this.blocked.set(true);
		this.blockedMessage.set(message);
	}

	public void unblock() {
		this.blocked.set(false);
		this.blockedMessage.set("");
	}

	public static int compareVersions(String version1, String version2) {
		//FIXME use proper version comparison implementation
		return version1.compareTo(version2);
	}

	public static int compareVersions(Solution solution1, Solution solution2) {
		//FIXME use proper version comparison implementation
		return solution1.getVersion().compareTo(solution2.getVersion());
	}
}
