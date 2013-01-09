package models.realm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import models.players.Player;
import models.players.PlayerTimer;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import world.World;
import world.WorldBuilding;
import world.WorldGood;

@Entity
public class Building extends Model {

    @Required
	public String name;

    @Required
	public int level;

    @Required
	public int productionTime;

    public int area = 0;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String description;

	public boolean enabled = true;
	public boolean boosted = false;
    public boolean simulated = false;

    public int depositQuantity = 0;
    public Date depositInit = new Date();

    public Date builtAt = new Date();

    @OneToOne(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public PlayerTimer timer;
    
    @ManyToMany
    public List<BuildingsGroup> groups = new ArrayList<BuildingsGroup>();

	public Building(String name, int level, int productionTime) {
		super();
		this.name = name;
		this.level = level;
		this.productionTime = productionTime;
		this.builtAt = new Date();
	}

    @Override
    public String toString() {
        return this.name;
    }

    public static List<Building> find(String name) {
        return Building.find("byName", name).fetch();
    }

    public int getEfficiency() {
    	// This building real production time
    	double pt = (double) this.productionTime;
    	// This building base production time (not reachable)
    	double op = (double) World.BUILDINGS.get(this.name).getBaseProductionTime();
    	// Efficiency in %
    	double res = (op / pt) * 100;
    	// If > 100 the building's production time is erroneous, so return -1
    	if(res > 100)
    		return -1;
    	// Scale it to integer
        return (int) res;
    }

    public static double calcAverageProductionTime(String buildingName) {
        //List<Building> bs = Building.find(buildingName);
    	List<Building> bs = Building.find("byName", buildingName).fetch(200); // not too much please, this is very time consuming !
        int count = 0;
        int time = 0;
        Iterator<Building> it = bs.iterator();
        while (it.hasNext()) {
            Building nextb = it.next();
            if (nextb.productionTime > 1) { // no 0 sec, and no 1 sec (no kidding !)
                count++;
                time += nextb.productionTime;
            }
        }
        if (count == 0) // avoid dividing by zero
            return 0;
        return time / count;
    }

    public static double getAverageProductionTime(String buildingName) {
    	return World.BUILDINGS.get(buildingName).getAverageProductionTime();
    }

    /**
     * Returns a hashmap representing the production time of the building.
     * Key "m" for minutes ;
     * Key "s" for seconds.
     * e.g. : 
     *   <code>
     *   	Building b = new Building();
     *   	b.productionTime = 3605;
     *   	String s = b.formatProductionTime("m") + " minutes" + b.formatProductionTime("s") + " seconds".
     *   </code>
     *   This would give <code>60 minutes 5 seconds</code>.
     * @return HashMap<String,Integer>
     */
	public HashMap<String,Integer> formatProductionTime() {
		if(this.productionTime == 0)
			this.productionTime = 1;
		int minutes = this.productionTime / 60;
		int seconds = this.productionTime % 60;
		HashMap<String,Integer> hm = new HashMap();
		hm.put("s", seconds);
		hm.put("m", minutes);
		return hm;
	}

	public void setProductionTime(int minutes, int seconds) {
		this.productionTime = minutes * 60 + seconds;
		if(this.productionTime == 0)
			this.productionTime = 1;
        this.save();
	}

	public String getImage() {
		return World.BUILDINGS.get(this.name).getImage();
	}

	public boolean doesExpire() {
		try {
			return World.BUILDINGS.get(this.name).doesExpire();
		} catch(Exception e) {
			return false;
		}
	}

	public int calcDepositQuantity() {
		// How many seconds between now and the day we set the initial deposit quantity ?
		DateTime now = new DateTime();
		DateTime init = new DateTime(this.depositInit);
		Duration dur = new Duration(init, now);
		long secs = dur.getMillis() / 1000;
		// How many cycles in that time ?
		int cycles = (int) secs / this.productionTime;
		// Then, how much resources spent ?
		int spent = (int) this.level * cycles;
		// And finally the current quantity...
		int result = this.depositQuantity - spent;
		if(result < 0)
			return 0;
		else
			return result;
	}

	public void setDepositQuantity(int depositQuantity) {
		this.depositQuantity = depositQuantity;
		this.depositInit = new Date();
		this.save();
	}

	public double getNeedByCycle(String playerName, String goodName) {
        Player p = Player.find(playerName);
		if(!this.enabled)
			return 0; // building disabled : no need
		WorldBuilding bmodel = World.BUILDINGS.get(this.name);
		Iterator<WorldGood> it = bmodel.getGoods().iterator();
		double res = 0;
		while(it.hasNext()) {
			WorldGood gmodel = it.next();
			if(!gmodel.getName().equals(goodName))
				continue;
			else if(gmodel.getQuantity() < 0) {
                double quantity = gmodel.getQuantity() * this.level;
				double realQuantity = quantity * p.cycleTime / this.productionTime;
				res += realQuantity;
			}
		}
        int ix = (int) (res * 100.0); // scale it
        res = ix / 100.0; // 2 digits after .
		return res;
	}

	public ArrayList<WorldGood> getNeeds(String playerName) {
        Player p = Player.find(playerName);
		ArrayList<WorldGood> res = new ArrayList();
		if(!this.enabled)
			return res; // building disabled : no need
		WorldBuilding bmodel = World.BUILDINGS.get(this.name);
		Iterator<WorldGood> it = bmodel.getGoods().iterator();
		while(it.hasNext()) {
			WorldGood gmodel = it.next();
			if(gmodel.getQuantity() < 0) {
                double quantity = gmodel.getQuantity() * this.level;
				double realQuantity = quantity * p.cycleTime / this.productionTime;
                int ix = (int) (realQuantity * 100.0); // scale it
                realQuantity = ix / 100.0; // 2 digits after .
                WorldGood rwg = new WorldGood(gmodel.getName(), (int) quantity, realQuantity);
				rwg.setImage(gmodel.getImage());
				res.add(rwg);
			}
		}
		return res;
	}

	public ArrayList<WorldGood> getProds(String playerName) {
        Player p = Player.find(playerName);
		ArrayList<WorldGood> res = new ArrayList();
		if(!this.enabled)
			return res; // building disabled : no production
		WorldBuilding bmodel = World.BUILDINGS.get(this.name);
		Iterator<WorldGood> it = bmodel.getGoods().iterator();
		while(it.hasNext()) {
			WorldGood gmodel = it.next();
			if(gmodel.getQuantity() > 0) {
                double quantity = gmodel.getQuantity() * this.level;
                double realQuantity = (quantity * p.cycleTime) / this.productionTime;
				if(this.boosted)
                    realQuantity = realQuantity * 2; // building boosted: double quantity
                int ix = (int) (realQuantity * 100.0); // scale it
                realQuantity = ix / 100.0; // 2 digits after .
                WorldGood rwg = new WorldGood(gmodel.getName(), (int) quantity, realQuantity);
				rwg.setImage(gmodel.getImage());
				res.add(rwg);
			}
		}
		return res;
	}
	
	/**
	 * Execute an action on this building. Those actions are the global actions the user can trigger
	 * on all buildings or all selected buildings of a buildings list.
	 * @param action
	 * @param option
	 */
	public void executeAction(Player player, String action, String option) {
		//Switch actions
		if(action.equals("boost"))
			this.boosted = true;
		else if(action.equals("unboost"))
			this.boosted = false;
		else if(action.equals("stop"))
			this.enabled = false;
		else if(action.equals("start"))
			this.enabled = true;
		else if(action.equals("simulate"))
			this.simulated = true;
		else if(action.equals("duplicate-simulated"))
			player.duplicateBuilding(this.id, this.simulated);
		else if(action.equals("validate-simulated"))
			this.simulated = false;
		else if(action.equals("up-level")) {
			if(this.level < 5)
				this.level++;
		}
		else if(action.equals("down-level")) { 
			if(this.level > 1)
				this.level--;
		}
		else if(action.equals("set-level")) {
			if(new Integer(option) > 0 && new Integer(option) <= 5)
				this.level = new Integer(option);
		}
		else if(action.equals("set-area")) {
			if(new Integer(option) > 0 && new Integer(option) <= 9)
				this.area = new Integer(option);
		}
		else if(action.equals("add-to-group")) {
			BuildingsGroup bg = BuildingsGroup.find(option);
			if(bg != null && !this.groups.contains(bg)) {
				bg.addBuilding(this);
				bg.save();
			}
		}
		else if(action.equals("remove-from-group")) {
			BuildingsGroup bg = BuildingsGroup.find(option);
			if(bg != null && this.groups.contains(bg)) {
				bg.removeBuilding(this);
				bg.save();
			}
		}
	}

	public static List<Building> sortList(List<Building> buildings, String sorting, String order) {
		if(sorting == null || order == null)
        	Collections.sort(buildings, Building.BY_ID);
        else {
            if(sorting.equalsIgnoreCase("NAME"))
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_NAME_DESC);
            	else
            		Collections.sort(buildings, Building.BY_NAME);
            else if(sorting.equalsIgnoreCase("LEVEL"))
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_LEVEL_DESC);
            	else
            		Collections.sort(buildings, Building.BY_LEVEL);
            else if(sorting.equalsIgnoreCase("PRODUCTIONTIME"))
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_PRODUCTION_TIME_DESC);
            	else
            		Collections.sort(buildings, Building.BY_PRODUCTION_TIME);
            else if(sorting.equalsIgnoreCase("AREA"))
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_AREA_DESC);
            	else
            		Collections.sort(buildings, Building.BY_AREA);
            else if(sorting.equalsIgnoreCase("DEPOSIT"))
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_DEPOSIT_DESC);
            	else
            		Collections.sort(buildings, Building.BY_DEPOSIT);
            else if(sorting.equalsIgnoreCase("EFFICIENCY"))
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_EFFICIENCY_DESC);
            	else
            		Collections.sort(buildings, Building.BY_EFFICIENCY);
            else
            	if(order.equalsIgnoreCase("DESC"))
            		Collections.sort(buildings, Building.BY_ID_DESC);
            	else
            		Collections.sort(buildings, Building.BY_ID);
        }
		return buildings;
	}

	public static final Comparator<Building> BY_NAME = new Comparator<Building>() {
		@Override
        public int compare(Building b1, Building b2) {
			return b1.name.compareTo(b2.name);
        }
	};

	public static final Comparator<Building> BY_NAME_DESC = new Comparator<Building>() {
		@Override
        public int compare(Building b1, Building b2) {
			return b2.name.compareTo(b1.name);
        }
	};

    public static final Comparator<Building> BY_ID = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return b1.id.compareTo(b2.id);
        }
    };

    public static final Comparator<Building> BY_ID_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return b2.id.compareTo(b1.id);
        }
    };

    public static final Comparator<Building> BY_LEVEL = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b1.level).compareTo(b2.level);
        }
    };

    public static final Comparator<Building> BY_LEVEL_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b2.level).compareTo(b1.level);
        }
    };

    public static final Comparator<Building> BY_AREA = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b1.area).compareTo(b2.area);
        }
    };

    public static final Comparator<Building> BY_AREA_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b2.area).compareTo(b1.area);
        }
    };

    public static final Comparator<Building> BY_PRODUCTION_TIME = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b1.productionTime).compareTo(b2.productionTime);
        }
    };

    public static final Comparator<Building> BY_PRODUCTION_TIME_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b2.productionTime).compareTo(b1.productionTime);
        }
    };

    public static final Comparator<Building> BY_DEPOSIT = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b1.depositQuantity).compareTo(b2.depositQuantity);
        }
    };

    public static final Comparator<Building> BY_DEPOSIT_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b2.depositQuantity).compareTo(b1.depositQuantity);
        }
    };

    public static final Comparator<Building> BY_EFFICIENCY = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b1.getEfficiency()).compareTo(b2.getEfficiency());
        }
    };

    public static final Comparator<Building> BY_EFFICIENCY_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return new Integer(b2.getEfficiency()).compareTo(b1.getEfficiency());
        }
    };
    
    public static final Comparator<Building> BY_DATE = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return b1.builtAt.compareTo(b2.builtAt);
        }
    };

    public static final Comparator<Building> BY_DATE_DESC = new Comparator<Building>() {
        @Override
        public int compare(Building b1, Building b2) {
            return b2.builtAt.compareTo(b1.builtAt);
        }
    };

}
