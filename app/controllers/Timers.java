package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import models.players.Player;
import models.players.PlayerAction;
import models.players.PlayerTimer;
import models.realm.Building;

import play.Logger;
import play.data.validation.InFuture;
import play.data.validation.Min;
import play.data.validation.Range;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.With;

@With(Logs.class)
public class Timers extends Controller {

//##########################################################################################################
//####################  STATIC HTML PAGES  #################################################################
//########################################################################################################## 
	
	/**
	 * Index page showing the list of the player timers
	 * @param playerName
	 */
    public static void index(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);

    	//List<PlayerTimer> durationTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DURATION);
    	//List<PlayerTimer> dateTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DATE);
    	//List<PlayerTimer> buildingTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_BUILDING);
    	
    	List<PlayerTimer> timers = player.timers;
    	Collections.sort(timers, PlayerTimer.BY_END_DATE_ASC);
    	
    	render(player, timers);
    }
    
    public static void overlay(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);    	
    	List<PlayerTimer> timers = new ArrayList<PlayerTimer>();
    	List<PlayerTimer> durationTimers;
    	List<PlayerTimer> dateTimers;
    	List<PlayerTimer> buildingsTimers;
    	if(player.preferences.TIMERS_SHOW_ON_HOME_TYPE1) {
    		durationTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DURATION);
    		timers.addAll(durationTimers);
    	}
    	if(player.preferences.TIMERS_SHOW_ON_HOME_TYPE2) {
    		dateTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DATE);
    		timers.addAll(dateTimers);
    	}
    	if(player.preferences.TIMERS_SHOW_ON_HOME_TYPE3) {
    		buildingsTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_BUILDING);
    		timers.addAll(buildingsTimers);
    	}
    	Collections.sort(timers, PlayerTimer.BY_END_DATE_ASC);
    	render("Timers/overlay/index.html", player, timers);
    }
    
    public static void overlayRefresh(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);    	
    	List<PlayerTimer> timers = new ArrayList<PlayerTimer>();
    	List<PlayerTimer> durationTimers;
    	List<PlayerTimer> dateTimers;
    	List<PlayerTimer> buildingsTimers;
    	if(player.preferences.TIMERS_SHOW_ON_HOME_TYPE1) {
    		durationTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DURATION);
    		timers.addAll(durationTimers);
    	}
    	if(player.preferences.TIMERS_SHOW_ON_HOME_TYPE2) {
    		dateTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DATE);
    		timers.addAll(dateTimers);
    	}
    	if(player.preferences.TIMERS_SHOW_ON_HOME_TYPE3) {
    		buildingsTimers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_BUILDING);
    		timers.addAll(buildingsTimers);
    	}
    	Collections.sort(timers, PlayerTimer.BY_END_DATE_ASC);
    	render("Timers/overlay/content.html", player, timers);
    }
    
	public static boolean showOverlay(Player p) {
		try {
			if(p == null && p.preferences == null)
				return false;
			if(PlayerTimer.getTimersByType(p, PlayerTimer.TIMER_ON_DURATION).size() > 0 && p.preferences.TIMERS_SHOW_ON_HOME_TYPE1)
				return true;
			if(PlayerTimer.getTimersByType(p, PlayerTimer.TIMER_ON_DATE).size() > 0 && p.preferences.TIMERS_SHOW_ON_HOME_TYPE2)
				return true;
			if(PlayerTimer.getTimersByType(p, PlayerTimer.TIMER_ON_BUILDING).size() > 0 && p.preferences.TIMERS_SHOW_ON_HOME_TYPE3)
				return true;
			return false;
		} catch(Exception e) {
			return false;
		}
	}

