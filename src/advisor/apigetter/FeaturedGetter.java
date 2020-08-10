package advisor.apigetter;

import advisor.AdvisorException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public class FeaturedGetter extends SpotifyApiGetter {
    private static final String ADDRESS = "/featured-playlists";

    public FeaturedGetter(String resource, String accessToken) {
        super(resource, accessToken);
    }

    @Override
    public List<String> execute() throws AdvisorException {
        get(ADDRESS);
        return entries;
    }

    @Override
    protected void processNewReleaseJson(String responseBody) {
        JsonObject object = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject playlists = object.getAsJsonObject("playlists");
        JsonArray items = playlists.getAsJsonArray("items");
        items.forEach(element -> {
            String name = element.getAsJsonObject().get("name").getAsString();
            String link = element.getAsJsonObject().get("external_urls").getAsJsonObject()
                    .get("spotify").getAsString();
            entries.add(name + "\n" + link);
        });
    }
}
