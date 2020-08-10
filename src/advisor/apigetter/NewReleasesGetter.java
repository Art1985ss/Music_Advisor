package advisor.apigetter;

import advisor.AdvisorException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class NewReleasesGetter extends SpotifyApiGetter {
    private static final String ADDRESS = "/new-releases";

    public NewReleasesGetter(String resource, String accessToken) {
        super(resource, accessToken);
    }

    @Override
    public List<String> execute() throws AdvisorException {
        get(ADDRESS);
        return entries;
    }

    @Override
    protected void processNewReleaseJson(String responseBody) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonElement albums = jsonObject.get("albums");
        JsonElement items = albums.getAsJsonObject().get("items");
        for (JsonElement e : items.getAsJsonArray()) {
            JsonObject obj = e.getAsJsonObject();
            String albumName = obj.get("name").getAsString();
            List<String> names = new ArrayList<>();
            obj.get("artists").getAsJsonArray().forEach(jsonElement -> {
                String name = jsonElement.getAsJsonObject().get("name").getAsString();
                names.add(name);
            });
            String link = "https://open.spotify.com/album/" + obj.get("id").getAsString();
            entries.add(albumName + "\n" + names + "\n" + link);
        }
    }
}