//##########################################################################################################
//#################### AJAX LOADED PARTIALS ################################################################
//########################################################################################################## 
    
    /**
     * List of timers (AJAX call)
     * This method is called by the auto-refresh AJAX functions.
     * @param playerName
     * @param timerType: Accepted values are 0, 1, 2, 3. See {@link PlayerTimer} fields : TIMER_ON_DURATION (1) ; TIMER_ON_DATE (2) ; TIMER_ON_BUILDING (3) ; all the timers (0).
     */
    public static void list(String playerName, int timerType) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	List<PlayerTimer> timers;
    	if(timerType == PlayerTimer.TIMER_ON_DURATION)
    		timers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DURATION);
    	else if(timerType == PlayerTimer.TIMER_ON_DATE)
    		timers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_DATE);
    	else if(timerType == PlayerTimer.TIMER_ON_BUILDING)
    		timers = PlayerTimer.getTimersByType(player, PlayerTimer.TIMER_ON_BUILDING);
    	else if(timerType == 0) {
    		timers = player.timers;
    		Collections.sort(timers, PlayerTimer.BY_END_DATE_ASC);
    	}
    	else
    		return;
    	renderTemplate("Timers/_list.html", timers);
    }
    
//##########################################################################################################
//#################### DISPLAY HTML FORMS ##################################################################
//##########################################################################################################

    /**
     * Display the FORM to add a new Timer based on DURATION
     * @param playerName
     */
    public static void addOnDurationForm(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// render
    	renderTemplate("Timers/forms/onDuration.html", player);
    }
    
    /**
     * Display the FORM to add a new Timer based on an expiration DATE
     * @param playerName
     */
    public static void addOnDateForm(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// render
    	renderTemplate("Timers/forms/onDate.html", player);
    }
    
    /**
     * Display the FORM to add a new Timer based on a BUILDING DEPOSIT depletion
     * @param playerName
     */
    public static void addOnBuildingForm(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Buildings that do expire
    	List<Building> buildings = new ArrayList<Building>();
    	if(player.buildings.size() > 0) {
    		Iterator<Building> it = player.buildings.iterator();
    		while(it.hasNext()) {
    			Building b = it.next();
    			if(b.doesExpire())
    				buildings.add(b);
    		}
    		Collections.sort(buildings, Building.BY_NAME);
    	}
    	// render
    	renderTemplate("Timers/forms/onBuilding.html", player, buildings);
    }
    
