package controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.Duration;
import org.joda.time.Period;

import models.adventures.Adventure;
import models.players.Player;
import models.players.PlayerPreferences;
import models.realm.Building;
import models.realm.snapshots.RealmSnapshot;
import play.mvc.Controller;
import world.World;
import world.WorldAvatar;

public class Preferences extends Controller {
	
	/**
	 * Update a preference with the given value, for the given user.
	 * @param playerName
	 * @param key
	 * @param value
	 */
	public static void update(String playerName, String key, String value) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Create preferences if they don't exist
		if(player.preferences == null) {
			PlayerPreferences p = new PlayerPreferences().save();
			player.preferences = p;
			player.save();
		}		
		// Update the given preference
		boolean done = false;
		
		if(key.equals("password")) { // special treatment for passwords
			player.preferences.PASSWORD_HASH = PlayerPreferences.hashPassword(value);
			done = true;
		}
		else
			done = player.preferences.updatePreference(key, value);	
		
		if(done)
			player.preferences.save();
		// Render (AJAX)
		if(done)
			renderTemplate("Preferences/update/_success.html");
		else
			renderTemplate("Preferences/update/_failure.html");
	}
	
	/**
	 * Index Page
	 * @param playerName
	 */
	public static void index(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		render(player);
	}
	
	/**
	 * Account & Security Preferences
	 * @param playerName
	 */
	public static void account(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		renderTemplate("Preferences/account/security.html",player);
	}
	
	/**
	 * Avatar Preferences
	 * @param playerName
	 */
	public static void avatar(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Get Data
        WorldAvatar avatars[] = new WorldAvatar[World.AVATARS.size()];
        avatars = World.AVATARS.values().toArray(avatars);
        Arrays.sort(avatars, WorldAvatar.BY_NAME);
		// render
		renderTemplate("Preferences/account/avatar.html", player, avatars);
	}
	
	/**
	 * Global display preferences
	 * @param playerName
	 */
	public static void display(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		renderTemplate("Preferences/display/index.html", player);
	}
	
	/**
	 * Realm display preferences
	 * @param playerName
	 */
	public static void realm(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		renderTemplate("Preferences/realm/index.html", player);
	}
	
	/**
	 * Cycles preferences
	 * @param playerName
	 */
	public static void cycles(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Get current values
		Duration pcd = new Duration(player.cycleTime * 1000);
		Period pc = pcd.toPeriod();
		// render
		renderTemplate("Preferences/cycles/index.html", player, pc);
	}
	
	/**
	 * Timers display & bahevior preferences
	 * @param playerName
	 */
	public static void timers(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		renderTemplate("Preferences/timers/index.html", player);
	}
	
	/**
	 * Data Export page
	 * @param playerName
	 */
	public static void export(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		renderTemplate("Preferences/data/export.html", player);
	}
	
	/**
	 * Data Purge page
	 * @param playerName
	 */
	public static void purge(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// render
		renderTemplate("Preferences/data/purge.html", player);
	}
	
	/**
	 * Saves the timers reminders overlay in the session
	 * @param x : position from right
	 * @param y : position from bottom
	 * @param h : height
	 * @param w : width
	 */
	public static void setOverlayPosition(int x, int y, int h, int w) {
		Controller.session.put("overlay-x", x);
		Controller.session.put("overlay-y", y);
		Controller.session.put("overlay-h", h);
		Controller.session.put("overlay-w", w);
	}
	
	/**
	 * Get timers reminders overlay position from session
	 * @param key, can be : x, y, h, w (see setOverlayPosition method)
	 * @return String
	 */
	public static String getOverlayPosition(String key) {
		if(key.equals("x"))
			if(Controller.session.contains("overlay-x"))
				return Controller.session.get("overlay-x")+"px";
			else
				return "0px";
		else if(key.equals("y"))
			if(Controller.session.contains("overlay-y"))
				return Controller.session.get("overlay-y")+"px";
			else
				return "0px";
		else if(key.equals("h"))
			if(Controller.session.contains("overlay-h"))
				return Controller.session.get("overlay-h")+"px";
			else
				return "20%";
		else if(key.equals("w"))
			if(Controller.session.contains("overlay-w"))
				return Controller.session.get("overlay-w")+"px";
			else
				return "20%";
		return "";
	}
	
	/**
	 * Reset overlay position to default
	 */
	public static void resetOverlayPosition() {
		Controller.session.remove("overlay-x");
		Controller.session.remove("overlay-y");
		Controller.session.remove("overlay-h");
		Controller.session.remove("overlay-w");
		Preferences.timers(Application.getSessionLogin());
	}
	
	/**
	 * Delete all building in this realm
	 * @param playerName
	 */
	public static void purgeRealm(String playerName) {
		Player p = Player.find(playerName);
		// Security
		Controller.notFoundIfNull(p);
        if (!Application.checkauth(p))
            return; // AUTH
        // We must use a trick here to avoid throwing a java.util.ConcurrentModificationException
        // First get the ids, then close the transaction, and finally proceed with thoses ids.
        ArrayList<Long> ids = new ArrayList<Long>();
        for(Building b : p.buildings)
        	ids.add(b.id);
        for(Long id : ids)
        	p.deleteBuilding(id);
	}
	
	/**
	 * Delete all snapshots of this realm
	 * @param playerName
	 */
	public static void purgeSnapshots(String playerName) {
		Player p = Player.find(playerName);
		// Security
		Controller.notFoundIfNull(p);
        if (!Application.checkauth(p))
            return; // AUTH
        // We must use a trick here to avoid throwing a java.util.ConcurrentModificationException
        // First get the ids, then close the transaction, and finally proceed with thoses ids.
        ArrayList<Long> ids = new ArrayList<Long>();
        for(RealmSnapshot s : p.snapshots) {
        	ids.add(s.id);
        }
        p.snapshots.clear();
        p.snapshots = null;
        p.save();
        for(Long id : ids) {
        	RealmSnapshot s = RealmSnapshot.findById(id);
        	s.delete();
        }
	}
	
	/**
	 * Delete all adventures for the given player
	 * @param playerName
	 */
	public static void purgeAdventures(String playerName) {
		Player p = Player.find(playerName);
		// Security
		Controller.notFoundIfNull(p);
        if (!Application.checkauth(p))
            return; // AUTH
        // We must use a trick here to avoid throwing a java.util.ConcurrentModificationException
        // First get the ids, then close the transaction, and finally proceed with thoses ids.
        ArrayList<Long> ids = new ArrayList<Long>();
        for(Adventure a : p.adventures)
        	ids.add(a.id);
        for(Long id : ids)
        	p.deleteAdventure(id);
	}
	
}
