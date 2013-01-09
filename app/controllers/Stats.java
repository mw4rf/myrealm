package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import models.players.Player;
import models.realm.snapshots.GoodSnapshot;
import models.realm.snapshots.RealmSnapshot;
import models.realm.stats.BuildingsHistory;
import models.realm.stats.BuildingsStatus;
import models.realm.stats.GoodEvolution;
import models.realm.stats.GoodsProduction;
import play.i18n.Messages;
import play.mvc.Controller;
import world.StatsContainer;
import world.World;

public class Stats extends Controller {
	
//########################################################################################################
//##### REALM STATS
//########################################################################################################
	
	/**
	 * Index page for the REALM stats of the given player
	 * @param playerName
	 */
	public static void realm(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		List<StatsContainer> map = new ArrayList();
    	// loop snapshots
    	List<RealmSnapshot> snl = RealmSnapshot.find("player = ? order by takenAt asc", player).fetch();
    	Iterator<RealmSnapshot> it = snl.iterator();
    	while(it.hasNext()) {
    		RealmSnapshot snap = it.next();
    		StatsContainer st = new StatsContainer();
    		st.name = snap.toString();
    		st.value = snap.buildingsAverageLevel;
    		map.add(st);
    	}
    	// map empty ? tell player to add buildings
    	if(map.size() < 1) {
    		flash.error(Messages.get("flash.error.stats.none.realm"));
    		Realm.index(playerName);
    		return;
    	}
    	// render
    	renderTemplate("Realm/snapshots/stats/index.html", map, player);
	}
	
	/**
	 * Build the stats for the given player.
	 * @param player
	 * @return
	 */
	protected static List getStats(Player player) {
		List<StatsContainer> map = new ArrayList();
    	// loop snapshots
    	List<RealmSnapshot> snl = RealmSnapshot.find("player = ? order by takenAt asc", player).fetch();
    	Controller.notFoundIfNull(snl);
    	Iterator<RealmSnapshot> it = snl.iterator();
    	while(it.hasNext()) {
    		RealmSnapshot snap = it.next();
    		StatsContainer st = new StatsContainer();
    		st.name = snap.toString();
    		st.value = snap.buildingsAverageLevel;
    		map.add(st);
    	}
    	// map empty ? tell player to add buildings
    	if(map.size() < 1) {
    		flash.error(Messages.get("flash.error.stats.none.realm"));
    	}
    	return map;
	}
	
	/**
	 * Show the page with the graph of the BUILDING COUNT AND LEVEL history
	 * @param playerName
	 */
	public static void buildingsHistory(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
		List<RealmSnapshot> map = Stats.getStats(player);
		// render
		renderTemplate("Realm/snapshots/stats/buildingsHistory.html", map, player);
	}
	
	/**
	 * Show the page with the graph of the BUILDING STATUS history
	 * @param playerName
	 */
	public static void buildingsStatus(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
		List<RealmSnapshot> map = Stats.getStats(player);
		// render
		renderTemplate("Realm/snapshots/stats/buildingsStatus.html", map, player);
	}
	
	/**
	 * Show the page with the graph of the PRODUCTION history
	 * @param playerName
	 */
	public static void goodsProduction(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Data
		List<RealmSnapshot> map = Stats.getStats(player);
		// render
		renderTemplate("Realm/snapshots/stats/goodsProduction.html", map, player);
	}
	
	/**
	 * Renders buildings history from snapshots, for the given player, as JSON.
	 * The JSON object rendered is an array of arrays containing :
	 * 		1) the total number of buildings in the realm (Count)
	 * 		2) the average level of the buildings in the realm (AverageLvl)
	 * The following format is used : [ [ Count:[time,value], AverageLvl[time,value] ], ... ]
	 * @param playerName
	 */
    public static void getJSONBuildingsHistory(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	
    	List<StatsContainer> countMap		 = new ArrayList();
    	List<StatsContainer> averageLevelMap = new ArrayList();    	
    	
    	// Loop snapshots
    	List<RealmSnapshot> snl = RealmSnapshot.find("player = ? order by takenAt asc", player).fetch();
    	Iterator<RealmSnapshot> it = snl.iterator();
    	while(it.hasNext()) {
    		// Get snapshot
    		RealmSnapshot snap = it.next();
    		// Get X axis
    		String nameval = "" + new DateTime(snap.takenAt).getMillis();
        	// Y axis : Buildings count
    		countMap.add(new StatsContainer(nameval, snap.buildingsCount));
    		// Y axis : Average Buildings Level
    		averageLevelMap.add(new StatsContainer(nameval, snap.buildingsAverageLevel));
    	}	
    	
    	// render
    	BuildingsHistory map = new BuildingsHistory();
    	map.name = playerName;
    	map.count = countMap;
    	map.averageLevel = averageLevelMap;
    	renderJSON(map);
    }
    
