package models.realm.snapshots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import models.players.Player;
import models.realm.Building;

import play.db.jpa.JPA;
import play.db.jpa.Model;
import world.WorldGood;

@Entity
public class RealmSnapshot extends Model {

	@ManyToOne
	public Player player;
	
	public Date takenAt;
	public boolean locked = false;
	
	public Integer buildingsCount = 0;
	public Double buildingsAverageLevel = (double) 0;
	public Integer buildingsCountEnabled = 0;
	public Integer buildingsCountStopped = 0;
	public Integer buildingsCountBoosted = 0;
	public Integer buildingsCountSimulated = 0;
	
	public Integer goodsCount = 0;
	public Integer goodsProducedCount = 0;
	public Integer goodsNeededCount = 0;
	public Integer goodsExcessCount = 0;
	public Integer goodsDeficitCount = 0;
	
	@OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval=true)
	public List<BuildingSnapshot> buildings = new ArrayList<BuildingSnapshot>();
	
	@OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval=true)
	public List<GoodSnapshot> goods = new ArrayList<GoodSnapshot>();
	
	public RealmSnapshot(Player p) {
		this.player = p;
		this.takenAt = new Date();
		this.buildingsCount = this.player.buildings.size();
		// We need to save here, in order to be able to add relations
		// if we don't save, we'll have a org.hibernate.TransientObjectException
		// (object references an unsaved transient instance) on GoodSnapshot.save
		this.save();
		// Build snapshot
		this.buildSnapshot();
		// save
		this.save();
	}
	
	/**
	 * Build the snapshot ; called by the constructor (and should never be called directly).
	 * This method build the realm snapshot, saving buildings and goods in the database. 
	 */
	private void buildSnapshot() {
		// Build buildings snapshots
		double levelsum = (double) 0;
		Iterator<Building> bit = this.player.buildings.iterator();
		while(bit.hasNext()) {
				Building b = bit.next();
				// Set RealmSnapshot properties
				levelsum += b.level;
				if(!b.enabled)
					this.buildingsCountStopped++;
				else
					this.buildingsCountEnabled++;
				if(b.boosted)
					this.buildingsCountBoosted++;
				if(b.simulated)
					this.buildingsCountSimulated++;
				// Add ned BuildingSnapshot
				BuildingSnapshot bs = new BuildingSnapshot(b, this).save();
				this.buildings.add(bs);
				// Save RealmSnapshot
				this.save();
		}
		this.buildingsAverageLevel = levelsum / this.player.buildings.size();
		this.save();
		// Build goods snapshots
		// First, get the data
		List<WorldGood> ratios = this.player.getGoodsRatio();
		HashMap<String,WorldGood> pratios = new HashMap<String, WorldGood>();
		Iterator<WorldGood> pit = this.player.getProducedGoods().iterator();
		while(pit.hasNext()) {
			WorldGood wg = pit.next();
			pratios.put(wg.getName(), wg);
		}
		HashMap<String,WorldGood> nratios = new HashMap<String, WorldGood>();
		Iterator<WorldGood> nit = this.player.getNeededGoods().iterator();
		while(nit.hasNext()) {
			WorldGood wg = nit.next();
			nratios.put(wg.getName(), wg);
		}
		// Second, fill the map
		Iterator<WorldGood> it = ratios.iterator();
		while(it.hasNext()) {
				// Get data
				WorldGood rg = it.next();
				WorldGood pg = pratios.get(rg.getName());
				WorldGood ng = nratios.get(rg.getName());
				// Update stats
				if(rg.getRealQuantity() < 0)
					this.goodsDeficitCount++;
				else
					this.goodsExcessCount++;
				// Create snapshot
				GoodSnapshot snap = new GoodSnapshot(this);
				snap.name = rg.getName();
				snap.snapshot = this;
				snap.quantity = rg.getRealQuantity();
				if(pg != null)
					snap.produced = pg.getRealQuantity();
				if(ng != null)
					snap.needed =   ng.getRealQuantity();
				snap.save();
				this.goods.add(snap);
				this.save();
		}
		// Save stats
		this.goodsCount = this.goods.size();
		this.goodsProducedCount = pratios.size();
		this.goodsNeededCount = nratios.size();
		this.save();
	}
	
	/**
	 * Count boosted buildings in this snapshot.
	 * @return {@link Integer} number of <b>boosted</b> buildings in the realm.
	 */
	@Deprecated
	public int countBoostedBuildings() {
		int count = 0;
		Iterator<BuildingSnapshot> it = this.buildings.iterator();
		while(it.hasNext()) {
			BuildingSnapshot bs = it.next();
			if(bs.boosted)
				count++;
		}
		return count;
	}
	
	/**
	 * Count stopped/disabled buildings in this snapshot.
	 * @return {@link Integer} number of <b>stopped</b> buildings in the realm.
	 */
	@Deprecated
	public int countStoppedBuildings() {
		int count = 0;
		Iterator<BuildingSnapshot> it = this.buildings.iterator();
		while(it.hasNext()) {
			BuildingSnapshot bs = it.next();
			if(!bs.enabled)
				count++;
		}
		return count;
	}
	
	/**
	 * Count enabled/working buildings in this snapshot.
	 * @return {@link Integer} number of <b>enabled</b> buildings in the realm.
	 */
	@Deprecated
	public int countEnabledBuildings() {
		return this.buildingsCount - this.countStoppedBuildings();
	}
	
	/**
	 * Count stopped/disabled buildings in this snapshot.
	 * @return {@link Integer} number of <b>simulated</b> buildings in the realm.
	 */
	@Deprecated
	public int countSimulatedBuildings() {
		int count = 0;
		Iterator<BuildingSnapshot> it = this.buildings.iterator();
		while(it.hasNext()) {
			BuildingSnapshot bs = it.next();
			if(bs.simulated)
				count++;
		}
		return count;
	}
	
	/**
	 * Loop through the buildings in the snapshot, and increment the number of:
	 * <br /><b>enabled</b>, <b>stopped</b>, <b>boosted</b>, <b>simulated</b> buildings.
	 * @return {@link HashMap} with the following keys: <i>enabled</i>, <i>stopped</i>, <i>boosted</i>, <i>simulated</i> ; and, for each key, an Integer as value.
	 */
	public HashMap<String,Integer> countBuildingProperties() {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		map.put("enabled", 0);
		map.put("stopped", 0);
		map.put("boosted", 0);
		map.put("simulated", 0);
		Iterator<BuildingSnapshot> it = this.buildings.iterator();
		while(it.hasNext()) {
			BuildingSnapshot bs = it.next();
			if(bs.enabled)
				map.put("enabled", map.get("enabled") + 1);
			else
				map.put("stopped", map.get("stopped") + 1);
			if(bs.boosted)
				map.put("boosted", map.get("boosted") + 1);
			if(bs.simulated)
				map.put("simulated", map.get("simulated") + 1);
		}
		return map;
	}
	
	/**
	 * Returns the GoodsSnapshot instance for the given good
	 * @param goodName
	 * @return {@link GoodSnapshot} or null
	 */
	public GoodSnapshot getGoodSnapshot(String goodName) {
		Iterator<GoodSnapshot> git = this.goods.iterator();
		while(git.hasNext()) {
			GoodSnapshot gn = git.next();
			if(gn.name.equals(goodName))
				return gn;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.player.name + " " + this.takenAt.toString();
	}
	
	/**
	 * Sorts a list of RealmSnapshot objects by descending date : from the most recent to the oldest.
	 */
    public static final Comparator<RealmSnapshot> BY_DATE_DESC = new Comparator<RealmSnapshot>() {
        @Override
        public int compare(RealmSnapshot a1, RealmSnapshot a2) {
            return a2.takenAt.compareTo(a1.takenAt);
        }
    };
    
    /**
     * Sorts a list of RealmSnapshot objects by ascending date : from the oldest to the most recent.
     */
    public static final Comparator<RealmSnapshot> BY_DATE_ASC = new Comparator<RealmSnapshot>() {
        @Override
        public int compare(RealmSnapshot a1, RealmSnapshot a2) {
            return a1.takenAt.compareTo(a2.takenAt);
        }
    };
	
}
