package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import notifiers.AdminMailers;
import notifiers.PlayerMailers;

import org.joda.time.DateTime;

import jobs.Backup;

import models.admin.AdminMessage;
import models.adventures.Adventure;
import models.adventures.AdventureComment;
import models.guilds.Guild;
import models.players.Player;
import models.players.PlayerAction;
import models.players.PlayerPreferences;
import models.players.PlayerTimer;
import models.realm.Building;
import play.Logger;
import play.exceptions.PlayException;
import play.i18n.Messages;
import play.libs.Crypto;
import play.modules.paginate.ModelPaginator;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http;
import play.mvc.With;
import serializers.PlayerJSonSerializer;
import serializers.PlayerWrapper;
import serializers.WorldBuildingSerializer;
import world.CustomStyle;
import world.Tip;
import world.World;
import world.WorldAdventure;
import world.WorldAvatar;
import world.WorldBuilding;

@With(Logs.class)
public class Application extends Controller {

	/**
	 * Application main page, displayed when calling the root URL.
	 * Shows a form to login or register.
	 */
    public static void index() {
        // If player is auth, redirect to his home page
        if (!Application.isAnonymous())
            if (Player.find(Application.getSessionLogin()) != null)
                Controller.redirect("Home.index", Application.getSessionLogin());
        // Else, render default index
        render();
    }
    
    /**
     * Public page rendering a RSS feed (XML output) for the player activity.
     * Rendirects to index page if 1) player doesn't exist ; 2) player has disabled the RSS feed in her preferences
     * @param playerName as {@link String}
     */
    public static void rss(String playerName) {
    	// Get player from DB
    	Player player = Player.find(playerName);
    	// Redirects if player doesn't exist
    	if(player == null)
    		index();
    	// Redirect if player has disabled the public RSS feed in her preferences
    	if(player.preferences == null || !player.preferences.ENABLE_ACTIVITY_FEED)
    		index();
    	// Proceed: get activity and render template
    	List<PlayerAction> actions = PlayerAction.find("player = ? order by at desc", player).fetch(20);
    	render("Application/activityFeed.rss", player, actions);
    }

    /**
     * Public list of {@link Player}
     */
    public static void playersList() {
        List<Player> players = Player.findAll();
        Collections.sort(players, Player.BY_NAME_IGNORE_CASE);
        render(players);
    }

    /**
     * Public list of {@link Adventure}
     */
    public static void adventuresList() {
        List<Adventure> adventures = Adventure.findAll();
        Collections.sort(adventures, Adventure.BY_ID_DESC);
        render(adventures);
    }
    
    @Deprecated
    public static void nextAdventuresList() {
    	Adventures.nextAdventuresList();
    }

    /**
     * Public list of {@link PlayerAction}
     */
    public static void actionsList() {
        //List<PlayerAction> actions = PlayerAction.find("order by at desc").fetch(1000);
        //Collections.sort(actions, PlayerAction.BY_ID_DESC);
        ModelPaginator<PlayerAction> actions = new ModelPaginator(PlayerAction.class).orderBy("at desc");
        actions.setPageSize(50);
        render(actions);
    }

    public static void playerActions(String playerName) {
    	Player player = Player.find(playerName);
    	if(player == null) {
    		index();
    		return;
    	}
        ModelPaginator<PlayerAction> actions = new ModelPaginator(PlayerAction.class, "player_id=?", player.id).orderBy("at desc");
        actions.setPageSize(50);
        render(player, actions);
    }

    public static String getLastAdminMessage() {
        try {
            return AdminMessage.getLastMessage().message;
        } catch (Exception e) {
            return "";
        }
    }
    

    /**
     * Action called to send an e-mail with an exception report, when an exception is thrown.
     * see views/errors/500.html and notifiers.AdminMailers.exceptionReport()
     * @param title 	The kind of Exception (e.g. "Execution exception" or "JPA/Hibernate Exception")
     * @param description 	The name of the Exception class (e.g. "NullPointerException")
     * @param source 	The source file throwing the Exception
     * @param line	The line in the source file throwing the Exception
     */
	public static void exceptionReport(String title, String description, String source, Integer line) {
		AdminMailers.exceptionReport(title, description, source, line);
		flash.success( "Error report sent!");
		render();
	}

