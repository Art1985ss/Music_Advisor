package advisor.apigetter;

import advisor.AdvisorException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public abstract class SpotifyApiGetter {
    private String resource = "https://api.spotify.com";
    private HttpResponse<String> apiResponse = null;
    private final HttpClient client;
    private String accessToken;
    protected List<String> entries;

    public SpotifyApiGetter(String resource, String accessToken) {
        this.resource = resource;
        this.accessToken = accessToken;
        client = HttpClient.newBuilder().build();
        entries = new ArrayList<>();
    }

    public abstract List<String> execute() throws AdvisorException;

    protected abstract void processNewReleaseJson(String responseBody);

    protected void get(String requestAddress) throws AdvisorException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(resource + "/v1/browse" + requestAddress))
                .GET()
                .build();
        try {
            apiResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            checkIfError(apiResponse.body());
            processNewReleaseJson(apiResponse.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new AdvisorException("Could not receive response");
        }
    }

    private void checkIfError(String response) throws AdvisorException {

        try {
            JsonObject object = JsonParser.parseString(response).getAsJsonObject();
            if (object.has("error")) {
                String errorMessage = object.get("error").getAsJsonObject()
                        .get("message").getAsString();
                throw new AdvisorException(errorMessage);
            }
        } catch (JsonSyntaxException | AdvisorException | IllegalStateException e) {
            throw new AdvisorException("Wrong json format : \n" + response);
        }
    }
}
