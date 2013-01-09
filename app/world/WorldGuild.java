package world;

import java.util.Comparator;

public class WorldGuild {

    protected String name = "";
    protected String id = "";
    protected String image = "";

    public WorldGuild(String id, String name) {
        this.name = name;
        this.id = id;
        this.image = "guilds/banner" + id + ".png";
    }

    public String getImage() {
        return this.image;
    }

    public static final Comparator<WorldGuild> BY_NAME = new Comparator<WorldGuild>() {
		@Override
        public int compare(WorldGuild b1, WorldGuild b2) {
			return b1.name.compareTo(b2.name);
		}
	};

}
