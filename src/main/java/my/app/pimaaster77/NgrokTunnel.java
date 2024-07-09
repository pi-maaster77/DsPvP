package my.app.pimaaster77;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NgrokTunnel {

    private Process ngrokProcess;

    public String getNgrokTunnelUrl(int port) {
        String url = null;
        try {
            // Start ngrok process
            ngrokProcess = new ProcessBuilder("ngrok", "tcp", String.valueOf(port)).start();

            // Wait for ngrok to start and establish the tunnel
            Thread.sleep(10000); // Aumentado a 10 segundos para dar más tiempo a ngrok

            // Fetch the tunnel URL from ngrok API
            url = fetchNgrokTunnelUrl();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    private String fetchNgrokTunnelUrl() {
        String apiUrl = "http://localhost:4040/api/tunnels";
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the response to get the public URL
            String jsonResponse = response.toString();
            int startIndex = jsonResponse.indexOf("public_url\":\"tcp://");
            if (startIndex != -1) {
                startIndex += "public_url\":\"tcp://".length();
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                return jsonResponse.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void closeNgrokTunnel() {
        if (ngrokProcess != null) {
            ngrokProcess.destroy();
        }
    }
}
