package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import models.admin.Greeting;
import models.adventures.Adventure;
import models.adventures.AdventureComment;
import models.guilds.Guild;
import models.players.Player;
import models.players.PlayerAction;
import models.realm.Building;
import play.modules.paginate.ModelPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import world.Tip;

@With(Logs.class)
public class Home extends Controller {

//##########################################################################################################
//####################  STATIC HTML PAGES  #################################################################
//########################################################################################################## 

	/**
	 * Profile Home Page (the user lands here)
	 * @param playerName
	 */
	public static void index(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Random tip
		Tip tip = Tip.getRandomTip();
		// Admin greeting
		List<Greeting> greetings = Greeting.all().fetch(); 
		// render
		renderTemplate("Home/index.html", player, tip, greetings);
	}
	
	/**
	 * Shows the "Nota bene" page
	 * @param playerName
	 */
	public static void notabene(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
		boolean contenteditable = false;
		if(player.name.equals(controllers.Application.getSessionLogin()) && controllers.Application.isAuth(player.name))
			contenteditable = true;
		// render
		renderTemplate("Home/notabene/index.html", player, contenteditable);
	}
	
	/**
	 * Statistics about the account.
	 * <br />Last comments, adventures, buildings added, etc.
	 * @param playerName
	 */
	public static void stats(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
        // Last adventures comments
        List<AdventureComment> lastComments = AdventureComment.find("author = ? order by publishedAt desc", player.name).fetch(5);
        // Last buildings
        List<Building> lastBuildings = new ArrayList<Building>();
        if(player.preferences != null && player.preferences.HOME_SHOW_LAST_BUILDINGS) {
            lastBuildings = player.buildings;
            Collections.sort(lastBuildings, Building.BY_ID_DESC);
            if (player.buildings.size() > 19)
                lastBuildings = lastBuildings.subList(0, 20);
        }
        // Last & next adventures
        List<Adventure> allAdventures = new ArrayList<Adventure>();
        List<Adventure> lastAdventures = new ArrayList<Adventure>();
        List<Adventure> nextAdventures = new ArrayList<Adventure>();
        if(player.preferences != null && ( player.preferences.HOME_SHOW_LAST_ADVENTURES || player.preferences.HOME_SHOW_NEXT_ADVENTURES )) {
	        allAdventures.addAll(player.adventures);
	        allAdventures.addAll(player.participations);
	        Collections.sort(allAdventures, Adventure.BY_DATE_END_DESC);
	        DateTime now = new DateTime();
	        Iterator<Adventure> it = allAdventures.iterator();
	    	int maxLast = 5;
	    	int count = 0;
	        while(it.hasNext()) {
	        	Adventure adv = it.next();
	        	DateTime start = new DateTime(adv.dateStart);
	        	DateTime end = new DateTime(adv.dateEnd);
	        	if(end.isBefore(now) && count < maxLast) {
	        		lastAdventures.add(adv);
	        		count++;
	        	}
	        	if(start.isAfter(now)) {
	        		nextAdventures.add(adv);
	        	}
	        }
        }
        // Counts
        int commentsCount = lastComments.size();
    	int buildingsCount = player.buildings.size();
        int adventuresCount = player.adventures.size();
        int adventuresCountInvited = player.participations.size();
		// render
		renderTemplate("Home/stats/index.html", player, buildingsCount, adventuresCount, adventuresCountInvited, commentsCount, lastBuildings, lastAdventures, lastComments, nextAdventures);
	}
	
	/**
	 * Own Activity list
	 * @param playerName
	 */
	public static void activity(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
        ModelPaginator<PlayerAction> actions = new ModelPaginator(PlayerAction.class, "player_id=?", player.id).orderBy("at desc");
        actions.setPageSize(50);
		// render
		renderTemplate("Home/activity/index.html", player, actions);
	}
	
	/**
	 * Friends managements & activity feeds
	 * @param playerName
	 */
	public static void friends(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
        // Friends & Friends of
        List<Player> friends = player.friends;
        List<Player> friendOf = player.getFriendOf();
        Collections.sort(friends, Player.BY_NAME);
        Collections.sort(friendOf, Player.BY_NAME);
		// Friends Actions List
        List<PlayerAction> values = player.getFriendsActivity();
        Collections.sort(values, PlayerAction.BY_ID_DESC);
        ValuePaginator<PlayerAction> actions = new ValuePaginator<PlayerAction>(values);
		// render
		renderTemplate("Home/friends/index.html", player, actions, friends, friendOf);
	}
	
	/**
	 * Show the guild tools (create a new one, join an existing one)
	 * @param playerName
	 */
	public static void guild(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
		List<Guild> guilds = Guild.findAll();
        Collections.sort(guilds, Guild.BY_NAME);
		// render
		renderTemplate("Home/guild/index.html", player, guilds);
	}
	
	/**
	 * Dev log page
	 * @param playerName
	 */
	public static void changelog(String playerName) {
		// Security
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
		
		// render
		renderTemplate("Home/changelog/index.html", player);
	}
	
//##########################################################################################################
//####################    AJAX ACTIONS    ##################################################################
//########################################################################################################## 	
	
	/**
	 * Add a new friend
	 * @param playerName
	 * @param friendName
	 */
	public static void addFriend(String playerName, String friendName) {
        // Get Player
        Player player = Player.find(playerName);
        Controller.notFoundIfNull(player);
        // Add friend
        Player friend = player.addFriend(friendName);

        // Success
        List<Player> friends = player.friends;
        renderTemplate("Home/friends/_list.html", friends);
    }

	/**
	 * Remove a friend
	 * @param playerName
	 * @param friendName
	 */
    public static void removeFriend(String playerName, String friendName) {
        // Get Player
        Player player = Player.find(playerName);
        Controller.notFoundIfNull(player);
        // Remove friend
        player.removeFriend(friendName);

        // Success
        List<Player> friends = player.friends;
        renderTemplate("Home/friends/_list.html", friends);
    }
    
    /**
     * Updates player's NotaBene (contenteditable field)
     * @param playerName
     * @param content
     */
    public static void updatePlayerNote(String playerName, String content) {
        Player player = Player.find(playerName);
        Controller.notFoundIfNull(player);

        if (!Application.checkauth(player))
            Controller.notFound();

        player.updateNote(content);
        renderText(content);
    }
}
