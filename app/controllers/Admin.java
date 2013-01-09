package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import notifiers.AdminMailers;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;

import jobs.Backup;
import jobs.GuildUpdater;
import jobs.MarketCleaner;
import jobs.SnapshotCleaner;
import jobs.SnapshotTaker;
import jobs.WorldLoader;
import jobs.WorldUpdater;

import models.admin.AdminMessage;
import models.admin.Greeting;
import models.admin.IrcBot;
import models.adventures.Adventure;
import models.adventures.AdventureComment;
import models.guilds.Guild;
import models.guilds.GuildWallMessage;
import models.guilds.Membership;
import models.players.Player;
import models.players.PlayerAction;
import models.players.PlayerPreferences;
import models.players.PlayerTimer;
import models.realm.Building;
import models.realm.snapshots.BuildingSnapshot;
import models.realm.snapshots.GoodSnapshot;
import models.realm.snapshots.RealmSnapshot;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.libs.Codec;
import play.libs.F.Promise;
import play.modules.paginate.ModelPaginator;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import world.World;

public class Admin extends Controller {

	@Before
	public static void checkAdmin() {
		if(!Application.isAdmin()) {
			Logger.warn("Illegal attemps to access Admin area");
			Controller.notFound();
		} else {
			Logger.info("Admin action performed");
		}
	}
	
	/**
	 * Show the index page of the admin section, with various stats.
	 */
	public static void index() {
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
		// render
		render(tables, tableCount, tableSize, startedSince, uptime);
	}
	//--------------------------------------------------------------------------------------------------------------
	//----------- Greeting
	//--------------------------------------------------------------------------------------------------------------
	
	/**
	 * PAGE: greeting
	 */
	public static void greetings() {
		List<Greeting> greetings = Greeting.all().fetch();
		render(greetings);
	}
	
	/**
	 * CRUD: add a new greeting
	 * @param message
	 * @param enable
	 */
	public static void addGreeting(String message, boolean enable) {
		Greeting greeting = new Greeting(message, enable).save();
		greetings();
	}
	
	/**
	 * CRUD: update greeting
	 * @param id
	 * @param message
	 * @param enable
	 */
	public static void updateGreeting(Long id, String message, boolean enable) {
		Greeting greeting = Greeting.findById(id);
		Controller.notFoundIfNull(greeting);
		greeting.message = message;
		greeting.enable = enable;
		greeting.save();
		greetings();
	}
	
	/**
	 * CRUD: delete greeting
	 * @param id
	 */
	public static void deleteGreeting(Long id) {
		Greeting greeting = Greeting.findById(id);
		Controller.notFoundIfNull(greeting);
		greeting.delete();
		greetings();
	}
	
	//--------------------------------------------------------------------------------------------------------------
	//----------- ADMIN MESSAGES
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * PAGE: admin messages
	 */
	public static void messages() {
		List<AdminMessage> adminMessages = AdminMessage.find("order by dateEnd desc").fetch();
		render(adminMessages);
	}

	/**
	 * CRUD : add a new Admin Message
	 * @param message
	 * @param dateStart
	 * @param dateEnd
	 */
	public static void addMessage(String message, Date dateStart, Date dateEnd) {
		AdminMessage am = new AdminMessage(message, dateStart, dateEnd).save();
		messages();
	}

	/**
	 * CRUD : delete Admin Message
	 * @param id
	 */
	public static void deleteMessage(Long id) {
		AdminMessage am = AdminMessage.findById(id);
		Controller.notFoundIfNull(am);
		am.delete();
		messages();
	}

	/**
	 * CRUD : update Admin Message
	 * @param id
	 * @param message
	 * @param dateStart
	 * @param dateEnd
	 */
	public static void updateMessage(Long id, String message, Date dateStart, Date dateEnd) {
		AdminMessage am = AdminMessage.findById(id);
		Controller.notFoundIfNull(am);
		am.message = message;
		am.dateEnd = dateEnd;
		am.dateStart = dateStart;
		am.save();
		messages();
	}

	//--------------------------------------------------------------------------------------------------------------
	//----------- JOBS
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * Index page for all jobs, and links to launch those jobs
	 */
	public static void jobs() {
		render();
	}
	
