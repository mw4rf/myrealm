package models.players;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.adventures.Adventure;
import models.guilds.Guild;
import models.market.MarketAdventureSale;
import models.realm.Building;
import models.realm.BuildingsGroup;

import play.db.jpa.Model;
import play.mvc.Router;

@Entity
public class PlayerAction extends Model {

    public String name;
    public String details;
    public Date at;

    @ManyToOne
    public Player player;

    // names
    public static final String LOG_REALM_ADD_BUILDING = "log.realm.add_building";
    public static final String LOG_REALM_ADD_GROUP = "log.realm.group.add";
    public static final String LOG_REALM_EDIT_BUILDING = "log.realm.building.edit";
    public static final String LOG_REALM_EDIT_GROUP = "log.realm.group.edit";
    public static final String LOG_REALM_DELETE_BUILDING = "log.realm.delete_building";
    public static final String LOG_REALM_DELETE_GROUP = "log.realm.group.delete";
    public static final String LOG_REALM_DELETE_SIMULATED_BUILDINGS = "log.realm.delete_simulated_buildings";
    public static final String LOG_REALM_SET_CYCLETIME = "log.realm.set_cycletime";
    public static final String LOG_REALM_BUILDING_BOOST_OFF = "log.realm.building.boost_off";
    public static final String LOG_REALM_BUILDING_BOOST_ON = "log.realm.building.boost_on";
    public static final String LOG_REALM_BUILDING_DISABLE = "log.realm.building.disable";
    public static final String LOG_REALM_BUILDING_ENABLE = "log.realm.building.enable";
    public static final String LOG_REALM_BUILDING_LVLUP = "log.realm.building.lvlup";
    public static final String LOG_REALM_BUILDING_SET_PRODUCTION_TIME = "log.realm.building.set_production_time";
    public static final String LOG_REALM_BUILDING_SET_DEPOSIT_QUANTITY = "log.realm.building.set_deposit_quantity";
    public static final String LOG_REALM_BUILDING_SET_AREA = "log.realm.building.set.area";
    
    public static final String LOG_REALM_CALCULATION_HMT = "log.realm.calculation.hmt";
    public static final String LOG_REALM_CALCULATION_HMTS = "log.realm.calculation.hmts";
    public static final String LOG_REALM_CALCULATION_HMTFB = "log.realm.calculation.hmtfb";
    public static final String LOG_REALM_CALCULATION_RS = "log.realm.calculation.rs";
    
    public static final String LOG_ADVENTURES_ADD_ADVENTURE = "log.adventures.add_adventure";
    public static final String LOG_ADVENTURES_EDIT_ADVENTURE = "log.adventures.edit_adventure";
    public static final String LOG_ADVENTURES_DELETE_ADVENTURE = "log.adventures.delete_adventure";
    public static final String LOG_ADVENTURES_ADD_PARTICIPANT = "log.adventures.add_participant";
    public static final String LOG_ADVENTURES_REMOVE_PARTICIPANT = "log.adventures.remove_participant";
    public static final String LOG_ADVENTURES_JOIN = "log.adventures.join";
    public static final String LOG_ADVENTURES_LEAVE = "log.adventures.leave";
    public static final String LOG_ADVENTURE_ADD_COMMENT = "log.adventures.add_comment";
    public static final String LOG_ADVENTURES_EDIT_COMMENT = "log.adventures.edit_comment";

    public static final String LOG_FRIENDS_ADD = "log.friends.add";
    public static final String LOG_FRIENDS_REMOVE = "log.friends.remove";

    public static final String LOG_GUILD_JOIN = "log.guild.join";
    public static final String LOG_GUILD_LEAVE = "log.guild.leave";
    public static final String LOG_GUILD_CREATE = "log.guild.create";
    public static final String LOG_GUILD_ADD_WALLMESSAGE = "log.guild.wallmessage";

    public static final String LOG_TIMERS_ADD = "log.timers.add";
    public static final String LOG_TIMERS_RENEW = "log.timers.renew";
    public static final String LOG_TIMERS_CLEAN = "log.timers.clean";
    public static final String LOG_TIMERS_DELETE = "log.timers.delete";

