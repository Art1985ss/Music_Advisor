package advisor;

import advisor.apigetter.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiManager extends Thread {
    private static final String CLIENT_ID = "437b8a9d9fd54c28b516d3755062011c";
    private static final String CLIENT_SECRET = "a90b358ce32f475f8c2723f5dec5b99a";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private String accessPoint;
    private String resource = "https://api.spotify.com";
    private HttpServer server = null;
    private HttpResponse<String> response = null;
    private String query;
    private String code;
    private String accessToken = null;
    private String refreshToken = null;

    public ApiManager(String accessPoint, String resource) {
        this.accessPoint = accessPoint;
        this.resource = resource;
    }

    @Override
    public void run() {
        try {
            getCode();
            System.out.println(accessPoint + "/authorize?client_id=" +
                    CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code");
            System.out.println("waiting for code...");
            while (code == null) {
                Thread.sleep(200);
            }
            server.stop(0);
            request();
        } catch (AdvisorException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        processResponse();

    }

    public List<String> getInfo(UserRequest userRequest, String param) throws AdvisorException {
        SpotifyApiGetter spotifyApiGetter = null;
        switch (userRequest) {
            case NEW:
                spotifyApiGetter = new NewReleasesGetter(resource, accessToken);
                break;
            case CATEGORIES:
                spotifyApiGetter = new CategoriesGetter(resource, accessToken);
                break;
            case PLAYLISTS:
                spotifyApiGetter = new PlaylistsGetter(resource, accessToken, param);
                break;
            case FEATURED:
                spotifyApiGetter = new FeaturedGetter(resource, accessToken);
                break;
            default:
        }
        if (spotifyApiGetter == null) {
            throw new AdvisorException("Could not initiate request");
        }
        return spotifyApiGetter.execute();
    }

    public String getResponse() {
        return response.body();
    }

    private void getCode() throws AdvisorException {
        try {
            server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (server == null) {
            throw new AdvisorException("Server could not been created");
        }
        server.createContext("/",
                exchange -> {
                    query = exchange.getRequestURI().getQuery();
                    String answer = processQuery();
                    exchange.sendResponseHeaders(200, answer.length());
                    exchange.getResponseBody().write(answer.getBytes());
                    exchange.getResponseBody().close();
                }
        );
        server.start();
    }

    private String processQuery() {
        if (query == null) {
            return "Not found authorization code. Try again.";
        }
        Matcher matcher = Pattern.compile("code=.+").matcher(query);
        if (matcher.matches()) {
            String[] codeQuery = query.split("=");
            code = codeQuery[1];
            query = "ku";
            return "Got the code. Return back to your program.";
        }
        return "Not found authorization code. Try again.";
    }

    public void request() throws AdvisorException {
        System.out.println("making http request for access_token...");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(accessPoint + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code&" +
                                "code=" + (refreshToken == null ? code : refreshToken) +
                                "&redirect_uri=" + REDIRECT_URI +
                                "&client_id=" + CLIENT_ID +
                                "&client_secret=" + CLIENT_SECRET))
                .build();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new AdvisorException("Could not receive response with access token");
        }
    }

    private void processResponse() {
        String response = this.response.body();
        response = response.replaceAll("\"", "");
        response = response.substring(1, response.length() - 1);
        String[] params = response.split(",");
        for (String param : params) {
            String[] str = param.split(":");
            if ("access_token".equals(str[0])) {
                accessToken = str[1];
                continue;
            }
            if ("refresh_token".equals(str[0])) {
                refreshToken = str[1];
            }
        }
    }


}