    /**
     * FROM : index page
     * If player exists, login.
     * If player doesn't exist, REDIRECT to newPlayer
     * @param playerName
     */
    public static void login(String playerName) {
    	// Player name can't be empty
        if (playerName.isEmpty())
            Controller.redirect("Application.index");
        // Player name must be validated (no spaces, special characters, etc.)
        playerName = Player.validatePlayerName(playerName);
        // Check if player already exist
        Player p = Player.find(playerName);
        
        // if player doesn't exist, redirect to newPlayer
        if(p == null) {
        	newPlayer(playerName);
        	return;
        }
        
        // if player exist, proceed to login
        Controller.session.put("user", playerName);
        
        // First login of users created with add-friend command
        // they haven't got preferences yet !
        if(p.preferences == null) {
        	PlayerPreferences preferences = new PlayerPreferences().save();
        	p.preferences = preferences;
        	p.save();
        }
        
        // If security enabled, check password
        if (p.preferences.ENABLE_SECURITY) {
            if (Application.checkauth(p)) {
                Controller.redirect("Home.index", playerName);
                // Record last connection
                p.setLastConnection();
            }
        } else {
            flash.success(Messages.get("flash.success.welcome", playerName));
            Controller.redirect("Home.index", playerName);
            // Record last connection
            p.setLastConnection();
        }
    }

    /**
     * FROM : login action
     * Show the form to create a new player
     * @param playerName
     */
    public static void newPlayer(String playerName) {
        if (playerName.isEmpty())
            Controller.redirect("Application.index");
        render(playerName);
    }

    /**
     * FROM : newPlayer action
     * Create a new player, save it, REDIRECT to login action
     * @param playerName
     * @param password
     * @param remember
     */
    public static void addPlayer(String playerName, String password, boolean remember) {
        if (playerName.isEmpty())
            Controller.redirect("Application.index");
        // Don't create again an existing player
        Player pl = Player.find(Player.validatePlayerName(playerName));
        if(pl != null) {
        	Home.index(Player.validatePlayerName(playerName));
        	return;
        }

    	// Create player
        Player p = new Player(playerName);
        // Create preferences
        p.preferences = new PlayerPreferences();
        p.preferences.save();
        p.save();
        // Set password
        if(!password.isEmpty()) {
        	p.preferences.PASSWORD_HASH = p.preferences.hashPassword(password);
        	p.preferences.ENABLE_SECURITY = true;
            if(remember) {
            	p.preferences.AUTH_COOKIE = true;
            	Controller.response.setCookie("user", Crypto.encryptAES(p.name), "30d");
                Controller.response.setCookie("pwdhash", Crypto.encryptAES(p.preferences.PASSWORD_HASH), "30d");
                Controller.session.put("auth", true);
                Controller.session.put("pwdhash", p.preferences.PASSWORD_HASH);
            }
            p.preferences.save();
        }
        // flash & render
        flash.put("info", Messages.get("flash.info.new.user", playerName));
        login(p.name);
    }

    /**
     * Ask the user to identify, giving his password.
     * @param playerName
     */
    public static void askPassword(String playerName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		index();
    	else {
    		boolean recoverpwd = false;
    		if(player.preferences != null && player.preferences.hasMailAddress())
    			recoverpwd = true;
    		render(playerName, recoverpwd);
    	}
    }

    public static void password(String playerName, String password) {
        Player p = Player.find(playerName);
        String pwdhash = PlayerPreferences.hashPassword(password);
        if (p.preferences.PASSWORD_HASH.equals(pwdhash)) {
            // Save Session
            Controller.session.put("auth", true);
            Controller.session.put("pwdhash", pwdhash);
            // Save Cookie
            if (p.preferences.AUTH_COOKIE) {
                Controller.response.setCookie("user", Crypto.encryptAES(playerName), "30d");
                Controller.response.setCookie("pwdhash", Crypto.encryptAES(pwdhash), "30d");
            }
            // Record last connection
            p.setLastConnection();
            // Redirect user
            flash.success(Messages.get("flash.success.welcome", playerName));
            Controller.redirect("Home.index", playerName);
        } else {
            flash.error(Messages.get("flash.error.badpassword", playerName));
            index();
        }
    }
    
