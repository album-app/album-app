package mdc.ida.album.io;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.CatalogUpdate;
import mdc.ida.album.model.CollectionUpdates;
import mdc.ida.album.model.Coordinates;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionArgument;
import mdc.ida.album.model.SolutionCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CollectionReader {

	public static SolutionCollection readCollection(AlbumInstallation installation, JsonNode jsonNode) {
		SolutionCollection collection = new SolutionCollection(installation);
		for (JsonNode catalogNode : jsonNode.get("catalogs")) {
			Catalog catalog = readCatalog(installation, catalogNode.get("name").asText(), catalogNode);
			collection.add(catalog);
		}
		return collection;
	}

	private static Catalog readCatalog(AlbumInstallation installation, String name, JsonNode jsonNode) {
		JsonNode solutions = jsonNode.get("solutions");
		ArrayList<Solution> solutionList = new ArrayList<>();
		if(solutions != null) {
			for (JsonNode solutionNode : solutions) {
				Solution solution = readSolution(solutionNode);
				solution.setCatalogName(name);
				solution.setInstallation(installation);
				solutionList.add(solution);
			}
		}
		Catalog catalog = new Catalog(installation, solutionList);
		catalog.setName(name);
		catalog.setId(jsonNode.get("catalog_id").asInt());
		catalog.setSrc(jsonNode.get("src").asText());
		catalog.setPath(jsonNode.get("path").asText());
		return catalog;
	}

	private static Solution readSolution(JsonNode node) {
		Solution solution = new Solution();
		setIntAttr(node, solution::setCatalogId, "catalog_id");
		setStringAttr(node, solution::setDescription, "description");
		setStringAttr(node, solution::setName, "solution_name", "name");
		setStringAttr(node, solution::setGroup, "solution_group", "group");
		setStringAttr(node, solution::setVersion, "solution_version", "version");
		setStringAttr(node, solution::setDocumentation, "documentation");
		setListStringAttr(node, solution::setTags, "tags");
		setStringAttr(node, solution::setCite, "cite");
		setStringAttr(node, solution::setRepo, "git_repo");
		setStringAttr(node, solution::setLicense, "license");
		setStringAttr(node, solution::setAuthor, "authors");
		setStringAttr(node, solution::setTitle, "title");
		setBooleanAttr(node, solution::setInstalled, "installed");
		List<SolutionArgument> albumArgs = new ArrayList<>();
		JsonNode args = node.get("args");
		if(args != null) {
			for (JsonNode arg : args) {
				SolutionArgument albumArg = new SolutionArgument();
				setStringAttr(arg, albumArg::setDescription, "description");
				setStringAttr(arg, albumArg::setName, "name");
				setStringAttr(arg, albumArg::setType, "type");
				albumArgs.add(albumArg);
			}
		}
		solution.setArgs(albumArgs);
		return solution;
	}

	private static void setStringAttr(JsonNode arg, Consumer<String> consumer, String fieldName) {
		JsonNode jsonNode = arg.get(fieldName);
		if(jsonNode != null && (jsonNode.isTextual() || jsonNode.isNumber())) {
			consumer.accept(jsonNode.asText());
		}
	}

	private static void setBooleanAttr(JsonNode arg, Consumer<Boolean> consumer, String fieldName) {
		JsonNode jsonNode = arg.get(fieldName);
		if(jsonNode != null) {
			consumer.accept(jsonNode.asBoolean());
		}
	}

	private static void setIntAttr(JsonNode arg, Consumer<Integer> consumer, String fieldName) {
		JsonNode jsonNode = arg.get(fieldName);
		if(jsonNode != null && jsonNode.isInt()) {
			consumer.accept(jsonNode.asInt());
		}
	}

	private static void setListStringAttr(JsonNode arg, Consumer<ObservableList<String>> consumer, String fieldName) {
		JsonNode jsonNode = arg.get(fieldName);
		ObservableList<String> res = FXCollections.observableArrayList(new ArrayList<>());
		if(jsonNode != null) {
			for (JsonNode node : jsonNode) {
				if(node.isTextual()) {
					res.add(node.asText());
				}
			}
		}
		consumer.accept(res);
	}

	private static void setStringAttr(JsonNode arg, Consumer<String> consumer, String fieldName, String alternativeFieldName) {
		JsonNode jsonNode = arg.get(fieldName);
		if(jsonNode == null) jsonNode = arg.get(alternativeFieldName);
		if(jsonNode != null && jsonNode.isTextual()) {
			consumer.accept(jsonNode.asText());
		}
	}

	public static List<Solution> readSolutionsList(JsonNode node) {
		List<Solution> res = new ArrayList<>();
		for (JsonNode solutionNode : node.get("solutions")) {
			Solution solution = readSolution(solutionNode);
			res.add(solution);
		}
		return res;
	}

	public static CollectionUpdates readUpdates(AlbumInstallation installation, JsonNode node) {
		CollectionUpdates collectionUpdates = new CollectionUpdates(installation);
		for (JsonNode catalogChangesNode : node.get("changes")) {
			String catalog = catalogChangesNode.get("catalog").get("name").asText();
			List<CatalogUpdate> updates = new ArrayList<>();
			for (JsonNode changeNode : catalogChangesNode.get("solution_changes")) {
				updates.add(readChange(changeNode));
			}
			collectionUpdates.put(catalog, updates);
		}
		return collectionUpdates;
	}

	private static CatalogUpdate readChange(JsonNode changeNode) {
		return new CatalogUpdate(
				new Coordinates(
						changeNode.get("group").asText(),
						changeNode.get("name").asText(),
						changeNode.get("version").asText()
				),
				CatalogUpdate.Action.valueOf(changeNode.get("change_type").asText()),
				changeNode.get("change_log").asText()
		);
	}
}
