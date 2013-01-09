package world;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Duration;
import org.joda.time.Period;

public class WorldBuilding {

	protected String name;
	protected List<WorldGood> goods;
	protected String imagefile;
	protected boolean expires;
	protected int basetime;

	protected double averageProductionTime = 0;

	public WorldBuilding(String name, List<WorldGood> goods) {
		super();
		this.name = name;
		this.goods = goods;
	}

	public List<WorldGood> getGoods() {
		return this.goods;
	}

    public WorldGood getGood(String goodName) {
        Iterator<WorldGood> it = this.goods.iterator();
        while (it.hasNext()) {
            WorldGood wg = it.next();
            if (wg.getName().equals(goodName))
                return wg;
        }
        return null;
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

    public boolean doesExpire() {
    	return this.expires;
    }

    public double getAverageProductionTime() {
    	return this.averageProductionTime;
    }

    public Period getAverageProductionTimeAsPeriod() {
    	Duration dur = new Duration(getBaseProductionTime() * 1000); // milliseconds
    	return dur.toPeriod();
    }
    
    public void setAverageProductionTime(double time) {
    	this.averageProductionTime = time;
    }


	public int getBaseProductionTime() {
		return basetime;
	}

	public int getRawNeed(String goodName) {
		Iterator<WorldGood> it = this.goods.iterator();
		while(it.hasNext()) {
			WorldGood wg = it.next();
			if(wg.getName().equals(goodName) && wg.getQuantity() < 0)
				return wg.getQuantity();
		}
		return 0;
	}

	public boolean doesNeed(String goodName) {
		Iterator<WorldGood> it = this.goods.iterator();
		while(it.hasNext()) {
			WorldGood wg = it.next();
			if(wg.getName().equals(goodName) && wg.getQuantity() < 0)
				return true;
		}
		return false;
	}

	public boolean doesProduce(String goodName) {
		Iterator<WorldGood> it = this.goods.iterator();
		while(it.hasNext()) {
			WorldGood wg = it.next();
			if(wg.getName().equals(goodName) && wg.getQuantity() > 0)
				return true;
		}
		return false;
	}
	
	/**
	 * Search for buildings in the {@link World}, by name, with prediction (i.e. match on the first letters).
	 * @param buildingName
	 * @return {@link List} of {@link WorldBuilding} objects
	 */
	public static List<WorldBuilding> search(String buildingName) {
		List<WorldBuilding> l = new ArrayList<WorldBuilding>();
		for(String name : World.BUILDINGS.keySet()) {
			if(name.toLowerCase().startsWith(buildingName.toLowerCase()))
				l.add(World.BUILDINGS.get(name));
		}
		return l;
	}

	public static final Comparator<WorldBuilding> BY_NAME = new Comparator<WorldBuilding>() {
		@Override
        public int compare(WorldBuilding b1, WorldBuilding b2) {
			return b1.name.compareTo(b2.name);
		}
	};
}