	/**
	 * Progression page for a job identified by its UUID
	 * @param uuid
	 */
	public static void jobStatus(String uuid) {
		boolean done = false;
		int percent = 0;
		String current = "";
		String message = "";
		try {
			done 	= (Boolean) Cache.get("JobStatus_" + uuid + "_done");
			percent	= (Integer) Cache.get("JobStatus_" + uuid + "_percent");
			current	= (String)  Cache.get("JobStatus_" + uuid + "_current");
			message	= (String)  Cache.get("JobStatus_" + uuid + "_message");
		} catch(Exception e) {
			e.printStackTrace();
		}
		render(uuid, done, percent, current, message);
	}
	
	/**
	 * Calls a cronjob to reload the {@link World} from XML.
	 */
	public static void reloadWorld() {
		String uuid = Codec.UUID();
		Promise<String> p = new WorldLoader(uuid).now();
		jobStatus(uuid);
	}

	/**
	 * Backup database
	 */
	public static void backup() {
		Promise<String> p = new Backup().now();
		String result = "FAILURE";
		boolean done = false;
		try {
			result = p.get();
			done = p.isDone();
		} catch (Exception e) {
		} finally {
			render("Admin/job.html", done, result);
		}
	}

	public static void cleanMarket() {
		String uuid = Codec.UUID();
		Promise<String> p = new MarketCleaner(uuid).now();
		jobStatus(uuid);
	}

	public static void updateWorld() {
		String uuid = Codec.UUID();
		Promise<String> p = new WorldUpdater(uuid).now();
		jobStatus(uuid);
	}

	public static void updateGuilds() {
		String uuid = Codec.UUID();
		Promise<String> p = new GuildUpdater(uuid).now();
		jobStatus(uuid);
	}
	
	public static void takeSnapshots() {
		String uuid = Codec.UUID();
		Promise<String> p = new SnapshotTaker(uuid).now();
		jobStatus(uuid);
	}
	
	public static void cleanSnapshots() {
		String uuid = Codec.UUID();
		Promise<String> p = new SnapshotCleaner(uuid).now();
		jobStatus(uuid);
	}

	//--------------------------------------------------------------------------------------------------------------
	//----------- IRC BOTS
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * PAGE : IRC Bots
	 */
	public static void ircbots() {
		List<IrcBot> bots = IrcBot.all().fetch();
		render(bots);
	}

	/**
	 * CRUD : add a new IRC Bot
	 * @param nick
	 * @param hostname
	 * @param channel
	 * @param type
	 */
	public static void addBot(String nick, String password, String hostname, String channel, String type) {
		IrcBot bot = new IrcBot(nick, password, hostname, channel, type).save();
		ircbots();
	}

	/**
	 * CRUD : update IRC Bot
	 * @param id
	 * @param nick
	 * @param hostname
	 * @param channel
	 */
	public static void updateBot(Long id, String nick, String password, String hostname, String channel) {
		IrcBot bot = IrcBot.findById(id);
		boolean started = bot.isStarted();
		if(started)
			bot.stop();
		bot.nick = nick;
		bot.password = password;
		bot.hostname = hostname;
		bot.channel = channel;
		bot.save();
		if(started)
			bot.start();
		ircbots();
	}

	/**
	 * CRUD : delete IRC Bot
	 * @param id
	 */
	public static void deleteBot(Long id) {
		IrcBot bot = IrcBot.findById(id);
		if(bot.isStarted())
			bot.stop();
		bot.delete();
		ircbots();
	}

	/**
	 * ACTION: start a stopped bot, stop a started bot
	 * @param id
	 */
	public static void switchBot(Long id) {
		IrcBot bot = IrcBot.findById(id);
		if(bot.isStarted())
			bot.stop();
		else
			bot.start();
		ircbots();
	}
	
	//--------------------------------------------------------------------------------------------------------------
	//----------- TESTS
	//--------------------------------------------------------------------------------------------------------------

	public static void tests() {
		render();
	}
	
	public static void mailTest() {
		AdminMailers.test();
		flash.put("info", "Test mail sent!");
		tests();
	}
	
	public static void exceptionTest() {
		String[] s = {"hello", "world"};
		flash.put("info", "Exception test thrown!");
		System.out.println(s[5]); // this will throw an IndexOutOfBoundsException
		tests();
	}
	
	public static void activityReportTest() {
		AdminMailers.activityReport();
		flash.put("info", "Activity report sent by mail!");
		tests();
	}
	
	//--------------------------------------------------------------------------------------------------------------
	//----------- SAFE DELETE
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * Display the form for safely deleting database records.
	 */
	public static void safeDeleteForm() {
		render();
	}
	
