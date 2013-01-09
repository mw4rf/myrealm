package jobs;

import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import models.realm.Building;
import play.Logger;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import world.World;
import world.WorldBuilding;

/**
 * Update {@link World}'s static fields informations.
 * Runs asynchronously after server bootstrap, and every 6 hours after that.
 * @author mw4rf
 *
 */
@OnApplicationStart(async = true)
@Every("6h") // runs every 6 hours
public class WorldUpdater extends Job {

	public String uuid = "";
	
	public WorldUpdater(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * This method call several private methods to clean the market.
	 * To run it : Promise p = new WorldUpdater().run()
	 */
	public String doJobWithResult() {
		String result = "";
		try {
			Logger.info("Starting WorldUpdater Job.");
			Cache.set("JobStatus_" + uuid + "_done", false);
			Cache.set("JobStatus_" + uuid + "_message", "Starting WorldUpdater Job.");
			Cache.set("JobStatus_" + uuid + "_percent", 0);
			Cache.set("JobStatus_" + uuid + "_current", "Updating World stats...");
			DateTime start = new DateTime();
			result += this.calcBuildingAverageProductionTime(); // this does the actual job
			DateTime end = new DateTime();
			Period time = new Duration(start, end).toPeriod();
			Cache.set("JobStatus_" + uuid + "_done", true);
			Cache.set("JobStatus_" + uuid + "_message", "WorldUpdater Job done.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_message", "Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_percent", 100);
			Cache.set("JobStatus_" + uuid + "_current", "World stats updated.");
			Logger.info("WorldUpdater Job Done");
			Logger.info("WorldUpdater Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec");
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("WorldUpdater FAILED", e);
			result += "<b>WorldUpdater failed !</b><br />" + e;
			Cache.set("JobStatus_" + uuid + "_message", "WorldUpdater JOB FAILED.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_percent", 0);
			Cache.set("JobStatus_" + uuid + "_current", "FATAL ERROR!");
			Cache.set("JobStatus_" + uuid + "_done", true);
		}
		return result;
	}
	
	/**
	 * Get the average production time for each building type, from the database.
	 * This is a VERY LONG operation. It uses A LOT of CPU resources.
	 * Each building in {@link World} is updated with current values, calculated from the database.
	 */
    private String calcBuildingAverageProductionTime() {
    	String result = "<b>Computing Average Production Time for Buildings...</b>";
    	int count = 0;
    	int total = World.BUILDINGS.size();
    	Iterator<WorldBuilding> it = World.BUILDINGS.values().iterator();
    	while(it.hasNext()) {
    		WorldBuilding wb = it.next();
    		wb.setAverageProductionTime(Building.calcAverageProductionTime(wb.getName()));
    		count++;
    		String inres = "[JOB] WorldUpdater : Average Production Time updated : Building <b>" + count + "</b>/" + total + " -- <i>" + wb.getName() + "</i>";
    		String inlog = "[JOB] WorldUpdater : Average Production Time updated : Building " + count + "/" + total + " -- " + wb.getName();
    		result += "<br />" + inres;
    		// Show progression
    		Cache.set("JobStatus_" + uuid + "_current", inres);
    		Cache.set("JobStatus_" + uuid + "_percent", (int)((count*100)/total));
    		Cache.set("JobStatus_" + uuid + "_message", inres + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
    		Logger.info(inlog);
    	}
    	result += "<br /><b>" + count + " World Buildings updated</b>";
    	return result;
	}
	
	
}
