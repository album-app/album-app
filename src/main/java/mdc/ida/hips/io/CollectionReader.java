package mdc.ida.hips.io;

import com.fasterxml.jackson.databind.JsonNode;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSInstallation;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.SolutionArgument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CollectionReader {

	public static HIPSCollection readCollection(HIPSInstallation installation, JsonNode jsonNode) {
		HIPSCollection collection = new HIPSCollection();
		Iterator<Map.Entry<String, JsonNode>> catalogs = jsonNode.fields();
		while (catalogs.hasNext()) {
			Map.Entry<String, JsonNode> catalogNode = catalogs.next();
			HIPSCatalog catalog = readCatalog(installation, catalogNode.getKey(), catalogNode.getValue());
			collection.add(catalog);
		}
		return collection;
	}

	private static HIPSCatalog readCatalog(HIPSInstallation installation, String name, JsonNode jsonNode) {
		HIPSCatalog catalog = new HIPSCatalog(installation);
		catalog.setName(name);
		for (JsonNode solutionNode : jsonNode) {
			HIPSolution solution = readHIPSolution(solutionNode);
			solution.setCatalog(name);
			catalog.add(solution);
		}
		return catalog;
	}

	private static HIPSolution readHIPSolution(JsonNode node) {
		HIPSolution solution = new HIPSolution();
		setStringAttr(node, solution::setDescription, "description");
		setStringAttr(node, solution::setName, "solution_name");
		setStringAttr(node, solution::setGroup, "solution_group");
		setStringAttr(node, solution::setVersion, "solution_version");
		setStringAttr(node, solution::setTitle, "title");
		List<SolutionArgument> hipsArgs = new ArrayList<>();
		JsonNode args = node.get("args");
		if(args != null) {
			for (JsonNode arg : args) {
				SolutionArgument hipsArg = new SolutionArgument();
				setStringAttr(arg, hipsArg::setDescription, "description");
				setStringAttr(arg, hipsArg::setName, "name");
				setStringAttr(arg, hipsArg::setType, "type");
				hipsArgs.add(hipsArg);
			}
		}
		solution.setArgs(hipsArgs);
		return solution;
	}

	private static void setStringAttr(JsonNode arg, Consumer<String> consumer, String fieldName) {
		JsonNode jsonNode = arg.get(fieldName);
		if(jsonNode != null && jsonNode.isTextual()) {
			consumer.accept(jsonNode.asText());
		}
	}
}