    public static final String LOG_MARKET_ADVENTURE_ADD = "log.market.adventure.add";
    public static final String LOG_MARKET_ADVENTURE_BUY = "log.market.adventure.buy";
    public static final String LOG_MARKET_ADVENTURE_ACCEPT = "log.market.adventure.accept";
    public static final String LOG_MARKET_ADVENTURE_REJECT = "log.market.adventure.reject";
    public static final String LOG_MARKET_ADVENTURE_WITHDRAW = "log.market.adventure.withdraw";

    public static final String LOG_SNAPSHOT_TAKEN = "log.snapshot.taken";
    public static final String LOG_SNAPSHOT_LOCKED = "log.snapshot.locked";
    public static final String LOG_SNAPSHOT_UNLOCKED = "log.snapshot.unlocked";
    public static final String LOG_SNAPSHOT_CLEANUP = "log.snapshot.cleanup";

    public PlayerAction(Player player, String name, String details) {
        this.player = player;
        this.name = name;
        this.details = details;
        this.at = new Date();
    }

    @Override
    public String toString() {
    	return "[" + this.at + "] {" + player.name + " } => " + this.name + " " + this.details;
    }
    
    /**
     * Format action with default configuration : 
     * 	-	<b>useUrl</b> is <i>true</i>
     * 	-	<b>absoluteUrl</b> is <i>false</i>
     * @return
     */
    public String formatAction() {
    	return this.formatAction(true, false);
    }

