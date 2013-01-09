package world;
import java.util.Comparator;

public class WorldAdventure {

	protected String name;
	protected String imagefile;

    protected int players;
    protected int difficulty;
    protected int days;

	public WorldAdventure(String name) {
		super();
		this.name = name;
	}


	public void setImage(String img) {
		this.imagefile = img;
	}

	public String getImage() {
		return this.imagefile;
	}

	public String getName() {
		return this.name;
	}

    public int getPlayers() {
        return players;
    }
    
    public boolean isMultiplayer() {
    	if(players > 1)
    		return true;
    	else
    		return false;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getDays() {
        return days;
    }

    public static final Comparator<WorldAdventure> BY_NAME = new Comparator<WorldAdventure>() {
		@Override
        public int compare(WorldAdventure a1, WorldAdventure a2) {
			return a1.name.compareTo(a2.name);
		}
	};
}