    /**
     * Send a new randomly-generated password to the given player, and redirect to password prompt page.
     * @param player
     */
    public static void sendNewPassword(String playerName) {
    	Player player = Player.find(playerName);
    	if(player == null || player.preferences == null || !player.preferences.hasMailAddress())
    		index();
    	// Create a new password
    	String password = PlayerPreferences.makeRandomPassword(10);
    	// Assign password to player
    	player.preferences.PASSWORD_HASH = PlayerPreferences.hashPassword(password);
    	player.preferences.save();
    	// Send mail
    	PlayerMailers.sendNewPassword(player, password);
    	// Flash & redirect
    	flash.put("info", Messages.get("password.send.new.done", player.preferences.EMAIL));
    	index();
    }

    public static void ajaxLogin(String playerName) {
        Player p = Player.find(playerName);
        // If player doesn't exist, create it
        if (p == null) {
            p = new Player(playerName);
            p.preferences = new PlayerPreferences();
            p.preferences.save();
            p.save();
            playerName = p.name; // validate player name
        }
        if (p.preferences == null) {
            p.preferences = new PlayerPreferences();
            p.preferences.save();
            p.save();
        }
        Controller.session.put("user", playerName);

        if (p.preferences.ENABLE_SECURITY) {
            // If cookie is set
            if (p.preferences.AUTH_COOKIE) {
                String usercookie;
                String pwdcookie;
                try {
                    usercookie = Crypto.decryptAES(Http.Request.current().cookies.get("user").value);
                    pwdcookie = Crypto.decryptAES(Http.Request.current().cookies.get("pwdhash").value);
                    Controller.session.put("auth", true);
                    Controller.session.put("user", usercookie);
                    Controller.session.put("pwdhash", pwdcookie);
                } catch (Exception e) {
                }
            }

            // If this is enough
            if (p.preferences.PASSWORD_HASH.equals(Controller.session.get("pwdhash")) && Boolean.parseBoolean(Controller.session.get("auth"))) {
                renderTemplate("Application/_ajaxAuthOk.html", p);
                // Record last connection
                p.setLastConnection();
            }
            // If we have to ask for a password
            else {
                renderTemplate("Application/_ajaxPasswordForm.html", p);
            }
        } else { // security not enabled
            renderTemplate("Application/_ajaxAuthOk.html", p);
            // Record last connection
            p.setLastConnection();
        }
    }

    public static void ajaxPassword(String playerName, String password) {
        Player p = Player.find(playerName);
        String pwdhash = PlayerPreferences.hashPassword(password);
        if (p.preferences.PASSWORD_HASH.equals(pwdhash)) {
            // Save Session
            Controller.session.put("auth", true);
            Controller.session.put("pwdhash", pwdhash);
            // Save Cookie
            if (p.preferences.AUTH_COOKIE) {
                Controller.response.setCookie("user", Crypto.encryptAES(playerName), "30d");
                Controller.response.setCookie("pwdhash", Crypto.encryptAES(pwdhash), "30d");
            }
            renderTemplate("Application/_ajaxAuthOk.html", p);
        } else {
            renderTemplate("Application/_ajaxAuthFailed.html", p);
        }
    }

    /**
     * User home page, i.e. where she gets redirected to after login.
     * @deprecated Redirects to Home.index, kept for compatibility with MyRealm 1 remains.
     * @param playerName
     */
    @Deprecated
    public static void home(String playerName) {
    	Home.index(playerName);
    }
    
    /**
     * Live search on {@link Player} name to populate JS dropdown search field.
     * Renders JSON object
     * @param playerName
     */
    public static void searchPlayer(String playerName) {
        List<PlayerWrapper> res = Player.search(playerName);
        renderJSON(res, PlayerJSonSerializer.get());
    }
    
    public static void searchBuilding(String buildingName) {
    	List<WorldBuilding> res = WorldBuilding.search(buildingName);
    	renderJSON(res, WorldBuildingSerializer.get());
    }

    public static void logout() {
        Controller.session.clear();
        Controller.response.removeCookie("user");
        Controller.response.removeCookie("pwdhash");
        flash.success(Messages.get("flash.success.logout"));
        index();
    }

    // -----------
    public static boolean isAuth(Player p) {
        if (p == null)
            return true;
        if (p.preferences == null)
            return true;
        if (!p.preferences.ENABLE_SECURITY)
            return true;
        if (p.preferences.PASSWORD_HASH.isEmpty())
            return true;

        if (p.preferences.PASSWORD_HASH.equals(Controller.session.get("pwdhash")) && Boolean.parseBoolean(Controller.session.get("auth")))
            return true;

        return false;
    }

