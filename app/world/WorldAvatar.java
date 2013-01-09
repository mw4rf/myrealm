package world;

import java.util.Comparator;

public class WorldAvatar {
	
	protected String id;
	protected String image;
    protected String name;
	
    public WorldAvatar(String id, String name) {
        this.name = name;
		this.id = id;
		this.image = "avatars/Avatar" + id + ".png";
	}
	
	public String getImage() {
		return this.image;
	}
	
	public String getId() {
		return this.id;
	}
	
    public String getName() {
        return this.name;
    }

    public static final Comparator<WorldAvatar> BY_ID = new Comparator<WorldAvatar>() {
        @Override
        public int compare(WorldAvatar b1, WorldAvatar b2) {
            return b1.id.compareTo(b2.id);
        }
    };

    public static final Comparator<WorldAvatar> BY_NAME = new Comparator<WorldAvatar>() {
        @Override
        public int compare(WorldAvatar b1, WorldAvatar b2) {
            return b1.name.compareTo(b2.name);
        }
    };

    public static final Comparator<WorldAvatar> BY_NAME_DESC = new Comparator<WorldAvatar>() {
        @Override
        public int compare(WorldAvatar b1, WorldAvatar b2) {
            return b2.name.compareTo(b1.name);
        }
    };

}
