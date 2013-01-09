package jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import models.players.Player;
import models.players.PlayerAction;
import models.realm.snapshots.BuildingSnapshot;
import models.realm.snapshots.GoodSnapshot;
import models.realm.snapshots.RealmSnapshot;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.On;

@On("0 0 15 L * ?") // every last (L) day of the MONTH, at 15:00:00 (3 pm)
public class SnapshotCleaner extends Job<String> {

	String uuid = "";
	
	public SnapshotCleaner(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * This method call several private methods to clean the market.
	 * To run it : Promise p = new SnapshotCleaner().run()
	 */
	public String doJobWithResult() {
		String result = "";
		try {
			Logger.info("Starting SnapshotCleaner Job.");
			Cache.set("JobStatus_" + uuid + "_done", false);
			Cache.set("JobStatus_" + uuid + "_message", "Starting SnapshotCleaner Job.");
			Cache.set("JobStatus_" + uuid + "_current", "Starting SnapshotCleaner Job");
			Cache.set("JobStatus_" + uuid + "_percent", 0);
			DateTime start = new DateTime();
			result += cleanSnapshots(); // this does the job
			//result += cleanRemains();
			DateTime end = new DateTime();
			Period time = new Duration(start, end).toPeriod();
			Cache.set("JobStatus_" + uuid + "_current", "Job done");
			Cache.set("JobStatus_" + uuid + "_percent", 100);
			Cache.set("JobStatus_" + uuid + "_done", true);
			Cache.set("JobStatus_" + uuid + "_message", "SnapshotCleaner Job done.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_message", "Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Logger.info("SnapshotCleaner Job Done");
			Logger.info("SnapshotCleaner Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec");
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("SnapshotCleaner JOB FAILED", e);
			Cache.set("JobStatus_" + uuid + "_message", "SnapshotCleaner JOB FAILED.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			result += "<b>Snapshot build failed !</b><br />" + e;
		}
		return result;
	}
	
	private String cleanSnapshots() {
		// Last month date
		Date lastMonth = new DateTime().minusDays(new DateTime().getDayOfMonth()).toDate();
		//--------------------------------------------------------------------------------------
		// Get old snapshots = not locked AND taken BEFORE last month
		Cache.set("JobStatus_" + uuid + "_current", "Getting old Snapshots to be locked");
		Cache.set("JobStatus_" + uuid + "_message", "[JOB] SnapshotCleaner : Getting old SnapShots. <br />" + Cache.get("JobStatus_" + uuid + "_message"));
		List<RealmSnapshot> snaps = RealmSnapshot.find("takenAt < DATE(?) and locked = ? order by takenAt desc", lastMonth, false).fetch();
		Logger.info("[JOB] SnapshotCleaner : Found " + snaps.size() + " snapshots");
		HashMap<Long,RealmSnapshot> lockmap = new HashMap(); // <Player.id,RealmSnapshot>
		Iterator<RealmSnapshot> it = snaps.iterator();
		while(it.hasNext()) {
			RealmSnapshot sn = it.next();
			lockmap.put(sn.player.id, sn); // overwrite any existing snapshot for that player, to make sure only 1 snapshot per player remains
		}
		Cache.set("JobStatus_" + uuid + "_message", "[JOB] SnapshotCleaner : " + snaps.size() + " old SnapShots found.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
		
		//--------------------------------------------------------------------------------------
		// Lock 1 snapshot in the last month
		Cache.set("JobStatus_" + uuid + "_message", "[JOB] SnapshotCleaner : Locking " + lockmap.size() + " old SnapShots. <br />" + Cache.get("JobStatus_" + uuid + "_message"));
		Cache.set("JobStatus_" + uuid + "_current", "Locking old snapshots");
		try {
			List<RealmSnapshot> toBeLocked = new ArrayList<RealmSnapshot>(lockmap.values());
			for(int i = 0 ; i < toBeLocked.size() ; i++) {
				RealmSnapshot sn = toBeLocked.get(i);
				sn.locked = true;
				sn.save();
				sn.player.logAction(PlayerAction.LOG_SNAPSHOT_LOCKED, sn.toString()); // LOG
				sn.player.logAction(PlayerAction.LOG_SNAPSHOT_CLEANUP, ""); // LOG
				Logger.info("[JOB] SnapshotCleaner : Snapshot [" + i + "/" + (toBeLocked.size()-1) + "] Locked : " + sn.toString());
				Cache.set("JobStatus_" + uuid + "_message", "[JOB] SnapshotCleaner : Snapshot " + i + "/" + toBeLocked.size() + " Locked : " + sn.toString() + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
				Cache.set("JobStatus_" + uuid + "_percent", (int) (i * 100 / toBeLocked.size()-1));
				// Commit & flush DB
				// This is VERY important. If we don't do that, the server will break !!
				RealmSnapshot.em().persist(sn);
				RealmSnapshot.em().getTransaction().commit();
				RealmSnapshot.em().getTransaction().begin();
				
			}
		} catch(Exception e) {;}
		
		//--------------------------------------------------------------------------------------
		// Get snapshots again
		snaps.clear();
		snaps = null;
		snaps = RealmSnapshot.find("takenAt < DATE(?) and locked = ? order by takenAt desc", lastMonth, false).fetch();
		List<Long> pids = new ArrayList<Long>();
		Logger.info(pids.size() + " snapshots will be deleted.");
		Cache.set("JobStatus_" + uuid + "_current", "Deleting " + pids.size() + " old snapshots");
		Cache.set("JobStatus_" + uuid + "_message", "Now Deleting <b>" + pids.size() + "</b> old SnapShots.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
		Iterator<RealmSnapshot> ait = snaps.iterator();
		while(ait.hasNext()) {
			RealmSnapshot sn = ait.next();
			pids.add(sn.id); // add this id to be deleted
		}
		
		//--------------------------------------------------------------------------------------
		// Delete old snapshots
		int total = pids.size();
		int count;
		for(count = 0 ; count < total ; count++) {
			RealmSnapshot toDel = RealmSnapshot.findById(pids.get(count));
			String realmName = toDel.player.name;			
			toDel.delete();
			Logger.info("Snapshot deleted: " + toDel.toString());
			// Logging & displaying infos
			String inres = "[JOB] SnapshotCleaner : <span class=\"yes\">[Realm <b>" + count + "</b>/" + (total - 1) + "] Snapshot deleted for <b>" + realmName + "</b></span>";
			String inlog = "[JOB] SnapshotCleaner : [Realm " + count + "/" + (total - 1) + "] Snapshot deleted for " + realmName;
			// Show progression
			Cache.set("JobStatus_" + uuid + "_current", inres);
			Cache.set("JobStatus_" + uuid + "_percent", (int)((count*100)/(total - 1)));
			Cache.set("JobStatus_" + uuid + "_message", inres + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			// log
			Logger.info(inlog);
			// Commit & flush DB
			// This is VERY important. If we don't do that, the server will break !!
			// Commit & flush DB
			// This is VERY important. If we don't do that, the server will break !!
			RealmSnapshot.em().getTransaction().commit();
			RealmSnapshot.em().getTransaction().begin();
		}
		// return
		return "Snapshot job: " + count + " realms processed.";
	}
	
	@Deprecated
	public String cleanRemains() {
		String result = "";
		int bcount = 0;
		int gcount = 0;
		int total = 0;
		int current = 0;
		Logger.info("Clearing orphan Building snapshots...");
		Cache.set("JobStatus_" + uuid + "_message", "Cleaning orphan Building Snapshots...</b><br />" + Cache.get("JobStatus_" + uuid + "_message"));
		List<BuildingSnapshot> bsl = BuildingSnapshot.findAll();
		total = bsl.size();
		for(Iterator<BuildingSnapshot> it = bsl.iterator(); it.hasNext();) {
			BuildingSnapshot bs = it.next();
			if(bs.snapshot == null || bs.snapshot.id == null) {
				bs.delete();
				bcount++;
			}
			Cache.set("JobStatus_" + uuid + "_current", current + "/" + total);
			Cache.set("JobStatus_" + uuid + "_percent", (int)((current*100)/(total - 1)));
			current++;
		}
		Logger.info(bcount + " orphan Building snapshots deleted");
		Cache.set("JobStatus_" + uuid + "_message", bcount + " orphan Building Snapshots deleted.</b><br />" + Cache.get("JobStatus_" + uuid + "_message"));
		Logger.info("Clearing orphan Goods snapshots...");
		Cache.set("JobStatus_" + uuid + "_message", "Cleaning orphan Goods Snapshots...</b><br />" + Cache.get("JobStatus_" + uuid + "_message"));
		List<GoodSnapshot> gsl = GoodSnapshot.findAll();
		total = gsl.size();
		current = 0;
		for(Iterator<GoodSnapshot> it = gsl.iterator(); it.hasNext();) {
			GoodSnapshot gs = it.next();
			if(gs.snapshot == null || gs.snapshot.id == null) {
				gs.delete();
				gcount++;
			}
			Cache.set("JobStatus_" + uuid + "_current", current + "/" + total);
			Cache.set("JobStatus_" + uuid + "_percent", (int)((current*100)/(total - 1)));
			current++;
		}
		Logger.info(gcount + " orphan Goods snapshots deleted");
		Cache.set("JobStatus_" + uuid + "_message", gcount + " orphan Goods Snapshots deleted.</b><br />" + Cache.get("JobStatus_" + uuid + "_message"));
		result += "<br />" + bcount + " orphan Building Snapshots deleted<br />" + gcount + " orphan Good Snapshot deleted";
		return result;
	}
}
