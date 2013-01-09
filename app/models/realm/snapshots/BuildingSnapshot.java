package models.realm.snapshots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.adventures.AdventureComment;
import models.players.Player;
import models.realm.Building;

import play.data.validation.Required;
import play.db.jpa.Model;
import world.World;
import world.WorldBuilding;
import world.WorldGood;

@Entity
public class BuildingSnapshot extends Model {
	
	@ManyToOne
	public RealmSnapshot snapshot;
	
	public Long buildingId;
	
	@Required
	public String name;

    @Required
	public int level;

    @Required
	public int productionTime;
    
    public boolean enabled = true;
	public boolean boosted = false;
    public boolean simulated = false;

    public int depositQuantity = 0;
    
    public BuildingSnapshot(Building b, RealmSnapshot snapshot) {
    	this.buildingId = b.id;
    	this.snapshot = snapshot;
    	this.name = b.name;
    	this.level = b.level;
    	this.productionTime = b.productionTime;
    	this.enabled = b.enabled;
    	this.boosted = b.boosted;
    	this.simulated = b.simulated;
    }
    
    /**
     * Returns a list of BuildingSnapshot objects for the given building.
     * Returns <code>null</code> if there's no available snapshot for the given building.
     * @param buildingId
     * @return
     */
    public static List<BuildingSnapshot> getBuildingSnapshots(Long buildingId) {
    	return BuildingSnapshot.find("byBuildingId", buildingId).fetch();
    }
    
    /**
     * Returns a list of the goods needed by this building.
     * @return
     */
	public ArrayList<WorldGood> getNeeds() {
        Player p = this.snapshot.player;
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

	/**
	 * Returns a list of the goods produced by this building.
	 * @return
	 */
	public ArrayList<WorldGood> getProds() {
        Player p = this.snapshot.player;
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
     * Returns a hashmap representing the production time of the building.
     * Key "m" for minutes ;
     * Key "s" for seconds.
     * e.g. : 
     *   <code>
     *   	BuildingSnapshot b = new BuildingSnapshot();
     *   	b.productionTime = 3605;
     *   	String s = b.formatProductionTime("m") + " minutes" + b.formatProductionTime("s") + " seconds".
     *   </code>
     *   This would give <code>60 minutes 5 seconds</code>.
     *   Note: this is the same method as in the {@link Building} class.
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
	
	/**
	 * Sorts a list of BuildingSnapshot objects by descending date : from the most recent to the oldest.
	 */
    public static final Comparator<BuildingSnapshot> BY_DATE_DESC = new Comparator<BuildingSnapshot>() {
        @Override
        public int compare(BuildingSnapshot a1, BuildingSnapshot a2) {
            return a2.snapshot.takenAt.compareTo(a1.snapshot.takenAt);
        }
    };
    
    /**
     * Sorts a list of BuildingSnapshot objects by ascending date : from the oldest to the most recent.
     */
    public static final Comparator<BuildingSnapshot> BY_DATE_ASC = new Comparator<BuildingSnapshot>() {
        @Override
        public int compare(BuildingSnapshot a1, BuildingSnapshot a2) {
            return a1.snapshot.takenAt.compareTo(a2.snapshot.takenAt);
        }
    };

}
