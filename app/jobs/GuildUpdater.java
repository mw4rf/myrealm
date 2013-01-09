package jobs;

import java.util.Iterator;
import java.util.List;

import models.guilds.Guild;
import models.market.MarketBoostExchange;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;

@On("0 5 0 * * ?") // every day at 0:05 every day
public class GuildUpdater extends Job<String> {

	public String uuid = "";
	
	public GuildUpdater(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * This method call several private methods to clean the market.
	 * To run it : Promise p = new MarketCleaner().run()
	 */
	public String doJobWithResult() {
		String result = "";
		try {
			Logger.info("Starting GuildUpdater Job.");
			Cache.set("JobStatus_" + uuid + "_done", false);
			Cache.set("JobStatus_" + uuid + "_message", "Starting GuildUpdater Job.");
			DateTime start = new DateTime();
			result += rollODayMember(); // this does the job
			DateTime end = new DateTime();
			Period time = new Duration(start, end).toPeriod();
			Cache.set("JobStatus_" + uuid + "_done", true);
			Cache.set("JobStatus_" + uuid + "_message", "GuildUpdater Job done.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_message", "Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Logger.info("GuildUpdater Job Done");
			Logger.info("GuildUpdater Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec");
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("GuildUpdater FAILED", e);
			result += "<b>GuildUpdater failed !</b><br />" + e;
			Cache.set("JobStatus_" + uuid + "_message", "GuildUpdater JOB FAILED.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
		}
		return result;
	}

	private String rollODayMember() {
		String result = "";
		List<Guild> guilds = Guild.all().fetch();
		Iterator<Guild> it = guilds.iterator();
		int count = 0;
		int total = guilds.size();
		while(it.hasNext()) {
			count++;
			Guild g = it.next();
			boolean rolled = g.cronRollODayMember();
			// Log & display
			String inres = "";
			String inlog = "";
			if(rolled) {
				inres += "[JOB] GuildUpdater : Roll O'Day Member : <span class=\"yes\">Guild <b>" + count + "</b>/" + total + " : [<b>" + g.tag + "</b>] <b>" + g.name + "</b> -- <i>ROLLED</i>.</span>";
				inlog += "[JOB] GuildUpdater : Roll O'Day Member : Guild " + count + "/" + total + " : [" + g.tag + "] " + g.name + " -- ROLLED";
			} else {
				inres += "[JOB] GuildUpdater : Roll O'Day Member : <span class=\"no\">Guild <b>" + count + "</b>/" + total + " : [<b>" + g.tag + "</b>] <b>" + g.name + "</b> -- <i>No need to roll now</i>.</span>";
				inlog += "[JOB] GuildUpdater : Roll O'Day Member : Guild " + count + "/" + total + " : [" + g.tag + "] " + g.name + " -- No need to roll now.";
			}
			// Show progression
			Cache.set("JobStatus_" + uuid + "_current", inres);
			Cache.set("JobStatus_" + uuid + "_percent", (int)((count*100)/total));
			Cache.set("JobStatus_" + uuid + "_message", inres + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Logger.info(inlog);
		}
		result += "<br /><b>ROLLED O'DAY GUILD MEMBERS for " + count + " guilds </b>";
		return result;
	}

}
