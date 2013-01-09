package serializers;

import java.lang.reflect.Type;

import world.WorldBuilding;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class WorldBuildingSerializer implements JsonSerializer<WorldBuilding> {
	
	public static WorldBuildingSerializer instance;
	
	private WorldBuildingSerializer() {
		
	}
	
    public static WorldBuildingSerializer get() {
        if (instance == null) {
            instance = new WorldBuildingSerializer();
        }
        return instance;
    }
    
    @Override
    public JsonElement serialize(WorldBuilding wb, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", wb.getName());
        return obj;
    }

}
