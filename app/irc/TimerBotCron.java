package irc;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;

import models.players.Player;
import models.players.PlayerTimer;

import org.jibble.pircbot.User;

import play.db.jpa.JPAPlugin;
import play.i18n.Messages;

class TimerBotCron extends TimerTask {

	protected TimerBot bot;

	public TimerBotCron(TimerBot bot) {
		this.bot = bot;
	}

	@Override
	public void run() {
		JPAPlugin.startTx(true); // /!\ NEEDED TO USE THE DATABASE /!\
		// Loop channels
		for (String channel : bot.getChannels()) {
			// Get users on the current channel
			User[] users = bot.getUsers(channel);
			for (User u : users) {
				String nick = u.getNick();
				// Get player from DB
				Player player = Player.find(nick);
				if (player != null) {
					// If player has timers
					if (player.timers.size() > 0) {
						Iterator<PlayerTimer> it = player.timers.iterator();
						while (it.hasNext()) {
							PlayerTimer pt = it.next();
							// If timer is about to expire AND a reminder is set
							if (pt.remind && pt.doesExpireSoon()) {
								HashMap<String, Integer> exp = pt.getTimeBeforeExpiration();
								int h = exp.get("h");
								int m = exp.get("m");
								int s = exp.get("s");
								bot.sendMessage(nick, Messages.getMessage(TimerBot.LOCALE, "irc.timer.will.expire", pt.name, h, m, s));
							} else if (pt.remind && pt.endAt.before(new Date())) {
								HashMap<String, Integer> exp = pt.getTimeAfterExpiration();
								int h = exp.get("h");
								int m = exp.get("m");
								int s = exp.get("s");
								bot.sendMessage(nick, Messages.getMessage(TimerBot.LOCALE, "irc.timer.has.expired", pt.name, h, m, s));
							}
						}
					}
				}
			}
		}
		JPAPlugin.closeTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
	}


}
