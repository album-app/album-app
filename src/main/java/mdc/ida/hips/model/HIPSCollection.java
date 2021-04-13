package mdc.ida.hips.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HIPSCollection extends ArrayList<HIPSolution> {
	public static HIPSCollection fromJSON(JsonNode jsonNode) {
		HIPSCollection collection = new HIPSCollection();
		Iterator<Map.Entry<String, JsonNode>> groups = jsonNode.fields();
		while (groups.hasNext()) {
			Map.Entry<String, JsonNode> group = groups.next();
			String groupName = group.getKey();
			Iterator<Map.Entry<String, JsonNode>> names = group.getValue().fields();
			while (names.hasNext()) {
				Map.Entry<String, JsonNode> name = names.next();
				String nameName = name.getKey();
				Iterator<Map.Entry<String, JsonNode>> versions = name.getValue().fields();
				while (versions.hasNext()) {
					Map.Entry<String, JsonNode> version = versions.next();
					HIPSolution solution = new HIPSolution();
					solution.group = groupName;
					solution.name = nameName;
					solution.version = version.getKey();
					JsonNode args = version.getValue().get("args");
					List<SolutionArgument> hipsArgs = new ArrayList<>();
					if(args != null) {
						for (JsonNode arg : args) {
							SolutionArgument hipsArg = new SolutionArgument();
							hipsArg.name = arg.get("name").asText();
							hipsArg.description = arg.get("description").asText();
							hipsArg.type = arg.get("type").asText();
							hipsArgs.add(hipsArg);
						}
					}
					solution.args = hipsArgs;
					collection.add(solution);
				}
			}
		}
		return collection;
	}
}
