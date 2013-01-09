package jobs;

import java.util.Iterator;
import java.util.List;

import models.market.MarketAdventureSale;
import models.market.MarketBoostExchange;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;

@On("0 0 5 * * ?") // every day at 5 am
public class MarketCleaner extends Job<String> {

	/**
	 * Boosts exchanges older than X days will be deleted. This is X.
	 */
	public static final int boostExpirationInDays = 2;
	
	public String uuid = "";
	
	public MarketCleaner(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * This method call several private methods to clean the market.
	 * To run it : Promise p = new MarketCleaner().run()
	 */
	public String doJobWithResult() {
		String result = "";
		try {
			Logger.info("Starting MarketCleaner Job.");
			Cache.set("JobStatus_" + uuid + "_done", false);
			Cache.set("JobStatus_" + uuid + "_message", "Starting MarketCleaner Job.");
			Cache.set("JobStatus_" + uuid + "_current", "MarketCleaner Job started...");
			Cache.set("JobStatus_" + uuid + "_percent", 0);
			DateTime start = new DateTime();
			result += deleteExpiredBoostExchanges(); // this does the job
			result += deleteExpiredAdventuresExchanges(); // this does the job
			DateTime end = new DateTime();
			Period time = new Duration(start, end).toPeriod();
			Cache.set("JobStatus_" + uuid + "_done", true);
			Cache.set("JobStatus_" + uuid + "_message", "MarketCleaner Job done.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_message", "Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec<br />" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_current", "MarketCleaner Job done.");
			Cache.set("JobStatus_" + uuid + "_percent", 100);
			Logger.info("MarketCleaner Job Done");
			Logger.info("MarketCleaner Job done in " + time.getHours() + "h " + time.getMinutes() + "min " + time.getSeconds() + "sec");
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("MarketCleaner FAILED", e);
			result += "<b>MarketCleaner failed !</b><br />" + e;
			Cache.set("JobStatus_" + uuid + "_message", "MarketCleaner JOB FAILED.<br />" + Cache.get("JobStatus_" + uuid + "_message"));
		}
		return result;
	}
	
	/**
	 * Delete expired boosts exchange offer. A boost is expired if its due date is before
	 * now - {@link MarketCleaner}.boostExpirationInDays days. Default value is 2 days, so all exchanges
	 * older than 2 days are deleted from the database.
	 * All deletions are appended to INFO log.
	 * The method outputs a String in HTML format, that can be displayed on screen.
	 * @return String result, HTML formatted.
	 */
	private String deleteExpiredBoostExchanges() {
		String result = "";
		int deleted = 0;
		DateTime now = new DateTime();
		List<MarketBoostExchange> exs = MarketBoostExchange.all().fetch();
		Iterator<MarketBoostExchange> it = exs.iterator();
		int count = 0;
		int total = exs.size();
		while(it.hasNext()) {
			count++;
			MarketBoostExchange mbe = it.next();
			// Expired 2 (or more) days ago
			if(mbe.done || new DateTime(mbe.boostAt).isBefore(now.minusDays(MarketCleaner.boostExpirationInDays))) {
				String inres = "[JOB] Market Cleanup : deleted MarketBoostExchange <b>" + mbe.id + "</b>";
				String inlog = "[JOB] Market Cleanup : deleted MarketBoostExchange " + mbe.id;
				// Show progression
				Cache.set("JobStatus_" + uuid + "_current", inres);
				Cache.set("JobStatus_" + uuid + "_percent", (int)((count*100)/total));
				Cache.set("JobStatus_" + uuid + "_message", inres + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
				Logger.info(inlog);
				result += "<br />" + inres;
				// do it
				mbe.delete();
				deleted++;
			}
		}
		return "<br /><b>MarketBoostExchanges DELETED</b> : " + deleted + "<p>" + result + "</p>";
	}
	
	/**
	 * Delete expired adventures exchange offer. An adventure exchange is expired if its due date is in the past.
	 * All deletions are appended to INFO log.
	 * The method outputs a String in HTML format, that can be displayed on screen.
	 * @return String result, HTML formatted.
	 */
	private String deleteExpiredAdventuresExchanges() {
		String result = "";
		int deleted = 0;
		DateTime now = new DateTime();
		List<MarketAdventureSale> exs = MarketAdventureSale.all().fetch();
		Iterator<MarketAdventureSale> it = exs.iterator();
		int count = 0;
		int total = exs.size();
		while(it.hasNext()) {
			count++;
			MarketAdventureSale sale = it.next();
			// Expired 2 (or more) days ago
			if(now.isAfter(new DateTime(sale.endAt))) {
				String inres = "[JOB] Market Cleanup : deleted MarketAdventureSale <b>" + sale.id + "</b>";
				String inlog = "[JOB] Market Cleanup : deleted MarketAdventureSale " + sale.id;
				// Show progression
				Cache.set("JobStatus_" + uuid + "_current", inres);
				Cache.set("JobStatus_" + uuid + "_percent", (int)((count*100)/total));
				Cache.set("JobStatus_" + uuid + "_message", inres + "<br />" + Cache.get("JobStatus_" + uuid + "_message"));
				Logger.info(inlog);
				result += "<br />" + inres;
				// do it
				sale.delete();
				deleted++;
			}
		}
		return "<br /><b>MarketAdventureSales DELETED</b> : " + deleted + "<p>" + result + "</p>";
	}
	
}
