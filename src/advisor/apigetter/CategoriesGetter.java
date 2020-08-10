package advisor.apigetter;

import advisor.AdvisorException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesGetter extends SpotifyApiGetter {
    private static final String ADDRESS = "/categories";
    private final Map<String, String> map = new HashMap<>();

    public CategoriesGetter(String resource, String accessToken) {
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
        JsonObject categories = jsonObject.getAsJsonObject("categories");
        JsonArray items = categories.getAsJsonArray("items");
        items.forEach(element -> {
            String category = element.getAsJsonObject().get("name").getAsString();
            map.put(category, element.getAsJsonObject().get("id").getAsString());
            String cap = category.substring(0, 1).toUpperCase() + category.substring(1);
            entries.add(cap);
        });
    }

    public String getId(String name) {
        get(ADDRESS);
        return map.get(name);
    }
}
