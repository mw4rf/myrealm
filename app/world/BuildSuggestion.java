package world;

public class BuildSuggestion {

    private WorldBuilding building;
    private int quantity;

    private WorldGood lackingGood;
    private WorldGood excessGood;

    public BuildSuggestion(WorldBuilding building, int quantity) {
        super();
        this.building = building;
        this.quantity = quantity;
    }

    public WorldBuilding getBuilding() {
        return building;
    }

    public String getBuildingName() {
        return building.getName();
    }

    public void setBuilding(WorldBuilding building) {
        this.building = building;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public WorldGood getLackingGood() {
        return lackingGood;
    }

    public String getLackingGoodName() {
        return lackingGood.name;
    }

    public double getLackingGoodQuantity() {
        return lackingGood.getRealQuantity();
    }

    public void setLackingGood(WorldGood lackingGood) {
        this.lackingGood = lackingGood;
    }

    public boolean hasLackingGood() {
        if (lackingGood != null)
            return true;
        else
            return false;
    }

    public WorldGood getExcessGood() {
        return excessGood;
    }

    public String getExcessGoodName() {
        return excessGood.name;
    }

    public double getExcessGoodQuantity() {
        return excessGood.getRealQuantity();
    }

    public void setExcessGood(WorldGood lackingGood) {
        this.excessGood = lackingGood;
    }

    public boolean hasExcessGood() {
        if (excessGood != null)
            return true;
        else
            return false;
    }

}
