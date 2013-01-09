package controllers;

import java.util.Collections;
import java.util.List;

import models.players.Player;
import models.players.PlayerAction;
import models.realm.snapshots.RealmSnapshot;
import play.data.validation.Required;
import play.mvc.Controller;

public class Snapshots extends Controller {

//##################################################################################################
//##### HTML Pages
//##################################################################################################

	/**
	 * Index page for managing a player's snapshots.
	 * @param playerName
	 */
	public static void index(String playerName) {
		// Load player
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
    	
    	// Load player's snapshots
    	List<RealmSnapshot> history = player.snapshots;
    	Collections.sort(history, RealmSnapshot.BY_DATE_DESC);
		
		renderTemplate("Realm/snapshots/list/index.html", player, history);
	}
	
//##################################################################################################
//##### ACTIONS
//##################################################################################################
	
	/**
	 * Toggle snapshot's lock state : lock it if it's unlocked, unlock it if it's locked.
	 * This can only be done by the player owner of the snapshot.
	 * @param snapshotId
	 */
	public static void toggleLock(@Required Long snapshotId) {
		// Load snapshot
		RealmSnapshot snap = RealmSnapshot.findById(snapshotId);
		Controller.notFoundIfNull(snap);
		// Security : current user must be auth & own that snapshot
		Player currentPlayer = Player.find(Application.getSessionLogin());	
		if(currentPlayer == null || !Application.isAuth(currentPlayer) || !snap.player.name.equals(currentPlayer.name)) {			
			Application.index(); // redirect
		}
		// Now, toggle snapshot state
		if(snap.locked) {
			snap.locked = false;
			snap.player.logAction(PlayerAction.LOG_SNAPSHOT_UNLOCKED, snap.toString()); // LOG
		} else {
			snap.locked = true;
			snap.player.logAction(PlayerAction.LOG_SNAPSHOT_LOCKED, snap.toString()); // LOG
		}
		snap.save();
		// redirect to snapshots list
		Snapshots.index(snap.player.name);
	}
}
