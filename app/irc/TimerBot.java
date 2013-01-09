package irc;


import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.Entity;

import models.admin.IrcBot;
import models.players.Player;
import models.players.PlayerTimer;

import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import play.db.Model;
import play.db.jpa.JPAPlugin;
import play.i18n.Messages;

public class TimerBot extends PircBot {

	public IrcBot bot = null;

	public TimerBotCron cron;
	public Timer timer = new Timer();

	public static final String LOCALE = "fr";
	public static final String DATE_FORMAT = "dd/MM/yyyy à HH:mm";

	public static final String COMMAND_INFO = "info";
	public static final String COMMAND_EXPIRE = "exp";
	public static final String COMMAND_HELP = "help";
	public static final String COMMAND_HELP_ALT = "aide";

	public TimerBot(IrcBot bot) {
		this.bot = bot;
		// Start bot
		this.setName(bot.nick);
		this.setAutoNickChange(true);
		try {
			this.connect(bot.hostname);
			this.joinChannel("#" + bot.channel);
		} catch(Exception ex) {;}
		// Start reminder thread
		this.cron = new TimerBotCron(this);
		this.timer.scheduleAtFixedRate(this.cron, 1000 * 60, 60 * 1000 * 10); // start after 1 minute, run every 10 minutes
		//this.timer.scheduleAtFixedRate(this.cron, 2000, 5000); // DEBUG
	}


