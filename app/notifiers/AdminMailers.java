package notifiers;

import java.util.HashMap;

import javax.persistence.Query;

import models.admin.IrcBot;
import models.adventures.Adventure;
import models.adventures.AdventureComment;
import models.guilds.Guild;
import models.guilds.GuildWallMessage;
import models.guilds.Membership;
import models.players.Player;
import models.players.PlayerAction;
import models.players.PlayerTimer;
import models.realm.Building;
import models.realm.snapshots.BuildingSnapshot;
import models.realm.snapshots.GoodSnapshot;
import models.realm.snapshots.RealmSnapshot;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import play.db.jpa.JPA;
import play.exceptions.PlayException;
import play.mvc.Mailer;

public class AdminMailers extends Mailer {

	/**
	 * Simple test method, sending a test e-mail to the administrator 
	 * (see myrealm.admin.mail in conf/application.conf)
	 */
	public static void test() {
		String to = play.Play.configuration.get("myrealm.admin.root") + " <" + play.Play.configuration.get("myrealm.admin.mail") + " >";
		setSubject("Hello World, %s!", play.Play.configuration.get("myrealm.admin.root"));
		addRecipient(play.Play.configuration.get("myrealm.admin.mail"));
		setFrom(play.Play.configuration.get("myrealm.admin.mail.from"));
		//addAttachment(Play.getFile("rules.pdf"));
		send(to); // will send views/AdminMailers/test.html and test.txt
	}
	
	/**
	 * Send an e-mail to MyRealm administrator with an exception report.
	 * A link is given to this mailer action when a user gets the infamous "Oops" error message.
	 * (see. app/views/errors/500.html)
	 */
	public static void exceptionReport(String title, String description, String source, Integer line) {
		// Send mail
		String to = play.Play.configuration.get("myrealm.admin.root") + " <" + play.Play.configuration.get("myrealm.admin.mail") + " >";
		setSubject(play.Play.configuration.get("application.name").toString() + " Exception Report");
		addRecipient(play.Play.configuration.get("myrealm.admin.mail"));
		setFrom(play.Play.configuration.get("myrealm.admin.mail.from"));
		send(to, title, description, source, line);
	}
	
	/**
	 * Sends a report of activity to the administrator, with some infos like
	 * server uptime, database tables size, etc.
	 */
	public static void activityReport() {
		// Pretty Table names
		HashMap<String,String> tables = new HashMap();
		tables.put("player", "Players");
		tables.put("building", "Buildings");
		tables.put("adventure", "Adventures");
		tables.put("adventurecomment", "Adventures Comments");
		tables.put("guild", "Guilds");
		tables.put("membership", "Guild Memberships");
		tables.put("guildwallmessage", "Guild Wall Messages");
		tables.put("ircbot", "IRC Bots");
		tables.put("playertimer", "Player Timers");
		tables.put("playeraction", "Player Actions");
		tables.put("realmsnapshot", "Realm Snapshots");
		tables.put("buildingsnapshot", "Buildings Snapshots");
		tables.put("goodsnapshot", "Goods Snapshots");
		// Stats: counts
		HashMap<String,Long> tableCount = new HashMap();
		tableCount.put("player", Player.count());
		tableCount.put("building", Building.count());
		tableCount.put("adventure", Adventure.count());
		tableCount.put("adventurecomment", AdventureComment.count());
		tableCount.put("guild", Guild.count());
		tableCount.put("membership", Membership.count());
		tableCount.put("guildwallmessage", GuildWallMessage.count());
		tableCount.put("ircbot", IrcBot.count());
		tableCount.put("playertimer", PlayerTimer.count());
		tableCount.put("playeraction", PlayerAction.count());
		tableCount.put("realmsnapshot", RealmSnapshot.count());
		tableCount.put("buildingsnapshot", BuildingSnapshot.count());
		tableCount.put("goodsnapshot", GoodSnapshot.count());
		// Stats: sizes
		HashMap<String,String> tableSize = new HashMap();
		if(play.Play.configuration.get("db").toString().startsWith("postgres")) {
			for(String t : tables.keySet()) {
				Query q = JPA.em().createNativeQuery("select pg_size_pretty(pg_total_relation_size('" + t + "'));");
				tableSize.put(t, q.getSingleResult().toString());
			}
		} else {
			for(String t : tables.keySet())
				tableSize.put(t, "?");
		}
		// Serveur uptime
		DateTime startedSince = new DateTime(play.Play.startedAt);
		Period uptime = new Duration(startedSince, new DateTime()).toPeriod();
		// Send mail
		String to = play.Play.configuration.get("myrealm.admin.root") + " <" + play.Play.configuration.get("myrealm.admin.mail") + " >";
		setSubject(play.Play.configuration.get("application.name").toString() +  " activity report");
		addRecipient(play.Play.configuration.get("myrealm.admin.mail"));
		setFrom(play.Play.configuration.get("myrealm.admin.mail.from"));
		send(to, tables, tableCount, tableSize, startedSince, uptime); // will send views/AdminMailers/activityReport.html	
	}
	
}
