package world;

import java.util.HashMap;

public class BuildingGoodMap {

    protected String goodname;
    protected WorldGood good;
    protected HashMap<String, WorldBuilding> neededBy = new HashMap<String, WorldBuilding>();
    protected HashMap<String, WorldBuilding> producedBy = new HashMap<String, WorldBuilding>();

    protected HashMap<String, Double> needsRatios = new HashMap<String, Double>();

    public BuildingGoodMap(WorldGood good) {
        this.good = good;
        this.goodname = good.name;
    }

    public String getGoodName() {
        return goodname;
    }

    public void setGoodname(String goodname) {
        this.goodname = goodname;
    }

    public WorldGood getGood() {
        return good;
    }

    public void setGood(WorldGood good) {
        this.good = good;
    }

    public HashMap<String, WorldBuilding> getNeededBy() {
        return neededBy;
    }

    public void setNeededBy(HashMap<String, WorldBuilding> neededBy) {
        this.neededBy = neededBy;
    }

    public HashMap<String, WorldBuilding> getProducedBy() {
        return producedBy;
    }

    public void setProducedBy(HashMap<String, WorldBuilding> producedBy) {
        this.producedBy = producedBy;
    }

	/**
	 * @return the needsRatios
	 */
	public HashMap<String, Double> getNeedsRatios() {
		return needsRatios;
	}

	/**
	 * @param needsRatios the needsRatios to set
	 */
	public void setNeedsRatios(HashMap<String, Double> needsRatios) {
		this.needsRatios = needsRatios;
	}



}