    public static boolean isAuth(String playerName) {
        if (playerName == null || playerName.isEmpty())
            return false;

        return Application.isAuth(Player.find(playerName));
    }

    public static boolean isSecured(Player p) {
        if (p == null)
            return false;
        if (p.preferences == null)
            return false;
        if (!p.preferences.ENABLE_SECURITY)
            return false;
        if (p.preferences.PASSWORD_HASH.isEmpty())
            return false;

        if (p.preferences.AUTH_COOKIE) {
            String usercookie;
            String pwdcookie;
            try {
                usercookie = Crypto.decryptAES(Http.Request.current().cookies.get("user").value);
                pwdcookie = Crypto.decryptAES(Http.Request.current().cookies.get("pwdhash").value);
                Controller.session.put("auth", true);
                Controller.session.put("user", usercookie);
                Controller.session.put("pwdhash", pwdcookie);
            } catch (Exception e) {
            }
        }

        if (p.preferences.PASSWORD_HASH.equals(Controller.session.get("pwdhash")) && Boolean.parseBoolean(Controller.session.get("auth")))
            return true;

        return false;
    }

    public static boolean isSecured(String playerName) {
        if (playerName == null || playerName.isEmpty())
            return false;

        return Application.isSecured(Player.find(playerName));
    }

    public static boolean isAuthAndSecured(Player p) {
        if (Application.isAuth(p) && Application.isSecured(p))
            return true;
        else
            return false;
    }

    public static boolean isAuthAndSecured(String playerName) {
        if (playerName == null || playerName.isEmpty())
            return false;

        return Application.isAuthAndSecured(Player.find(playerName));
    }

    public static boolean checkauth(Player p) {
        if (p == null)
            return true;
        if (p.preferences == null)
            return true;
        if (!p.preferences.ENABLE_SECURITY)
            return true;
        if (p.preferences.PASSWORD_HASH.isEmpty())
            return true;

        if (p.preferences.AUTH_COOKIE) {
            String usercookie;
            String pwdcookie;
            try {
                usercookie = Crypto.decryptAES(Http.Request.current().cookies.get("user").value);
                pwdcookie = Crypto.decryptAES(Http.Request.current().cookies.get("pwdhash").value);
                Controller.session.put("auth", true);
                Controller.session.put("user", usercookie);
                Controller.session.put("pwdhash", pwdcookie);
            } catch (Exception e) {
            }
        }

        if (p.preferences.PASSWORD_HASH.equals(Controller.session.get("pwdhash")) && Boolean.parseBoolean(Controller.session.get("auth")))
            return true;

        askPassword(p.name);
        return false;
    }

    public static boolean authFromCookie() {
        String usercookie;
        String pwdcookie;
        try {
            usercookie = Crypto.decryptAES(Http.Request.current().cookies.get("user").value);
            pwdcookie = Crypto.decryptAES(Http.Request.current().cookies.get("pwdhash").value);
            Controller.session.put("auth", true);
            Controller.session.put("user", usercookie);
            Controller.session.put("pwdhash", pwdcookie);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAnonymous() {
        Application.authFromCookie();
        if (Application.getSessionLogin().equals("Anonymous"))
            return true;
        else
            return false;
    }

    public static String getSessionLogin() {
        String anonymous = "Anonymous";

        try {
            return Crypto.decryptAES(Http.Request.current().cookies.get("user").value);
        } catch (Exception e) {
            String username = Controller.session.get("user");
            if (Application.isAuth(username))
                return username;
            else
                return anonymous;
        }
    }

    public static boolean isAdmin() {
        // DEBUG
    	boolean enableadmin = "true".equals(play.Play.configuration.getProperty("myrealm.admin.enable"));
    	if(!enableadmin)
    		return false;
    	String root = play.Play.configuration.getProperty("myrealm.admin.root");
        if (Application.getSessionLogin().equals(root))
            return true;
        // DEBUG
        if (!Application.authFromCookie())
            return false;
        Player p = Player.find(Application.getSessionLogin());
        if (p != null && p.isAdmin)
            return true;
        else
            return false;
    }

    public static boolean isAdmin(String playerName) {
        // Admin access IF http user is admin (from cookies)
        if (!playerName.equals(Application.getSessionLogin()))
            return false;
        // Admin access IF http user = admin player from DB
        if (!Application.isAdmin())
            return false;
        return true;
    }

}
