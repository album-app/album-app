package mdc.ida.hips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

public class HIPSClient {

    private int port;
    private String host;
    private boolean increasePort = false;

    public HIPSClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public JsonNode send(String msg) throws IOException {
        System.out.println("HIPSClient: sending " + msg);
        JsonNode jsonNode;
        InputStream serverMsg = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet getRequest = new HttpGet(host + ":" + port + msg);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = null;

            response = httpClient.execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            serverMsg = response.getEntity().getContent();
            if (statusCode != 200) {
                serverMsg.close();
                httpClient.close();
                //TODO user logger
                System.out.println("Failed : HTTP error code : "
                        + statusCode);
                return null;
            }

            if(serverMsg == null) return null;
            if(msg.equals("/shutdown")) {
                serverMsg.close();
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            jsonNode = null;
            try {
                jsonNode = mapper.readTree(serverMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            if (serverMsg != null) {
                serverMsg.close();
            }
        }
        System.out.println("Server response: " + jsonNode);
        return jsonNode;
    }

    public void dispose() {
        try {
            send(createHIPSRequest(new ObjectMapper(), "shutdown"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createHIPSRequest(ObjectMapper mapper, String actionName) {
        return createHIPSRequest(mapper, actionName, mapper.createObjectNode());
    }

    public String createHIPSRequest(ObjectMapper mapper, String actionName, ObjectNode actionArgs) {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", actionName);
        request.set("args", actionArgs);
        StringBuilder s = new StringBuilder("/" + actionName);
        if(actionArgs != null && actionArgs.size() > 0) {
            AtomicReference<Boolean> first = new AtomicReference<>(true);
            actionArgs.fieldNames().forEachRemaining(name -> {
                s.append(first.get() ? "?" : "&");
                s.append(name);
                s.append("=");
                s.append(actionArgs.get(name).asText());
                first.set(false);
            });
        }
        return s.toString();
    }

    public static class ServerNotAvailableException extends RuntimeException {
        public ServerNotAvailableException(String message) {
            super(message);
        }
    }
}
