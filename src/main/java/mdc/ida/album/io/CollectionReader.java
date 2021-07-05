package mdc.ida.album.io;

import com.fasterxml.jackson.databind.JsonNode;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionArgument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CollectionReader {

	public static SolutionCollection readCollection(AlbumInstallation installation, JsonNode jsonNode) {
		SolutionCollection collection = new SolutionCollection();
		Iterator<Map.Entry<String, JsonNode>> catalogs = jsonNode.fields();
		while (catalogs.hasNext()) {
			Map.Entry<String, JsonNode> catalogNode = catalogs.next();
			Catalog catalog = readCatalog(installation, catalogNode.getKey(), catalogNode.getValue());
			collection.add(catalog);
		}
		return collection;
	}

	private static Catalog readCatalog(AlbumInstallation installation, String name, JsonNode jsonNode) {
		Catalog catalog = new Catalog(installation);
		catalog.setName(name);
		for (JsonNode solutionNode : jsonNode) {
			Solution solution = readSolution(solutionNode);
			solution.setCatalog(name);
			catalog.add(solution);
		}
		return catalog;
	}

	private static Solution readSolution(JsonNode node) {
		Solution solution = new Solution();
		setStringAttr(node, solution::setDescription, "description");
		setStringAttr(node, solution::setName, "solution_name");
		setStringAttr(node, solution::setGroup, "solution_group");
		setStringAttr(node, solution::setVersion, "solution_version");
		setStringAttr(node, solution::setTitle, "title");
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
		if(jsonNode != null && jsonNode.isTextual()) {
			consumer.accept(jsonNode.asText());
		}
	}
}
