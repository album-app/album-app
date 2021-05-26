package mdc.ida.hips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mdc.ida.hips.service.HIPSServerService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HIPSClient {

    @Parameter
    private UIService ui;

    @Parameter
    private HIPSServerService hipsService;

    private Socket socket;
    private int port;

    public HIPSClient(int port) {
        this.port = port;
    }

    private void startSocket() throws IOException {
        String host = "localhost";
        System.out.println("Connecting to " + host + ":" + port);
        socket = new Socket(host, port);
    }

    public JsonNode send(String msg) throws IOException {
        if(socket == null || socket.isClosed()) {
            startSocket();
        }
        System.out.println("sending " + msg);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.write(msg + "\n");
        out.flush();
        String serverMsg = in.readLine();
        System.out.println("Server response: " + serverMsg);
        in.close();
        out.close();
        if(serverMsg == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(serverMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }
}
