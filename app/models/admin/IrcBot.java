package models.admin;

import irc.IRC;
import irc.MessageBot;
import irc.RandomBot;
import irc.TimerBot;

import java.util.Comparator;

import javax.persistence.Entity;

import org.jibble.pircbot.PircBot;

import models.guilds.Guild;
import models.players.Player;

import play.db.jpa.JPABase;
import play.db.jpa.Model;

@Entity
public class IrcBot extends Model {

	public String nick;
	public String password;
	public String hostname;
	public String channel;

	public String type;

	/* Static fields */
	public static final String BOT_TIMER = "TimerBot";
	public static final String BOT_MESSAGE = "MessageBot";
	public static final String BOT_RANDOM = "RandomBot";

	public IrcBot(String nick, String password, String hostname, String channel, String type) {
		super();
		this.nick = nick;
		this.password = password;
		this.hostname = hostname;
		this.channel = channel;
		this.type = type;
	}

	public void start() {
		if(IRC.BOTS.containsKey(this.id)) {
			this.stop();
		}

		PircBot mybot;
		if(this.type.equals(IrcBot.BOT_MESSAGE))
			mybot = new MessageBot(this);
		else if(this.type.equals(IrcBot.BOT_TIMER))
			mybot = new TimerBot(this);
		else
			mybot = new RandomBot(this);

		IRC.BOTS.put(this.id, mybot);
	}

	public void stop() {
		PircBot tb = IRC.BOTS.get(this.id);
		if(tb != null) {
			tb.disconnect();
			//tb.dispose(); // throws an exception	
			IRC.BOTS.remove(this.id);
			tb = null;
		}
	}

	public boolean isStarted() {
		if(!IRC.BOTS.containsKey(this.id))
			return false;
		PircBot bot = IRC.BOTS.get(this.id);
		if(bot.isConnected())
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return this.nick + " [" + this.hostname + " #" + this.channel + "]";
	}

}