//##########################################################################################################
//#################### FORMS SUBMIT ACTIONS ################################################################
//########################################################################################################## 
    /**
     * Add a new Timer based on a DURATION
     * @param playerName
     * @param name
     * @param remind
     * @param startAt
     * @param hours
     * @param mins
     */
    public static void addTimerWithDuration(@Required String playerName, @Required String name, @Required boolean remind, @Required Date startAt, @Required @Min(0) int hours, @Required @Range(min = 0, max = 60) int mins) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
        if (!Application.checkauth(player))
            return; // AUTH
        // Validation
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
        	int duration = hours * 3600 + mins * 60;
        	PlayerTimer pt = new PlayerTimer(player, name, startAt, duration);
        	pt.startAt = startAt;
        	pt.remind = remind;
        	pt.type = PlayerTimer.TIMER_ON_DURATION;
        	pt.save();
        	player.timers.add(pt);
        	player.save();
            // Log & Flash
            flash.success(Messages.get("flash.success.timer.added", pt.name));
            player.logAction(PlayerAction.LOG_TIMERS_ADD, "" + pt.id); // LOG
        }
        // render
    	index(player.name);
    }

    /**
     * Add a new timer based on an EXPIRATION DATE
     * @param playerName
     * @param name
     * @param remind
     * @param startAt
     * @param endAt
     */
    public static void addTimerWithExpiration(@Required String playerName, @Required String name, @Required boolean remind, @Required Date startAt, @Required @InFuture Date endAt) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
        if (!Application.checkauth(player))
            return; // AUTH
        // Validation
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
            // Do the job
        	PlayerTimer pt = new PlayerTimer(player, name, startAt, endAt);
        	pt.remind = remind;
        	pt.type = PlayerTimer.TIMER_ON_DATE;
        	pt.save();
        	player.timers.add(pt);
        	player.save();
            // Log & Flash
            flash.success(Messages.get("flash.success.timer.added", pt.name));
            player.logAction(PlayerAction.LOG_TIMERS_ADD, "" + pt.id); // LOG
        }
        // render
    	index(player.name);
    }

    /**
     * Add a new timer based on a BUILDING DEPOSIT
     * @param playerName
     * @param remind
     * @param buildingId
     * @param remainingQuantity
     */
    public static void addTimerOnBuilding(@Required String playerName, @Required boolean remind, @Required Long buildingId, @Required @Min(0) int remainingQuantity) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
        if (!Application.checkauth(player))
            return; // AUTH
        // Validation
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
        	Building building = Building.findById(buildingId);
    		// A building can't have more than 1 timer on it
    		if(building.timer != null) {
    			flash.error(Messages.get("flash.error.timer.already.exist"));
    			index(player.name);
    			return;
    		}
        	building.setDepositQuantity(remainingQuantity);
    		// Set new timer
        	PlayerTimer pt = new PlayerTimer(player, building, remainingQuantity);
        	pt.remind = remind;
        	pt.type = PlayerTimer.TIMER_ON_BUILDING;
        	pt.save();
        	player.timers.add(pt);
        	player.save();
            // Log & Flash
            flash.success(Messages.get("flash.success.timer.added", pt.name));
            player.logAction(PlayerAction.LOG_TIMERS_ADD, "" + pt.id); // LOG
        }
        // render
    	index(player.name);
    }

    public static void addTimerWithDeposit(String playerName, Long buildingId, int remainingQuantity) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
        if (!Application.checkauth(player))
            return; // AUTH
        boolean remind = false;
    	if(player != null && player.preferences != null) {
    		remind = player.preferences.TIMERS_DEFAULT_REMIND_TYPE3;
    	}
    	Building building = Building.findById(buildingId);
		// A building can't have more than 1 timer on it
		if(building.timer != null) {
			flash.error(Messages.get("flash.error.timer.already.exist"));
			index(player.name);
			return;
		}
		// Set new timer
    	PlayerTimer pt = new PlayerTimer(player, building, remainingQuantity);
    	pt.remind = remind;
    	pt.type = PlayerTimer.TIMER_ON_BUILDING;
    	pt.save();
    	player.timers.add(pt);
    	player.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.timer.added", pt.name));
        player.logAction(PlayerAction.LOG_TIMERS_ADD, "" + pt.id); // LOG
        // render
    	Realm.index(player.name);
    }
    
    public static void deleteExpired(String playerName) {
    	Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
    	ArrayList<PlayerTimer> tobedeleted = new ArrayList();
    	Iterator<PlayerTimer> it = player.timers.iterator();
    	while(it.hasNext()) {
    		PlayerTimer pt = it.next();
    		if(pt != null && pt.hasExpired()) {
    			tobedeleted.add(pt);
    		}
    	}
    	// Second loop needed to avoid concurrent modification exception
    	Iterator<PlayerTimer> it2 = tobedeleted.iterator();
    	while(it2.hasNext()) {
    		PlayerTimer pt = it2.next();
			player.timers.remove(pt);
			pt.delete();
    	}
    	player.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.timer.cleaned"));
        player.logAction(PlayerAction.LOG_TIMERS_CLEAN, ""); // LOG
        // render
    	index(player.name);
    }

    public static void renewTimer(String playerName, Long timerId) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
        if (!Application.checkauth(player))
            return; // AUTH
    	PlayerTimer pt = PlayerTimer.findById(timerId);
    	Duration dur = pt.getDuration();
    	DateTime newStart = new DateTime(); // now
    	DateTime newEnd = newStart.plus(dur);
    	pt.startAt = newStart.toDate();
    	pt.endAt = newEnd.toDate();
    	pt.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.timer.renewed", pt.name));
        player.logAction(PlayerAction.LOG_TIMERS_RENEW, "" + pt.id); // LOG
        // render
    	index(playerName);
    }

    public static void deleteTimer(String playerName, Long timerId) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
        if (!Application.checkauth(player))
            return; // AUTH
        PlayerTimer pt = PlayerTimer.findById(timerId);
        if(player.timers.contains(pt)) {
        	player.timers.remove(pt);
        	pt.delete();
        	player.save();
            // Log & Flash
            flash.success(Messages.get("flash.success.timer.deleted"));
            player.logAction(PlayerAction.LOG_TIMERS_DELETE, ""); // LOG
        } else {
        	flash.error(Messages.get("flash.error.timer.deleted"));
        }
        // render
        index(playerName);
    }

}
