package models.realm.snapshots;

import java.util.Iterator;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.players.Player;

import play.db.jpa.Model;
import world.World;
import world.WorldBuilding;
import world.WorldGood;

@Entity
public class GoodSnapshot extends Model {
	
	@ManyToOne
	public RealmSnapshot snapshot;
	
	public String name;
	public Double quantity = (double) 0;
	public Double produced = (double) 0;
	public Double needed =   (double) 0;
	
	public GoodSnapshot(RealmSnapshot snapshot) {
		this.snapshot = snapshot;
	}
	
	/**
	 * Get the average production by cycle, in all snapshots, for the given good
	 * @param player
	 * @param goodName
	 * @return
	 */
	public static double getAverageProducedQuantity(Player player, String goodName) {
		double sum = (double) 0;
		double count = (double) 0;
		Iterator<RealmSnapshot> snit = player.snapshots.iterator();
		while(snit.hasNext()) {
			RealmSnapshot snap = snit.next();
			GoodSnapshot gs = snap.getGoodSnapshot(goodName);
			// Check if snapshot has this good, if not continue
			if(gs == null)
				continue;
			// Snapshot has this good, check value
			if(gs.produced != null && gs.produced > 0)
				sum += gs.produced;
			count++;
		}
		if(count == 0)
			return (double) 0;
		else
			return (double) sum / count;
	}
	
	/**
	 * Get the average needs by cycle, in all snapshots, for the given good
	 * @param player
	 * @param goodName
	 * @return
	 */
	public static double getAverageNeededQuantity(Player player, String goodName) {
		double sum = (double) 0;
		double count = (double) 0;
		Iterator<RealmSnapshot> snit = player.snapshots.iterator();
		while(snit.hasNext()) {
			RealmSnapshot snap = snit.next();
			GoodSnapshot gs = snap.getGoodSnapshot(goodName);
			// Check if snapshot has this good, if not continue
			if(gs == null)
				continue;
			// Snapshot has this good, check value
			if(gs.needed != null && gs.needed < 0)
				sum += gs.needed;
			count++;
		}
		if(count == 0)
			return (double) 0;
		else
			return (double) sum / count;
	}
	
	

}
