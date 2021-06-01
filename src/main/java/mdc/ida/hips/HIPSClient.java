package mdc.ida.hips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

public class HIPSClient {

    private int port;
    private String host;
    private boolean increasePort = false;

    public HIPSClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public JsonNode send(String msg) throws IOException {
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
            send("shutdown");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ServerNotAvailableException extends RuntimeException {
        public ServerNotAvailableException(String message) {
            super(message);
        }
    }
}
