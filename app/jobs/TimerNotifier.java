package jobs;

import java.util.Iterator;

import notifiers.TimerMailers;

import models.players.PlayerTimer;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@Every("15mn") // runs every 15 minutes
public class TimerNotifier extends Job {
	
	public static final int RUNTIME = 15 * 60; // runtime in seconds = 15 minutes
	
	@Override
	public void doJob() {
		
		// Security: do not send mail in development mode (production only)
		if(play.Play.configuration.get("application.mode").equals("dev")) {
			System.out.println("Dev mode: mailer jobs canceled.");
			return;
		}
		
		// 1. Expiring timers
		for(Iterator<PlayerTimer> it = PlayerTimer.getExpiringTimers(TimerNotifier.RUNTIME).iterator() ; it.hasNext() ;) {
			PlayerTimer timer = it.next();
			// If notifications are disabled, continue.
			if(!timer.remind)
				continue;
			// Check if player has e-mail address AND wants e-mail notification for expiring timers
			if(!timer.player.preferences.hasMailAddress() || !timer.player.preferences.MAIL_NOTIFY_TIMERS_EXPIRING)
				continue;
			// Send the mail
			TimerMailers.expiringTimers(timer);
		}
		
		// 2. Expired timers
		for(Iterator<PlayerTimer> it = PlayerTimer.getExpiredTimers(TimerNotifier.RUNTIME).iterator() ; it.hasNext() ;) {
			PlayerTimer timer = it.next();
			// If notifications are disabled, continue.
			if(!timer.remind)
				continue;
			// Check if player has e-mail address AND wants e-mail notification for expiring timers
			if(!timer.player.preferences.hasMailAddress() || !timer.player.preferences.MAIL_NOTIFY_TIMERS_EXPIRED)
				continue;
			// Send the mail
			TimerMailers.expiredTimers(timer);
		}
		
	}
	
}
