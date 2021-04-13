package mdc.ida.hips.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mdc.ida.hips.HIPSClient;
import mdc.ida.hips.HIPSOptions;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSLaunchRequestEvent;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.SolutionArgument;
import org.scijava.command.DynamicCommandInfo;
import org.scijava.command.Inputs;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.IOException;

@Plugin(type = Service.class)
public class DefaultHIPSService extends AbstractService implements HIPSService {

	@Parameter
	LogService log;

	@Parameter
	UIService ui;

	@Parameter
	EventService eventService;

	private HIPSClient client;

	@Override
	public void init(HIPSOptions.Values options) {
		this.client = new HIPSClient(options.port());
		context().inject(client);
	}

	@Override
	public void updateAndDisplayIndex() {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "get_index";
		try {
			client.send(actionName, createHIPSRequest(mapper, actionName) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void launchSolution(HIPSolution solution) {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "launch_hips";
		ObjectNode actionArgs = mapper.createObjectNode();
		actionArgs.put("group", solution.group);
		actionArgs.put("name", solution.name);
		actionArgs.put("version", solution.version);
		ObjectNode solutionArgs = mapper.createObjectNode();
		actionArgs.set("args", solutionArgs);

		if(solution.args.size() > 0) {
			System.out.println("Harvesting inputs for " + solution.group + ":" + solution.name + ":" + solution.version + "...");
			Inputs inputs = new Inputs(getContext());
			for (SolutionArgument arg : solution.args) {
				inputs.addInput(createModuleItem(inputs.getInfo(), arg));
			}
			inputs.harvest();
			for (SolutionArgument arg : solution.args) {
				solutionArgs.put(arg.name, inputs.getInput(arg.name).toString());
			}
		}
		System.out.println("launching " + solution.group + ":" + solution.name + ":" + solution.version + "...");
		try {
			client.send(actionName, createHIPSRequest(mapper, actionName, actionArgs) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String createHIPSRequest(ObjectMapper mapper, String actionName) {
		return createHIPSRequest(mapper, actionName, mapper.createObjectNode());
	}

	private String createHIPSRequest(ObjectMapper mapper, String actionName, ObjectNode actionArgs) {
		ObjectNode request = mapper.createObjectNode();
		request.put("action", actionName);
		request.set("args", actionArgs);
		return request.toString();
	}

	@EventHandler
	public void launchSolution(HIPSLaunchRequestEvent event) {
		new Thread(() -> launchSolution(event.getSolution())).start();
	}

	private ModuleItem<?> createModuleItem(DynamicCommandInfo info, SolutionArgument arg) {
		if(arg.type.equals("file")) {
			ModuleItem<File> item = new DefaultMutableModuleItem<File>(info, arg.name, File.class);
			item.setDescription(arg.description);
			return item;
		}
		if(arg.type.equals("string")) {
			ModuleItem<String> item = new DefaultMutableModuleItem<String>(info, arg.name, String.class);
			item.setDescription(arg.description);
			return item;
		}
		return null;
	}

	@EventHandler
	public void updateIndex(HIPSCollectionUpdatedEvent event) {
		HIPSCollection collection = HIPSCollection.fromJSON(event.getCollection());
		ui.show(collection);
	}

	@Override
	public void handleServerResponse(String msg, String response) {
		if(response == null) return;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readTree(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(msg.equals("get_index")) {
			eventService.publish(new HIPSCollectionUpdatedEvent(jsonNode));
		}
	}

}