    /**
     * Formats action, and returns a proper String. Some actions have a parameter, e.g. the row Id
     * of a building or player, or any other object. For those actions, the object is loaded
     * from the DB and some fields are used, e.g. name of the building.
     * @return formated String
     */
    public String formatAction(boolean useUrl, boolean absoluteUrl) {
        String action = this.name;
        String result = "";
        
        String absurl = "";
        if(absoluteUrl)
        	absurl = play.Play.configuration.get("application.address").toString();

        // BUILDINGS
        if(action.equals(LOG_REALM_ADD_BUILDING)
                || action.equals(LOG_REALM_EDIT_BUILDING)
                || action.equals(LOG_REALM_BUILDING_BOOST_OFF)
                || action.equals(LOG_REALM_BUILDING_BOOST_ON)
                || action.equals(LOG_REALM_BUILDING_DISABLE)
                || action.equals(LOG_REALM_BUILDING_ENABLE)
                || action.equals(LOG_REALM_BUILDING_LVLUP)
                || action.equals(LOG_REALM_BUILDING_SET_PRODUCTION_TIME)
        		|| action.equals(LOG_REALM_BUILDING_SET_DEPOSIT_QUANTITY)
        		|| action.equals(LOG_REALM_BUILDING_SET_AREA)) {
            Building b = Building.findById(Long.parseLong(this.details));
            if (b == null)
                return "";
            HashMap<String, Object> map = new HashMap();
            map.put("playerName", player.name);
            map.put("buildingId", b.id);
            String url = Router.reverse("Realm.showBuilding", map).url;
            if(useUrl)
            	result = "<a href=\"" + absurl + url + "\">" + b.name + "</a>";
            else
            	result = b.name;
            return result;
        }

        if (action.equals(LOG_REALM_DELETE_BUILDING)) {
            return details;
        }
        
        // GROUPS
        if(action.equals(LOG_REALM_ADD_GROUP)
        	|| action.equals(LOG_REALM_EDIT_GROUP)) {
            BuildingsGroup a = BuildingsGroup.findById(Long.parseLong(this.details));
            if (a == null)
                return "";
            result = a.name;
            return result;
        }
        
        if(action.equals(LOG_REALM_DELETE_GROUP)) {
        	return details;
        }

        // ADVENTURES
        if(action.equals(LOG_ADVENTURES_ADD_ADVENTURE)
 || action.equals(LOG_ADVENTURES_ADD_PARTICIPANT) || action.equals(LOG_ADVENTURES_REMOVE_PARTICIPANT)
 || action.equals(LOG_ADVENTURES_EDIT_ADVENTURE) || action.equals(LOG_ADVENTURES_JOIN) || action.equals(LOG_ADVENTURES_LEAVE)
                || action.equals(LOG_ADVENTURE_ADD_COMMENT) || action.equals(LOG_ADVENTURES_EDIT_COMMENT)) {
            Adventure a = Adventure.findById(Long.parseLong(this.details));
            if (a == null)
                return "";
            HashMap<String, Object> map = new HashMap();
            map.put("playerName", player.name);
            map.put("adventureId", a.id);
            String url = Router.reverse("Adventures.showAdventure", map).url;
            if(useUrl)
            	result = "<a href=\"" + absurl + url + "\">" + a.name + "</a>";
            else
            	result = a.name;
            return result;
        }

        if (action.equals(LOG_ADVENTURES_DELETE_ADVENTURE)) {
            return details;
        }

        // FRIENDS
        if (action.equals(LOG_FRIENDS_ADD) || action.equals(LOG_FRIENDS_REMOVE)) {
            Player friend = Player.find(this.details);
            HashMap<String, Object> map = new HashMap();
            map.put("playerName", friend.name);
            String url = Router.reverse("Application.home", map).url;
            if(useUrl)
            	result = "<a href=\"" + absurl + url + "\">" + friend.name + "</a>";
            else
            	result = friend.name;
            return result;
        }

        // TIMERS
        if (action.equals(LOG_TIMERS_ADD) || action.equals(LOG_TIMERS_RENEW)) {
        	PlayerTimer pt = PlayerTimer.findById(Long.parseLong(this.details));
        	if(pt != null) {
            	HashMap<String, Object> map = new HashMap();
                map.put("timerId", pt.id);
                map.put("playerName", pt.player.name);
                String url = Router.reverse("Timers.index", map).url;
                if(useUrl)
                	result = "<a href=\"" + absurl + url + "\">" + pt.name + "</a>";
                else
                	result = pt.name;
                return result;
        	}
        	else return details;
        }

        if(action.equals(LOG_TIMERS_CLEAN) || action.equals(LOG_TIMERS_DELETE)) {
        	return details;
        }

        // GUILDS
        if(action.equals(LOG_GUILD_JOIN) || action.equals(LOG_GUILD_LEAVE) || action.equals(LOG_GUILD_CREATE) || action.equals(LOG_GUILD_ADD_WALLMESSAGE)) {
        	Guild guild = Guild.findById(Long.parseLong(this.details));
        	HashMap<String, Object> map = new HashMap();
            map.put("guildTag", guild.tag);
            String url = Router.reverse("Guilds.home", map).url;
            if(useUrl)
            	result = "<a href=\"" + absurl + url + "\">" + "[" + guild.tag + "] " + guild.name + "</a>";
            else
            	result = "[" + guild.tag + "] " + guild.name;
            return result;
        }

        // MARKET (ADVENTURES)
        if(action.equals(LOG_MARKET_ADVENTURE_ACCEPT) || action.equals(LOG_MARKET_ADVENTURE_ADD) || action.equals(LOG_MARKET_ADVENTURE_BUY) || action.equals(LOG_MARKET_ADVENTURE_REJECT) || action.equals(LOG_MARKET_ADVENTURE_WITHDRAW)) {
        	MarketAdventureSale sale = MarketAdventureSale.findById(Long.parseLong(this.details));
        	HashMap<String, Object> map = new HashMap();
            map.put("id", sale.id);
            String url = Router.reverse("Market.details", map).url;
            if(useUrl)
            	result = "<a href=\"" + absurl + url + "\">" + sale.toString() + "</a>";
            else
            	result = sale.toString();
            return result;
        }
        
        // SNAPSHOTS
        if(action.equals(LOG_SNAPSHOT_LOCKED) || action.equals(LOG_SNAPSHOT_UNLOCKED) || action.equals(LOG_SNAPSHOT_TAKEN)){
        	return this.details;
        }
        
        // Other results (default)
        result = action;

        // return
        return result;
    }

    public static final Comparator<PlayerAction> BY_ID = new Comparator<PlayerAction>() {
        @Override
        public int compare(PlayerAction b1, PlayerAction b2) {
            return b1.id.compareTo(b2.id);
        }
    };

    public static final Comparator<PlayerAction> BY_ID_DESC = new Comparator<PlayerAction>() {
        @Override
        public int compare(PlayerAction b1, PlayerAction b2) {
            return b2.id.compareTo(b1.id);
        }
    };
}

