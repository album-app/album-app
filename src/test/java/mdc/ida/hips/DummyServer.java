package mdc.ida.hips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DummyServer {
	private ServerSocket serverSocket;

	public static DummyServer launch(int port) throws IOException {
		DummyServer server = new DummyServer();
		server.start(port);
		return server;
	}

	private void start(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		new Thread(() -> {
			try {
				while (true) {
					Socket socket = serverSocket.accept();
					(new ClientHandler(socket)).start();
				}
			} catch (IOException e) {
				System.err.println(e.toString());
				System.exit(1);
			}
		}).start();
	}

	private static class ClientHandler extends Thread {
		private final Socket clientSocket;
		private BufferedReader in;

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					handleRequest(out, inputLine);
				}

				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void handleRequest(PrintWriter out, String msg) {
		if(msg == null) return;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readTree(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(jsonNode != null) {
			JsonNode action = jsonNode.get("action");
			if(action != null) {
				if(action.asText().equals("get_index")) {
					out.println(getCollection());
				}
				if(action.asText().equals("launch_hips")) {
					out.println("");
				}
			}
		}
		out.flush();
	}

	public static String getCollection() {
		return "{\"ida-mdc\": {\"avatar-ghost\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/avatar_ghost.py\"}}, \"imagej-display\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/imagej_display.py\", \"args\": [{\"name\": \"name\", \"type\": \"string\", \"description\": \"Image name for display\"}, {\"name\": \"file\", \"type\": \"file\", \"description\": \"Image file to display\"}]}}, \"imagej-cca\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/imagej_cca.py\"}}, \"stardist-labeling\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/stardist_labeling.py\"}}, \"napari-display\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/napari_display.py\"}}, \"app-napari\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/app_napari.py\"}}, \"blender\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/blender.py\"}}, \"sciview\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/sciview.py\"}}, \"sciview-volume-rendering\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/sciview_volume_rendering.py\"}}, \"sciview_labels_to_mesh\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/sciview_labels_to_mesh.py\"}}, \"otsu_segmentation\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/otsu-segmentation.py\"}}, \"blender-render\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/blender_render.py\"}}, \"blender-import-meshes\": {\"0.1.0\": {\"file\": \"/home/random/Development/hips/repos/hips-catalog/solutions/blender_import_meshes.py\"}}}}";
	}

	public void dispose() throws IOException {
		serverSocket.close();
	}
}
