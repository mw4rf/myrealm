package serializers;

import java.lang.reflect.Type;

import models.players.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PlayerJSonSerializer implements JsonSerializer<PlayerWrapper> {

    public static PlayerJSonSerializer instance;

    private PlayerJSonSerializer() {
    }

    public static PlayerJSonSerializer get() {
        if (instance == null) {
            instance = new PlayerJSonSerializer();
        }
        return instance;
    }

    @Override
    public JsonElement serialize(PlayerWrapper player, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", player.name);
        obj.addProperty("id", player.id);
        return obj;
    }
}