	@Override
	protected void onNickChange(String oldNick, String login, String hostname,
			String newNick) {
		bot.nick = newNick;
		bot.save();
	}

	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		JPAPlugin.startTx(true); // /!\ NEEDED TO USE THE DATABASE /!\
		String[] split = message.split(" ");
		// Kronos info
		if(Tools.startsWithIgnoreCase(message, TimerBot.COMMAND_INFO)) {
			// List all timers
			if(split.length < 2) {
				this.listTimers(sender, sender);
			}
			// Show timer details
			else
				this.showTimer(sender, split[1]);
		}
		// Kronos expiration 60
		else if(message.toLowerCase().contains(TimerBot.COMMAND_EXPIRE.toLowerCase())) {
			int min = 0;
			for(String s : split) {
				if(Tools.isInteger(s))
				{
					min = Integer.parseInt(s);
					break;
				}
			}
			if(min == 0) {
				sendMessage(sender, Messages.getMessage(TimerBot.LOCALE, "irc.bad.syntax", sender));
				return;
			} else {
				this.listTimersWithExpiration(sender, sender, min);
			}
		}
		// Help
		else if(message.toLowerCase().contains(TimerBot.COMMAND_HELP.toLowerCase()) || message.toLowerCase().contains(TimerBot.COMMAND_HELP_ALT.toLowerCase())) {
			sendMessage(sender, Messages.getMessage(TimerBot.LOCALE, "irc.timer.help", this.getNick()));
		}
		else {
			sendMessage(sender, "Je n'ai pas compris ! (envoyez-moi : info)");
		}
		JPAPlugin.closeTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
	}


	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		JPAPlugin.startTx(true); // /!\ NEEDED TO USE THE DATABASE /!\
		// Kronos info
		if(message.toLowerCase().contains(this.getNick().toLowerCase()) && message.toLowerCase().contains(TimerBot.COMMAND_INFO.toLowerCase())) {
			sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.timer.report.private", sender));
			this.listTimers(sender, sender);
		}
		// Kronos expiration 60
		else if(message.toLowerCase().contains(this.getNick().toLowerCase()) && message.toLowerCase().contains(TimerBot.COMMAND_EXPIRE.toLowerCase())) {
			String[] split = message.split(" ");
			int min = 0;
			for(String s : split) {
				if(Tools.isInteger(s))
					min = Integer.parseInt(s);
			}
			if(min == 0) {
				sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.bad.syntax", sender));
				return;
			} else {
				int count = this.listTimersWithExpiration(sender, sender, min);
				if(count == -1) {
					sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.timer.report.private", sender));
				} else if(count == 0) {
					this.sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.timer.answer.exp.no", min));
				} else {
					sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.timer.answer.exp.count", sender, count, min));
					sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.timer.report.private", sender));
				}
			}
		}
		// Help
		else if(message.toLowerCase().contains(this.getNick().toLowerCase()) && ( message.toLowerCase().contains(TimerBot.COMMAND_HELP.toLowerCase()) || message.toLowerCase().contains(TimerBot.COMMAND_HELP_ALT.toLowerCase()) ) ) {
			sendMessage(channel, Messages.getMessage(TimerBot.LOCALE, "irc.timer.report.private", sender));
			sendMessage(sender, Messages.getMessage(TimerBot.LOCALE, "irc.timer.help", this.getNick()));
		}
		else if(message.contains(this.getNick()))
			sendMessage(channel, "Oui ?");
		JPAPlugin.closeTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
	}


	private void listTimers(String sender, String to) {
		Player player = Player.find(sender);
		// Player not found
		if(player == null) {
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.no.such.player", sender));
			return;
		}
		// Player found : no timer
		if(player.timers.size() < 1) {
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.no.timer", sender));
			return;
		}
		// Header
		if(player.timers.size() > 0)
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.timer.answer.info", sender));
		// Sort timers by expiration date
		Collections.sort(player.timers, PlayerTimer.BY_END_DATE_ASC);
		// List timers now
		Iterator<PlayerTimer> it = player.timers.iterator();
		while(it.hasNext()) {
			PlayerTimer pt = it.next();
			HashMap<String,Integer> exp = pt.getTimeBeforeExpiration();
			int h = exp.get("h");
			int m = exp.get("m");
			int s = exp.get("s");
			DateTimeFormatter formatter = DateTimeFormat.forPattern(TimerBot.DATE_FORMAT);
			DateTime timerEnd = new DateTime(pt.endAt);
			this.sendMessage(to, "[" + sender + "] " + Messages.getMessage(TimerBot.LOCALE,"irc.timer.details", pt.name, formatter.print(timerEnd), h, m, s));
		}
	}

	private int listTimersWithExpiration(String sender, String to, int minutes) {
		Player player = Player.find(sender);
		// Player not found
		if(player == null) {
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.no.such.player", sender));
			return -1;
		}
		// Player found : no timer
		if(player.timers.size() < 1) {
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.no.timer", sender));
			return -1;
		}
		// Header
		if(player.timers.size() > 0)
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.timer.answer.exp", minutes));
		// Sort timers by expiration date
		Collections.sort(player.timers, PlayerTimer.BY_END_DATE_ASC);
		// List timers now
		Iterator<PlayerTimer> it = player.timers.iterator();
		int count = 0;
		while(it.hasNext()) {
			PlayerTimer pt = it.next();
			// will if expire in the next <minutes> ?
			DateTime delay = new DateTime().plusMinutes(minutes);
			DateTime endAt = new DateTime(pt.endAt);
			if(endAt.isBefore(delay)) {
				HashMap<String,Integer> exp = pt.getTimeBeforeExpiration();
				int h = exp.get("h");
				int m = exp.get("m");
				int s = exp.get("s");
				DateTimeFormatter formatter = DateTimeFormat.forPattern(TimerBot.DATE_FORMAT);
				DateTime timerEnd = new DateTime(pt.endAt);
				this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE,"irc.timer.details", pt.name, formatter.print(timerEnd), h, m, s));
				count++;
			}
		}
		// Footer
		if(count < 1)
			this.sendMessage(to, Messages.getMessage(TimerBot.LOCALE, "irc.timer.answer.exp.no", minutes));
		return count;
	}

	private void showTimer(String sender, String timerName) {
		sendMessage(sender, "Cette fonction n'est pas encore implémentée. Envoyez-moi 'aide'");
	}


	@Override
	protected void onKick(String channel, String kickerNick,String kickerLogin, String kickerHostname, String recipientNick,String reason) {
		if(recipientNick.equals(this.getNick())) {
			this.joinChannel(channel);
			this.deOp(channel, kickerNick);
			this.kick(channel, kickerNick);
		}
	}



}
