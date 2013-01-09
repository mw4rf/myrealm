package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import models.players.Player;
import models.realm.Building;
import play.mvc.Before;
import play.mvc.Controller;
import world.WorldBuilding;
import world.WorldGood;

public class Import extends Controller {

	@Before
	public static void checkAuth() {
		if(!Application.isAuth(Application.getSessionLogin()))
			Controller.notFound();
	}
	

	/**
	 * Main wrapper, to call other methods.
	 * @param playerId
	 * @param format : XML
	 * @param data : Buildings
	 * @param mode : add / update / destroy
	 */
	public static void importer(Long playerId, String format, String data, String mode, File attachment) {
		if(data.equals("buildings"))
			if(format.equals("xml"))
				buildingsXML(playerId, mode, attachment);
	}
	
	
	/**
	 * Import buildings from XML.
	 * <br />Add mode : add all the imported buildings in a new GROUP.
	 * <br />Update mode : update existing buildings, add new ones.
	 * <br />Destroy mode : destroy all buildings, and import new ones.
	 * @param playerId
	 * @param mode : add / update / destroy
	 */
	public static void buildingsXML(Long playerId, String mode, File attachment) {
		Player player = Player.findById(playerId);
		Controller.notFoundIfNull(player);
		// Load XML
		
	}
	
}