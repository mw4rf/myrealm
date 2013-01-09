package controllers;

import java.util.ArrayList;
import java.util.List;

import models.adventures.Adventure;
import models.players.Player;
import models.players.PlayerAction;
import models.realm.Building;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * This class is used to export the user profile data to several formats, so the user is not
 * "locked-in" MyRealm and can use her data in other softwares.
 * @author mw4rf
 *
 */
public class Export extends Controller {
	
	@Before
	public static void checkAuth() {
		if(!Application.isAuth(Application.getSessionLogin()))
			Controller.notFound();
	}
	
	/**
	 * Main helper method called to export data. This method calls other methods in this class, depending on
	 * the <b>data</b> {@link String} provided. All other parameters are simply passed to the called method.
	 * @param playerId the Id of the player to load
	 * @param format the export format (CSV, TSV, XML, HTML)
	 * @param screen true to display on screen, false to download as file
	 * @param data the database table to export
	 */
	public static void export(Long playerId, String format, Boolean screen, String data) {
		if(data.equals("buildings"))
			buildings(playerId, format, screen);
		else if(data.equals("adventures"))
			adventures(playerId, format, screen);
		else if(data.equals("actions"))
			actions(playerId, format, screen);
		else
			Controller.notFound();
	}
	
	/**
	 * Export the list of all the buildings owned by the given player.
	 * @param playerId the Id of the player to load
	 * @param format the export format (CSV, TSV, XML, HTML)
	 * @param screen true to display output, false to download in a file
	 */
	public static void buildings(Long playerId, String format, Boolean screen) {
		// Load player and check it exists
		Player player = Player.findById(playerId);
		Controller.notFoundIfNull(player);
		// Load data
		List<Building> buildings = player.buildings;
		// Check format
		if(format.equals("XML"))
			if(screen)
				render("Export/buildings/buildingsToXML.txt", buildings);
			else
				render("Export/buildings/buildingsToXML.xml", buildings);
		else if (format.equals("HTML"))
			if(!screen) {
				Controller.response.setHeader("Content-type", "application/force-download") ;
				Controller.response.setHeader("Content-Disposition", "attachment; filename=\"buildings.html\""); 
				render("Export/buildings/buildingsToHTML.html", buildings); }
			else
				render("Export/buildings/buildingsToHTML.html", buildings);
		else if(format.equals("TSV"))
			if(screen) // show on screen
				render("Export/buildings/buildingsToTSV.txt", buildings);
			else // download file
				render("Export/buildings/buildingsToTSV.tsv", buildings);
		else // default CSV
			if(screen)
				render("Export/buildings/buildingsToCSV.txt", buildings);
			else
				render("Export/buildings/buildingsToCSV.csv", buildings);
	}
	
	/**
	 * Export the list of all the adventures owned by the given player, and all the adventures she was part of.
	 * @param playerId the Id of the player to load
	 * @param format the export format (CSV, TSV, XML, HTML)
	 * @param screen true to display output, false to download in a file
	 */
	public static void adventures(Long playerId, String format, Boolean screen) {
		// Load player and check it exists
		Player player = Player.findById(playerId);
		Controller.notFoundIfNull(player);
		// Load data
		List<Adventure> adventures = new ArrayList<Adventure>();
		adventures.addAll(player.adventures);
		adventures.addAll(player.participations);
		// Check format
		if(format.equals("XML"))
			if(screen)
				render("Export/adventures/adventuresToXML.txt", adventures);
			else
				render("Export/adventures/adventuresToXML.xml", adventures);
		else if (format.equals("HTML"))
			if(!screen) {
				Controller.response.setHeader("Content-type", "application/force-download") ;
				Controller.response.setHeader("Content-Disposition", "attachment; filename=\"adventures.html\""); 
				render("Export/adventures/adventuresToHTML.html", adventures); }
			else
				render("Export/adventures/adventuresToHTML.html", adventures);
		else if(format.equals("TSV"))
			if(screen) // show on screen
				render("Export/adventures/adventuresToTSV.txt", adventures);
			else // download file
				render("Export/adventures/adventuresToTSV.tsv", adventures);
		else // default CSV
			if(screen)
				render("Export/adventures/adventuresToCSV.txt", adventures);
			else
				render("Export/adventures/adventuresToCSV.csv", adventures);
	}
	
	/**
	 * Export the list of all the actions performed by the given player.
	 * @param playerId the Id of the player to load
	 * @param format the export format (CSV, TSV, XML, HTML)
	 * @param screen true to display output, false to download in a file
	 */
	public static void actions(Long playerId, String format, Boolean screen) {
		// Load player and check it exists
		Player player = Player.findById(playerId);
		Controller.notFoundIfNull(player);
		// Load data
		List<PlayerAction> actions = player.actions;
		// Check format
		if(format.equals("XML"))
			if(screen)
				render("Export/actions/actionsToXML.txt", actions);
			else
				render("Export/actions/actionsToXML.xml", actions);
		else if (format.equals("HTML"))
			if(!screen) {
				Controller.response.setHeader("Content-type", "application/force-download") ;
				Controller.response.setHeader("Content-Disposition", "attachment; filename=\"actions.html\""); 
				render("Export/actions/actionsToHTML.html", actions); }
			else
				render("Export/actions/actionsToHTML.html", actions);
		else if(format.equals("TSV"))
			if(screen) // show on screen
				render("Export/actions/actionsToTSV.txt", actions);
			else // download file
				render("Export/actions/actionsToTSV.tsv", actions);
		else // default CSV
			if(screen)
				render("Export/actions/actionsToCSV.txt", actions);
			else
				render("Export/actions/actionsToCSV.csv", actions);
	}
	
}
