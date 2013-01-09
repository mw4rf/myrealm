package world;

import java.util.Comparator;

public class WorldGood {

	protected String name;
	protected int quantity;
	protected String imagefile;

	protected double realQuantity = 0;
	
	protected double realValue = 0;
	protected double relativeValue = 0;

	public WorldGood(String name, int quantity) {
		super();
		this.name = name;
		this.quantity = quantity;
	}

	public void setImage(String img) {
		this.imagefile = img;
	}

	public String getImage() {
		return this.imagefile;
	}

	public WorldGood(String name, int quantity, double realQuantity) {
		super();
		this.name = name;
		this.quantity = quantity;
		this.realQuantity = realQuantity;
	}

	public String getName() {
		return name;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getRealQuantity() {
        int ix = (int) (this.realQuantity * 100.0); // scale it
        return ix / 100.0;
	}

    public void setRealQuantity(double realQuantity) {
        this.realQuantity = realQuantity;
    }

    public BuildingGoodMap getBuildingMap() {
    	return World.GOODS.get(this.name);
    }

    public double getRealValue() {
		return realValue;
	}

	public void setRealValue(double realValue) {
		this.realValue = realValue;
	}

	public double getRelativeValue() {
		return relativeValue;
	}

	public void setRelativeValue(double relativeValue) {
		this.relativeValue = relativeValue;
	}



	public static final Comparator<WorldGood> BY_NAME = new Comparator<WorldGood>() {
		@Override
        public int compare(WorldGood b1, WorldGood b2) {
			return b1.name.compareTo(b2.name);
		}
	};

	public static final Comparator<WorldGood> BY_QUANTITY = new Comparator<WorldGood>() {
		@Override
        public int compare(WorldGood b1, WorldGood b2) {
			return Integer.valueOf(b1.quantity).compareTo(Integer.valueOf(b2.quantity));
		}
	};

}