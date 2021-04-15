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
import org.scijava.app.StatusService;
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
import java.util.function.Consumer;

@Plugin(type = Service.class)
public class DefaultHIPSServerService extends AbstractService implements HIPSServerService {

	@Parameter
	LogService log;

	@Parameter
	UIService ui;

	@Parameter
	EventService eventService;

	@Parameter
	StatusService statusService;

	private HIPSClient client;

	@Override
	public void init(HIPSOptions.Values options) {
		this.client = new HIPSClient(options.port());
		context().inject(client);
	}

	@Override
	public void updateIndex(Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "get_index";
		JsonNode response = client.send(createHIPSRequest(mapper, actionName) + "\n");
		if(response == null) return;
		statusService.showStatus("Updated HIPS collection.");
		HIPSCollectionUpdatedEvent event = new HIPSCollectionUpdatedEvent(HIPSCollection.fromJSON(response));
		callback.accept(event);
		eventService.publish(event);
	}

	@Override
	public void launchSolution(HIPSolution solution) {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "launch_hips";
		ObjectNode actionArgs = mapper.createObjectNode();
		actionArgs.put("group", solution.getGroup());
		actionArgs.put("name", solution.getName());
		actionArgs.put("version", solution.getVersion());
		ObjectNode solutionArgs = mapper.createObjectNode();
		actionArgs.set("args", solutionArgs);

		if(solution.getArgs().size() > 0) {
			System.out.println("Harvesting inputs for " + solution.getGroup() + ":" + solution.getName() + ":" + solution.getVersion() + "...");
			Inputs inputs = new Inputs(getContext());
			for (SolutionArgument arg : solution.getArgs()) {
				inputs.addInput(createModuleItem(inputs.getInfo(), arg));
			}
			inputs.harvest();
			for (SolutionArgument arg : solution.getArgs()) {
				solutionArgs.put(arg.name, inputs.getInput(arg.name).toString());
			}
		}
		System.out.println("launching " + solution.getGroup() + ":" + solution.getName() + ":" + solution.getVersion() + "...");
		try {
			JsonNode response = client.send(createHIPSRequest(mapper, actionName, actionArgs) + "\n");
			//TODO handle response if there is one
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	private void launchSolution(HIPSLaunchRequestEvent event) {
		new Thread(() -> launchSolution(event.getSolution())).start();
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

}