	public static void safeDeleteAction(String table, Long id, boolean force) {
		if(table.equals("player"))
			if(force)
				Player.delete("id = ?", id);
			else
				playerDelete(id);
		else if(table.equals("playertimer"))
			if(force)
				PlayerTimer.delete("id = ?", id);
			else
				playerTimerDelete(id);
		else if(table.equals("building"))
			Building.delete("id = ?", id);
		
		// redirect
		safeDeleteForm();
	}
	
	//--------------------------------------------------------------------------------------------------------------
	//----------- DATABASE CRUD : PLAYERS
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * List of players
	 */
	public static void players() {
        ModelPaginator<Player> players = new ModelPaginator(Player.class).orderBy("name asc");
        players.setPageSize(50);
        render(players);
	}

	/**
	 * Find single player bu Id
	 * @param id
	 */
	public static void player(Long id) {
		Player player = Player.findById(id);
		if(player == null) {
			flash.error("Record <b>%s</b> not found", id);
			players();
		}
		render(player);
	}

	/**
	 * Find single player by name (search form)
	 * @param playerName
	 */
	public static void playerName(String playerName) {
		Player player = Player.find(playerName);
		if(player == null) {
			flash.error("Record <b>%s</b> not found", playerName);
			players();
		}
		render("Admin/player.html", player);
	}

	/**
	 * Delete player record
	 * @param id
	 */
	public static void playerDelete(Long id) {
		Player player = Player.findById(id);
		if(player == null) {
			flash.error("Record <b>%s</b> not found", id);
			players();
		}
		try {
			// If has buildings, delete all buildings
			if(player.buildings.size() > 0) {
				ArrayList<Long> ids = new ArrayList<Long>();
		        for(Building b : player.buildings)
		        	ids.add(b.id);
		        for(Long i : ids)
		        	player.deleteBuilding(i);
			}
			// delete player
			player.delete();
			flash.success("Record <b>%s</b> deleted", id);
		} catch(Exception e) {
			flash.error("Record <b>?</b> could not be deleted", id);
		} finally {
			players();
		}
	}

	/**
	 * Set or reset player login
	 * @param playerId
	 * @param login
	 */
	public static void playerSetLogin(Long playerId, String login) {
		Player player = Player.findById(playerId);
		if(player == null) {
			flash.error("Record <b>%s</b> not found", playerId);
			players();
		}
		try {
			player.name = login;
			player.save();
			flash.success("Record <b>%s</b> updated", player.name);
		} catch(Exception e) {
			flash.error("Record <b>?</b> could not be updated", player.name);
		} finally {
			player(playerId);
		}
	}

	/**
	 * Set or reset player password
	 * @param playerId
	 * @param password
	 */
	public static void playerSetPassword(Long playerId, String password) {
		Player player = Player.findById(playerId);
		if(player == null) {
			flash.error("Record <b>%s</b> not found", playerId);
			players();
		}
		try {
			player.preferences.PASSWORD_HASH = PlayerPreferences.hashPassword(password);
			player.preferences.save();
			player.save();
			flash.success("Record <b>%s</b> updated", player.name);
		} catch(Exception e) {
			flash.error("Record <b>?</b> could not be updated", player.name);
		} finally {
			player(playerId);
		}
	}
	
	//--------------------------------------------------------------------------------------------------------------
	//----------- DATABASE CRUD : PLAYERTIMERS
	//--------------------------------------------------------------------------------------------------------------
	
	/**
	 * List of ALL playerTimers, for ALL players
	 */
	public static void playerTimers() {
        ModelPaginator<PlayerTimer> timers = new ModelPaginator(PlayerTimer.class).orderBy("id desc");
        timers.setPageSize(50);
        render(timers);
	}
	
	/**
	 * List of playerTimers for the given player
	 * @param player
	 */
	public static void playerTimersList(Long id) {
		Player player = Player.findById(id);
        List<PlayerTimer> timers = player.timers;
        render("Admin/playerTimers.html", timers);
	}
	
	/**
	 * Unlink the given PlayerTimer from its player owner, and deletes it from the DB.
	 * @param id
	 */
	public static void playerTimerDelete(Long id) {
		PlayerTimer t = PlayerTimer.findById(id);
		t.player.timers.remove(t);
		t.player.save();
		t.delete();
		playerTimers();
	}
	
}