    /**
     * Renders buildings status from snapshots, for the given player, as JSON.
     * The JSON object rendered is an array of arrays containing :
     * 		1) The number of BOOSTED buildings in the realm (Boosted)
     * 		2) The number of STOPPED buildings in the realm (Stopped)
     * The following format is used : [ [ Boosted:[building,count], Stopped[building,count], ... ]
     * @param playerName
     */
    public static void getJSONBuildingsStatus(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	
    	List<StatsContainer> boosted = new ArrayList();
    	List<StatsContainer> stopped = new ArrayList();    	
    	
    	// Loop snapshots
    	List<RealmSnapshot> snl = RealmSnapshot.find("player = ? order by takenAt asc", player).fetch();
    	Iterator<RealmSnapshot> it = snl.iterator();
    	while(it.hasNext()) {
    		// Get snapshot
    		RealmSnapshot snap = it.next();
    		// Get X axis
    		String nameval = "" + new DateTime(snap.takenAt).getMillis();
        	// Y axis : Boosted buildings count
    		boosted.add(new StatsContainer(nameval, snap.countBoostedBuildings()));
    		// Y axis : Stopped buildings count
    		stopped.add(new StatsContainer(nameval, snap.countStoppedBuildings()));
    	}	
    	
    	// render
    	BuildingsStatus map = new BuildingsStatus();
    	map.name = playerName;
    	map.boosted = boosted;
    	map.stopped = stopped;
    	renderJSON(map);
    }
    
    /**
     * Renders goods production from snapshot, for the given player, as JSON.
     * The JSON object rendered is an array of objects with 3 fields :
     * 		1) name of the good (name) ;
     * 		2) average productions for that good (production) ;
     * 		3) average needs for that good (needs).
     * The following format is used: [ [ good, production, needs ], ... ]
     * @param playerName
     */
    public static void getJSONGoodsProduction(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	
    	List<GoodsProduction> map = new ArrayList<GoodsProduction>(); 	
    	
    	// Loop available goods
    	Iterator<String> wgit = World.GOODS.keySet().iterator();
    	while(wgit.hasNext()) {
    		String goodName = wgit.next();
    		// Get data
    		double prod = GoodSnapshot.getAverageProducedQuantity(player, goodName);
    		double need = GoodSnapshot.getAverageNeededQuantity(player, goodName);
    		// Fill container
    		map.add(new GoodsProduction(goodName, prod, need));
    	}
    	// Sort by name
    	Collections.sort(map, GoodsProduction.BY_NAME);
    	// render
    	renderJSON(map);
    }
    
    /**
     * Renders the evolution of the production for the given good and the given player, as JSON.
     * The JSON object rendered is an array of arrays containing :
     * 		1) quantity produced ;
     * 		2) quantity needed ;
     * 		3) ratio.
     * The following format is used: [ [ produced:[name,value], needed:[name,value], ratio:[name,value] ],... ]
     * @param playerName
     * @param goodName
     */
    public static void getJSONGoodEvolution(String playerName, String goodName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	
    	List<StatsContainer> produced = new ArrayList<StatsContainer>(); 	
    	List<StatsContainer> needed = new ArrayList<StatsContainer>();
    	List<StatsContainer> ratio = new ArrayList<StatsContainer>();
    	
    	// Loop snapshots
    	List<RealmSnapshot> snl = RealmSnapshot.find("player = ? order by takenAt asc", player).fetch();
    	Iterator<RealmSnapshot> it = snl.iterator();
    	while(it.hasNext()) {
    		// Get snapshot
    		RealmSnapshot snap = it.next();
    		// Get data
    		double pg = snap.getGoodSnapshot(goodName).produced;
    		double ng = snap.getGoodSnapshot(goodName).needed;
    		double rg = snap.getGoodSnapshot(goodName).quantity;
    		// Get X axis
    		String nameval = "" + new DateTime(snap.takenAt).getMillis();
        	// Y axis : production
    		produced.add(new StatsContainer(nameval, pg));
    		// Y axis : needs
    		needed.add(new StatsContainer(nameval, ng));
    		// Y axis : ratio
    		ratio.add(new StatsContainer(nameval, rg));
    	}	
    	
    	// render
    	GoodEvolution map = new GoodEvolution();
    	map.name = playerName;
    	map.produced = produced;
    	map.needed = needed;
    	map.ratio = ratio;
    	renderJSON(map);
    }

}
