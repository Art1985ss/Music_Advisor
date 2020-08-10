package advisor.apigetter;

import advisor.AdvisorException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;


public class PlaylistsGetter extends SpotifyApiGetter {
    private static final String ADDRESS_CATEGORIES = "/categories/";
    private static final String ADDRESS = "/playlists";
    private final String categoryId;

    public PlaylistsGetter(String resource, String accessToken, String categoryName) {
        super(resource, accessToken);
        CategoriesGetter categoriesGetter = new CategoriesGetter(resource, accessToken);
        this.categoryId = categoriesGetter.getId(categoryName.trim());
    }

    @Override
    public List<String> execute() throws AdvisorException {
        if (categoryId == null) {
            throw new AdvisorException("Unknown category name.");
        }
        get(ADDRESS_CATEGORIES + categoryId + ADDRESS);
        return entries;
    }

    @Override
    protected void processNewReleaseJson(String responseBody) {
        System.out.println(responseBody);
        JsonObject object = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject playlists = object.getAsJsonObject("playlists");
        JsonArray items = playlists.getAsJsonArray("items");
        items.forEach(element -> {
            String link = element.getAsJsonObject().get("external_urls").getAsJsonObject()
                    .get("spotify").getAsString();
            String name = element.getAsJsonObject().get("name").getAsString();
            entries.add(name + "\n" + link);
        });
    }
}
