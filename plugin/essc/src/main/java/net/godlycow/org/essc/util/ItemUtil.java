package net.godlycow.org.essc.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ItemUtil {
    private static ItemUtil instance;
    private final Map<String, Material> aliasToMaterial = new HashMap<>();

    private ItemUtil() {
        load();
    }

    public static ItemUtil getInstance() {

        if (instance == null) {
            instance = new ItemUtil();
        }

        return instance;
    }

    private void load() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("items.json"))  {
            if (is == null)
                return;
            JsonObject json = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);


            if (json == null)
                return;

            for (Map.Entry<String, JsonElement> entry :  json.entrySet()) {
                String alias = entry.getKey().toLowerCase();
                JsonElement val = entry.getValue();

                String matName;

                if (val.isJsonObject())
                {
                    matName = val.getAsJsonObject().get("material").getAsString();
                }
                else
                {
                    matName = val.getAsString();
                }
                try {
                    Material mat = Material.valueOf(matName);
                    aliasToMaterial.put(alias, mat);
                } catch (IllegalArgumentException ignored) {}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        aliasToMaterial.clear();
        load();
    }

    public Material resolve(String query) {

        if (query == null || query.isEmpty())
            return null;


        String clean = normalize(query);

        //exact match
        Material direct = aliasToMaterial.get(clean);
        if (direct != null)
            return direct;

        //strip all non-alphanumeric for looser match
        String stripped = clean.replaceAll("[^a-z0-9]", "");
        for ( Map.Entry<String, Material> e : aliasToMaterial.entrySet()) {
            if (e.getKey().replaceAll("[^a-z0-9]", "").equals(stripped)) {
                return e.getValue();

            }
        }

        return null;
    }

    public boolean matchesAny(String query, Material material) {

        if (query == null || query.isEmpty())
            return false;
        String clean = normalize(query);
        String matName = material.name().toLowerCase();

        if (matName.equals(clean))
            return true;


        Material resolved = resolve(query);
        if (resolved != null && resolved == material)
            return true;


        String strippedMat = matName.replaceAll("[^a-z0-9]", "");
        String strippedQuery = clean.replaceAll("[^a-z0-9]", "");

        return strippedMat.contains(strippedQuery) || strippedQuery.contains(strippedMat);
    }

    public int size() {
        return aliasToMaterial.size();
    }

    private String normalize(String input) {
        return input.toLowerCase().trim();
    }
}
