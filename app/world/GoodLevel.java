package world;

import java.util.Comparator;

import models.realm.Building;

public class GoodLevel {
	
	private String goodName = "";
	private int totalLevels = 0;
	private int boostedLevels = 0;
	private int stoppedLevels = 0;
	private int simulatedLevels = 0;
	
	public GoodLevel() {
	}
	
	public GoodLevel(String goodName) {
		super();
		this.goodName = goodName;
	}
	public int getTotalLevels() {
		return totalLevels;
	}
	public void setTotalLevels(int totalLevels) {
		this.totalLevels = totalLevels;
	}
	public int getBoostedLevels() {
		return boostedLevels;
	}
	public void setBoostedLevels(int boostedLevels) {
		this.boostedLevels = boostedLevels;
	}
	public int getStoppedLevels() {
		return stoppedLevels;
	}
	public void setStoppedLevels(int stoppedLevels) {
		this.stoppedLevels = stoppedLevels;
	}
	public int getSimulatedLevels() {
		return simulatedLevels;
	}
	public void setSimulatedLevels(int simulatedLevels) {
		this.simulatedLevels = simulatedLevels;
	}

	public String getGoodName() {
		return goodName;
	}

	public void setGoodName(String goodName) {
		this.goodName = goodName;
	}
	
	public String getGoodImage() {
		return World.GOODS.get(goodName).getGood().getImage();
	}
	
	public void addLevel() {
		this.totalLevels++;
	}
	
	public void addBoostedLevel() {
		this.boostedLevels++;
	}
	
	public void addSimulatedLevel() {
		this.simulatedLevels++;
	}
	
	public void addStoppedLevel() {
		this.stoppedLevels++;
	}
	
	/**
	 * Add to this good all the levels of production given by the building passed as argument.
	 * Levels depend on building raw production (i.e. 1 or more units by cycle) and building level.
	 * This methods doesn't return anything, but set/update the following instance variables :
	 * <br /><i>totalLevels</i> : total levels for that good (from the given building and other buildings)
	 * <br /><i>simulatedLevels</i> : total levels given by simulated buildings
	 * <br /><i>stoppedLevels</i> : total levels lost by stopped buildings
	 * <br /><i>boostedLevels</i> : total levels gained by boosted buildings
	 * @param b
	 */
	public void addLevels(Building b) {
		// Get what the building produces
		WorldGood wg = World.BUILDINGS.get(b.name).getGood(this.goodName);
		// Get quantity produced -- should be 1 for most buildings but not for all (i.e. beer)
		int q = wg.getQuantity();
		// Get building real production by cycle
		int p = q * b.level;
		
		totalLevels = totalLevels + p;
		if(b.simulated)
			simulatedLevels = simulatedLevels + p;
		if(!b.enabled)
			stoppedLevels = stoppedLevels + p;
		if(b.boosted)
			boostedLevels = boostedLevels + p;
	}
	
	public static final Comparator<GoodLevel> BY_NAME_ASC = new Comparator<GoodLevel>() {
		@Override
        public int compare(GoodLevel b1, GoodLevel b2) {
			return b1.goodName.compareTo(b2.goodName);
		}
	};
	
	public static final Comparator<GoodLevel> BY_NAME_DESC = new Comparator<GoodLevel>() {
		@Override
        public int compare(GoodLevel b1, GoodLevel b2) {
			return b2.goodName.compareTo(b1.goodName);
		}
	};

}
