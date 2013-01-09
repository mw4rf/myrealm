package jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import models.players.Player;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;

@On("0 0 0 * * ?") // every day at midnight
public class SnapshotTaker extends Job<String> {

	String uuid = "";
	
	public SnapshotTaker(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * This method call several private methods to clean the market.
	 * To run it : Promise p = new SnapshotTaker().run()
	 */
	public String doJobWithResult() {
		String result = "";
		try {
			Logger.info("Starting SnapshotTaker Job.");
			Cache.set("JobStatus_" + uuid + "_done", false);
			Cache.set("JobStatus_" + uuid + "_message", "Starting SnapshotTaker Job.");
			DateTime start = new DateTime();
			result += takeSnapshots(); // this does the job
			DateTime end = new DateTime();
			Period time = new Duration(start, end).toPeriod();
			Cache.set("JobStatus_" + uuid + "_done", true);
			Cache.set("JobStatus_" + uuid + "_message", "SnapshotTaker Job done.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_message", "Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Logger.info("SnapshotTaker Job Done");
			Logger.info("SnapshotTaker Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec");
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("SnapshotTaker JOB FAILED", e);
			Cache.set("JobStatus_" + uuid + "_message", "SnapshotTaker JOB FAILED.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			result += "<b>Snapshot build failed !</b><br />" + e;
		}
		return result;
	}
	
	private String takeSnapshots() {
		// Get all players connected is the last 24hours
		Date yesterday = new DateTime().minusDays(1).toDate();
		// We use a trick here. If we loop the List<Players> players and commit(), flush() and clear() the transaction,
		// we get a hibernation error : after the first iteration of the loop, the transaction is closed, and therefore
		// not available anymore for the second iteration of the loop.
		// The trick is opening a new transaction for each iteration of the loop, commiting, flushing and clearing this
		// transaction when the job is done. 
		// To do that, we load the IDs of the users, and for each ID we load the user.
		// Despite appearences, this IS an optimization (because for such a job, we MUST flush and clear after
		// each iterator ; if we don't, we'll run out of memory and the server will break.
		List<Player> players = Player.find("lastConnection > ? OR lastVisit > ?", yesterday, yesterday).fetch();
		List<Long> pids = new ArrayList<Long>();
		Iterator<Player> it = players.iterator();
		while(it.hasNext()) {
			pids.add(it.next().id);
		}
		int total = pids.size();
		// For each player, take snapshot
		int count;
		for(count = 0 ; count < total ; count++) {
			Player p = Player.findById(pids.get(count));
			// Logging & displaying infos
			String inres = "";
			String inlog = "";
			// do the long job now
			if(p.takeRealmSnapshot()) {
				inres = "[JOB] SnapshotTaker : <span class=\"yes\">[Realm <b>" + count + "</b>/" + (total - 1) + "] Snapshot taken for <b>" + p.name + "</b></span>";
				inlog = "[JOB] SnapshotTaker : [Realm " + count + "/" + (total - 1) + "] Snapshot taken for " + p.name;
			}
			else {
				inres = "[JOB] SnapshotTaker : <span class=\"no\">[Realm <b>" + count + "</b>/" + (total - 1) + "] Realm empty for <b>" + p.name + "</b> ; <i>no snapshot taken</i></span>";
				inlog = "[JOB] SnapshotTaker : [Realm " + count + "/" + (total - 1) + "] Realm empty for " + p.name + " ; no snapshot taken";
			}
			// Show progression
			Cache.set("JobStatus_" + uuid + "_current", inres);
			if(total > 1) // prevent divide by zero
				Cache.set("JobStatus_" + uuid + "_percent", (int)((count*100)/(total - 1)));
			else
				Cache.set("JobStatus_" + uuid + "_percent", 100);
			Cache.set("JobStatus_" + uuid + "_message", inres + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			// log
			Logger.info(inlog);
			// Commit & flush DB
			// This is VERY important. If we don't do that, the server will break !!
			Player.em().getTransaction().commit();
			Player.em().getTransaction().begin();
			Player.em().flush();
			Player.em().clear();
			//
		}
		// return
		return "Snapshot job: " + count + " realms processed.";
	}
	
}
