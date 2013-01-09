package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.spi.LoggingEvent;

import models.players.Player;
import models.players.PlayerAction;
import models.players.PlayerTimer;
import models.realm.Building;
import models.realm.BuildingsGroup;
import models.realm.snapshots.BuildingSnapshot;
import models.realm.snapshots.RealmSnapshot;
import play.Logger;
import play.data.validation.Min;
import play.data.validation.Range;
import play.data.validation.Required;
import play.i18n.Messages;
import play.modules.log4play.PlayWebSocketLogAppender;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.With;
import world.BuildSuggestion;
import world.BuildingGoodMap;
import world.GoodLevel;
import world.StatsContainer;
import world.World;
import world.WorldBoost;
import world.WorldBuilding;
import world.WorldGood;
import world.WorldSoldier;

@With(Logs.class)
public class Realm extends Controller {

    /**
     * Index: list of all the buildings of the player
     */
    public static void index(String playerName) {
        // Get player
        Player player = Player.find(playerName);
        Controller.notFoundIfNull(player);

        // Last 5 buildings
        List<Building> temp = player.buildings;
        Collections.sort(temp, Building.BY_DATE_DESC); 
        List<Building> last5buildings = new ArrayList<Building>();
        if(temp.size() >= 5)
        	last5buildings = temp.subList(0, 5);
        else
        	last5buildings = temp;
        
        // Building status count
        int countStpd = 0; // stopped
        int countBstd = 0; // boosted
        int countSmtd = 0; // simulated
        int countNoAr = 0; // no area defined
        int countDpst = 0; // with deposit
        int countAll = player.buildings.size(); // total
        for(Building b : player.buildings) {
        	if(!b.enabled)
        		countStpd++;
        	if(b.boosted)
        		countBstd++;
        	if(b.simulated)
        		countSmtd++;
        	if(b.area <= 0)
        		countNoAr++;
        	if(b.doesExpire())
        		countDpst++;
        }
        
        // Buildings history chart
     	List<RealmSnapshot> map = Stats.getStats(player);
     	
     	// Goods ratio
     	List<WorldGood> goods = player.getGoodsRatio();
     	List<WorldGood> needs = player.getNeededGoods();
     	List<WorldGood> prods = player.getProducedGoods();
        
     	// Goods count
     	int countGoods = World.GOODS.size();
     	int countPrcd = needs.size();
     	int countNeed = prods.size();
     	int countExcs = 0;
     	int countDfct = 0;
     	for(WorldGood g : goods) {
     		if(g.getRealQuantity() < 0)
     			countDfct++;
     		else
     			countExcs++;
     		if(g.getQuantity() < 0)
     			countNeed++;
     	}
     	
        // Add visit
        player.addVisit(request.remoteAddress);

        // render
        render(player, last5buildings, countBstd, countSmtd, countStpd, countNoAr, countDpst, countAll, goods, countGoods, countDfct, countExcs, countNeed, countPrcd);
    }
    
    /**
     * Shows the list of buildings by TYPE
     * <br /><u>Renders</u>: HTML, Realm/lists/type/index.html
     * <br /><u>Vars</u>: {@link Player} <i>player</i>, {@link HashSet}&lt;{@link String}&gt; <i>types</i> with the <b>name</b> of each building type.
     * @param playerName
     */
    public static void listByType(String playerName) {
    	Player player = Player.find(playerName);   	
    	Controller.notFoundIfNull(player);
    	// Get different building type
    	HashSet<String> types = new HashSet<String>();
    	for(Iterator<Building> it = player.buildings.iterator(); it.hasNext();)
    		types.add(it.next().name);
    	// render
    	renderTemplate("Realm/lists/type/index.html", player, types);
    }
    
    /**
     * Shows the list of buildings by AREA
     * <br /><u>Renders</u>: HTML, Realm/lists/area/index.html
     * <br /><u>Vars</u>: {@link Player} <i>player</i>
     * @param playerName
     */
    public static void listByArea(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// render
    	renderTemplate("Realm/lists/area/index.html", player);
    }
    
    /**
     * Shows the list of buildings by GROUP
     * <br /><u>Renders</u>: HTML, Realm/lists/group/index.html
     * <br /><u>Vars</u>: {@link Player} <i>player</i>
     * @param playerName
     */
    public static void listByGroup(String playerName) {
    	Player player = Player.find(playerName);
    	// render
    	renderTemplate("Realm/lists/group/index.html", player);
    }
    
    /**
     * Show the list of STOPPED buildings
     * <br /><u>Renders</u>: HTML, Realm/lists/manage/stopped.html
     * <br /><u>Vars</u>: {@link Player} <i>player</i>, {@link List}&lt;{@link Building}&gt; <i>stopped</i>, {@link List}&lt;{@link WorldGood}&gt; <i>goodsForDisabled</i>
     * @param playerName
     */
    public static void listStopped(String playerName) {
    	String status = "stopped";
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// List of buildings
    	List<Building> buildings = new ArrayList<Building>();
    	for(Iterator<Building> it = player.buildings.iterator(); it.hasNext();) {
    		Building b = it.next();
    		if(!b.enabled)
    			buildings.add(b);
    	}
    	// Goods ratio for disabled buildings
    	List<WorldGood> goodsForDisabled = player.getGoodsRatioForDisabledBuildings();
    	 // Sort
    	String sorting = ""; 
    	String order = "";
        if(player.preferences != null ) {
        	sorting = player.preferences.BUILDINGS_LIST_STOPPED_SORT;
        	order = player.preferences.BUILDINGS_LIST_STOPPED_ORDER;
        }
        if(sorting == null || sorting.isEmpty())
        	sorting = "NAME";
        if(order == null || order.isEmpty())
        	order = "ASC";
        buildings = Building.sortList(buildings, sorting, order);
    	// render
    	renderTemplate("Realm/lists/manage/stopped.html", player, buildings, goodsForDisabled, sorting, order, status);
    }
    
    /**
     * Show the list of BOOSTED buildings
     * <br /><u>Renders</u>: HTML, Realm/lists/manage/stopped.html
     * <br /><u>Vars</u>: {@link Player} <i>player</i>, {@link List}&lt;{@link Building}&gt; <i>boosted</i>, {@link List}&lt;{@link WorldGood}&gt; <i>goodsFromBoost</i>
     * @param playerName
     */
    public static void listBoosted(String playerName) {
    	String status = "boosted";
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// List of buildings
    	List<Building> buildings = new ArrayList<Building>();
    	for(Iterator<Building> it = player.buildings.iterator(); it.hasNext();) {
    		Building b = it.next();
    		if(b.boosted)
    			buildings.add(b);
    	}
    	// Goods ratio for disabled buildings
    	List<WorldGood> goodsFromBoost = player.getGoodsRatioFromBoost();
    	// Sort
    	String sorting = ""; 
    	String order = "";
    	if(player.preferences != null ) {
    		sorting = player.preferences.BUILDINGS_LIST_BOOSTED_SORT;
    		order = player.preferences.BUILDINGS_LIST_BOOSTED_ORDER;
    	}
    	if(sorting == null || sorting.isEmpty())
    		sorting = "NAME";
    	if(order == null || order.isEmpty())
    		order = "ASC";
    	buildings = Building.sortList(buildings, sorting, order);
    	// render
    	renderTemplate("Realm/lists/manage/boosted.html", player, buildings, goodsFromBoost, sorting, order, status);
    }
    
    /**
     * Show the list of SIMULATED buildings
     * <br /><u>Renders</u>: HTML, Realm/lists/manage/stopped.html
     * <br /><u>Vars</u>: {@link Player} <i>player</i>, {@link List}&lt;{@link Building}&gt; <i>simulated</i>, {@link List}&lt;{@link WorldGood}&gt; <i>pGoodsForSimulated</i>, {@link List}&lt;{@link WorldGood}&gt; <i>nGoodsForSimulated</i>
     * @param playerName
     */
    public static void listSimulated(String playerName) {
    	String status = "simulated";
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// List of buildings
    	List<Building> buildings = new ArrayList<Building>();
    	for(Iterator<Building> it = player.buildings.iterator(); it.hasNext();) {
    		Building b = it.next();
    		if(b.simulated)
    			buildings.add(b);
    	}
    	// Goods ratio for disabled buildings
    	 List<WorldGood> goodsForSimulated = player.getGoodsRatioForSimulatedBuildings();
         List<WorldGood> nGoodsForSimulated = player.getNeededGoodsBySimulation();
         List<WorldGood> pGoodsForSimulated = player.getProducedGoodsBySimulation();
    	 // Sort
    	String sorting = ""; 
    	String order = "";
        if(player.preferences != null ) {
        	sorting = player.preferences.BUILDINGS_LIST_SIMULATED_SORT;
        	order = player.preferences.BUILDINGS_LIST_SIMULATED_ORDER;
        }
        if(sorting == null || sorting.isEmpty())
        	sorting = "NAME";
        if(order == null || order.isEmpty())
        	order = "ASC";
         buildings = Building.sortList(buildings, sorting, order);
    	// render
    	renderTemplate("Realm/lists/manage/simulated.html", player, buildings, goodsForSimulated, nGoodsForSimulated, pGoodsForSimulated, sorting, order, status);
    }
    
    /**
     * Show the page with the ABSOLUTE PRODUCTION (what is produced and what is spent)
     * @param playerName
     */
    public static void showAbsoluteProduction(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Data
        List<WorldGood> pgoods = player.getProducedGoods();
        List<WorldGood> ngoods = player.getNeededGoods();
    	// render
    	renderTemplate("Realm/production/absolute/index.html", player, pgoods, ngoods);
    }
    
    /**
     * Show the page with the RELATIVE PRODUCTION (resources in excess or in deficit)
     * @param playerName
     */
    public static void showRelativeProduction(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Data
        List<WorldGood> goods = player.getGoodsRatio();
    	// render
    	renderTemplate("Realm/production/relative/index.html", player, goods);
    }
    
    /**
     * Show the page with GOODS LEVELS
     * @param playerName
     */
    public static void showGoodsLevels(String playerName) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Data
    	List<WorldGood> pgoods = player.getProducedGoods();
    	List<Building> buildings = player.buildings;
    	// Init set
    	HashMap<String,GoodLevel> levelsMap = new HashMap<String,GoodLevel>();
    	for(Iterator<String> wgit = World.GOODS.keySet().iterator(); wgit.hasNext();) {
    		String s = wgit.next();
    		levelsMap.put(s, new GoodLevel(s));
    	}
    	// Loop goods
    	for(Iterator<WorldGood> it = pgoods.iterator() ; it.hasNext() ;) {
    		WorldGood wg = it.next();
    		BuildingGoodMap bgm = wg.getBuildingMap();
    		// Loop buildings producing that good
    		for(Iterator<String> bgmit = bgm.getProducedBy().keySet().iterator() ; bgmit.hasNext() ;) {
    			String bname = bgmit.next();
    			// Loop player buildings
    			for(Iterator<Building> bit = buildings.iterator() ; bit.hasNext() ;) {
    				Building b = bit.next();
    				if(b.name.equals(bname)) {
    					levelsMap.get(wg.getName()).addLevels(b); // addLevels() belongs to GoodLevel class
    				}
    			}
    		}
    	}
    	// sort list by name
    	List<GoodLevel> levels = new ArrayList(levelsMap.values());
    	Collections.sort(levels, GoodLevel.BY_NAME_ASC);
    	// render
    	render("Realm/production/levels/index.html", player, levels);
    }
    
    /**
     * Show the page with the BOOSTS & OBJECTS PRODUCTION ratios
     * @param playerName
     */
    public static void showBoostsProduction(String playerName) {
    	Player player = Player.find(playerName);
        // Data
        List<WorldBoost> boostsRatio = player.getBoostsRatio();
    	// render
    	renderTemplate("Realm/production/units/boosts.html", player, boostsRatio);
    }
    
    /**
     * Show the page with the SOLDIERS PRODUCTION ratios
     * @param playerName
     */
    public static void showSoldiersProduction(String playerName) {
    	Player player = Player.find(playerName);
        // Data
        List<WorldSoldier> soldiersRatio =  player.getSoldiersRatio(true);
    	// render
    	renderTemplate("Realm/production/units/soldiers.html", player, soldiersRatio);
    }
    
    /**
     * Show the page with the RELATIVE NEEDS charts
     * @param playerName
     */
    public static void showRelativeNeeds(String playerName) {
    	Player player = Player.find(playerName);
    	// Data
    	List<WorldGood> ngoods = player.getNeededGoods();
    	// render
    	renderTemplate("Realm/production/needs/index.html", player, ngoods);
    }

    /**
     * Loads the relative needs chart for the given good.
     * Used on Realm/production/needs/index.html by showRelativeNeeds() method.
     * @param playerName
     * @param goodName
     */
    public static void loadStatsForGood(String playerName, String goodName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		return;
    	renderTemplate("Realm/production/needs/chart.html", player, goodName);
    }

    /**
     * Loads the data needed to make the relative needs chart, and returns it as a JSON object.
     * RESPONSE: AJAX JSON
     * @param playerName
     * @param goodName
     */
    public static void getJSONStatsForGood(String playerName, String goodName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		return;
    	List<StatsContainer> map = player.getNeedsRatiosForGood(goodName);
    	renderJSON(map);
    }

    /**
     * Load individual building stats when the user click on the relative needs chart
     * RESPONSE: AJAX HTML
     * @param playerName
     * @param buildingName
     */
    public static void loadBuildingStats(String playerName, String buildingName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		return;
    	List<Building> buildings = player.getBuildingsByName(buildingName);
    	List<WorldGood> ratios = player.getGoodsRatioForBuildingType(buildingName);
    	renderTemplate("Realm/production/needs/stats.html", player, buildingName, buildings, ratios);
    }

    public static void loadRatios(String playerName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		return;
        List<WorldGood> pgoods = player.getProducedGoods();
        List<WorldGood> ngoods = player.getNeededGoods();
        List<WorldGood> goods = player.getGoodsRatio();
        // render
        renderTemplate("Realm/_productionRatios.html", pgoods, ngoods, goods);
    }

    public static void loadRatiosForGroup(String playerName, Long groupId) {
    	Player player = Player.find(playerName);
    	BuildingsGroup group = BuildingsGroup.findById(groupId);
    	Controller.notFoundIfNull(player);
    	Controller.notFoundIfNull(group);
        List<WorldGood> pgoods = player.getProducedGoods(group.buildings);
        List<WorldGood> ngoods = player.getNeededGoods(group.buildings);
        List<WorldGood> goods = player.getGoodsRatio(group.buildings);
        // render
        renderTemplate("Realm/production/_list.html", pgoods, ngoods, goods);
    }

    public static void loadBuildings(@Required String playerName, @Required String buildingName, String sorting, String order) {
        // Player
        Player player = Player.find(playerName);
        //if(!Application.checkauth(player)) return; // AUTH
        // Building
        List<Building> buildings = new ArrayList<Building>();
        Iterator<Building> it = player.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (b.name.equals(buildingName))
                buildings.add(b);
        }
        // World building
        WorldBuilding building = World.BUILDINGS.get(buildingName);
        // Sort
        if(player.preferences != null && ( sorting == null || sorting.isEmpty() || order == null || order.isEmpty() ) ) {
        	sorting = player.preferences.BUILDINGS_LIST_TYPE_SORT;
        	order = player.preferences.BUILDINGS_LIST_TYPE_ORDER;
        }
        buildings = Building.sortList(buildings, sorting, order);
        // render
        renderTemplate("Realm/lists/_buildingsGroup.html", player, buildings, building, sorting, order);
    }

    public static void loadArea(@Required String playerName, @Required int area, String sorting, String order) {
    	Player player = Player.find(playerName);
        if (player == null) {
            return;
        }
        List<Building> buildings = player.getBuildingsByArea(area);
        // Sort
        if(player.preferences != null && ( sorting == null || sorting.isEmpty() || order == null || order.isEmpty() ) ) {
        	sorting = player.preferences.BUILDINGS_LIST_AREA_SORT;
        	order = player.preferences.BUILDINGS_LIST_AREA_ORDER;
        }
        buildings = Building.sortList(buildings, sorting, order);
        // render
        renderTemplate("Realm/lists/_buildingsGroup.html", player, buildings, area, sorting, order);
    }

    public static void loadGroup(String playerName, Long groupId, String sorting, String order) {
    	BuildingsGroup group = BuildingsGroup.findById(groupId);
    	Player player = Player.find(playerName);
        if (player == null) {
            return;
        }
        List<Building> buildings = group.buildings;
        // Sort
        if(player.preferences != null && ( sorting == null || sorting.isEmpty() || order == null || order.isEmpty() ) ) {
        	sorting = player.preferences.BUILDINGS_LIST_GROUP_SORT;
        	order = player.preferences.BUILDINGS_LIST_GROUP_ORDER;
        }
        buildings = Building.sortList(buildings, sorting, order);
    	// ratios & production
    	List<WorldGood> goods = player.getGoodsRatio(group.buildings);
    	List<WorldGood> ngoods = player.getNeededGoods(group.buildings);
    	List<WorldGood> pgoods = player.getProducedGoods(group.buildings);
        // render
        renderTemplate("Realm/lists/_buildingsGroup.html", player, buildings, group, groupId, sorting, order, goods, ngoods, pgoods);
    }
    
    /**
     * Loads a list of buildings according to their <i>status</i> <b>(boosted, stopped, simulated)</b>,
     * sorted by <i>order</i> and <i>sorting</i> criteria.
     * <br />Calls a partial template. This method must be used as a response to an AJAX query.
     * @param playerName
     * @param sorting
     * @param order
     * @param status
     */
    public static void loadStatus(String playerName, String sorting, String order, String status) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// List of buildings
    	List<Building> buildings = new ArrayList<Building>();
    	for(Iterator<Building> it = player.buildings.iterator(); it.hasNext();) {
    		Building b = it.next();
    		if(status.equalsIgnoreCase("boosted") && b.boosted)
    			buildings.add(b);
    		else if(status.equalsIgnoreCase("stopped") && !b.enabled)
    			buildings.add(b);
    		else if(status.equalsIgnoreCase("simulated") && b.simulated)
    			buildings.add(b);
    	}
        // Sort
        if(player.preferences != null && ( sorting == null || sorting.isEmpty() || order == null || order.isEmpty() ) ) {
        	sorting = player.preferences.BUILDINGS_LIST_TYPE_SORT;
        	order = player.preferences.BUILDINGS_LIST_TYPE_ORDER;
        }
        buildings = Building.sortList(buildings, sorting, order);
    	// render
    	renderTemplate("Realm/lists/manage/_list.html", player, buildings, sorting, order, status);
    }

    @Deprecated
    public static void makeSuggestions(String playerName) {
        // Get player
        Player player = Player.find(playerName);
        if (player == null) {
            return;
        }
        // Build suggestions
        List<BuildSuggestion> suggestions = new ArrayList();
        if(player.preferences == null || player.preferences.SHOW_ADVISES)
            suggestions = player.makeProductionAdvises();
        // Boost suggestions
        HashMap<String,Double> boosts = player.buildBoostsAdvises();
        // render (ajax)
        renderTemplate("Realm/_suggestions.html", player, suggestions, boosts);
    }
    
    /**
     * Renders PRODUCTION ADVICES for the given player.
     * @param playerName
     */
    public static void advicesProduction(String playerName) {
    	// Security
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Data
    	 List<BuildSuggestion> advices = player.makeProductionAdvises();
    	// render
    	renderTemplate("Realm/advises/production.html", player, advices);
    }
    
    /**
     * Renders BOOSTS ADVICES for the given player.
     * @param playerName
     */
    public static void advicesBoosts(String playerName) {
    	// Security
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Data
    	HashMap<String,Double> advices = player.buildBoostsAdvises();
    	//-
		HashMap<String,Integer> dft = player.formatTime((int)player.boostCycle);
		String dres = "";
		if(dft.get("h") > 0)
			dres += dft.get("h") + " " + Messages.get("time.hours") + " ";
		if(dft.get("m") > 0)
			dres += dft.get("m") + " " + Messages.get("time.minutes") + " ";
		if(dft.get("s") > 0)
			dres += dft.get("s") + " " + Messages.get("time.seconds");
    	// render
    	renderTemplate("Realm/advises/boosts.html", player, advices, dres);
    }

    public static void addGroup(@Required String playerName, @Required String name, String fgcolor, String bgcolor) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        if(player == null)
        	return;
        // do it
    	BuildingsGroup group = new BuildingsGroup(player, name, fgcolor, bgcolor).save();
    	player.groups.add(group);
    	player.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.group.add", name));
        player.logAction(PlayerAction.LOG_REALM_ADD_GROUP, "" + group.id); // LOG
    	//render
    	index(player.name);
    }

    public static void updateGroup(@Required String playerName, @Required Long groupId, @Required String name, String fgcolor, String bgcolor) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        BuildingsGroup group = BuildingsGroup.findById(groupId);
        if(player == null || group == null)
        	return;
        // do it
        group.name = name;
        if(fgcolor != null)
        	group.fgcolor = fgcolor;
        if(bgcolor != null)
        	group.bgcolor = bgcolor;
        group.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.group.edit", name));
        player.logAction(PlayerAction.LOG_REALM_EDIT_GROUP, "" + group.id); // LOG
    	//render
    	showGroup(player.name, group.id);
    }

    public static void deleteGroup(@Required String playerName, @Required Long groupId) {
    	Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        BuildingsGroup group = BuildingsGroup.findById(groupId);
        if(player == null || group == null)
        	return;
        // do it
        // Remove from player
        player.groups.remove(group);
        player.save();
        // Remove from buildings
        Iterator<Building> it = group.buildings.iterator();
        while(it.hasNext()) {
        	Building b = it.next();
        	b.groups.remove(group);
        	b.save();
        }
        // Clear group
        group.buildings.clear();
        group.save();
        // Delete group
        group.delete();
        // Log & Flash
        flash.success(Messages.get("flash.success.group.delete"));
        player.logAction(PlayerAction.LOG_REALM_DELETE_GROUP, ""); // LOG
        // render
        index(playerName);
    }

    public static void assignGroup(String playerName, Long buildingId, Long groupId) {
    	Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
    	BuildingsGroup group = BuildingsGroup.findById(groupId);
        Building building = Building.findById(buildingId);
        if(building == null || group == null)
        	return;
        // do it
        group.addBuilding(building);
        // render
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }

    public static void removeGroup(String playerName, Long buildingId, Long groupId) {
    	Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
    	BuildingsGroup group = BuildingsGroup.findById(groupId);
        Building building = Building.findById(buildingId);
        if(building == null || group == null)
        	return;
        // do it
        group.removeBuilding(building);
        // render
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }

    public static void showGroup(String playerName, Long groupId) {
    	BuildingsGroup group = BuildingsGroup.findById(groupId);
    	Player player = Player.find(playerName);
    	// ratios & production
    	List<WorldGood> goods = player.getGoodsRatio(group.buildings);
    	List<WorldGood> ngoods = player.getNeededGoods(group.buildings);
    	List<WorldGood> pgoods = player.getProducedGoods(group.buildings);
    	// render
    	render("Realm/lists/group/show.html", group, player, goods, ngoods, pgoods);
    }
    
    public static void addGroupForm(String playerName) {
    	Player player = Player.find(playerName);
    	renderTemplate("Realm/forms/addGroup.html", player);
    }

    public static void addBuildingForm(String playerName) {
    	Player player = Player.find(playerName);
        // Available buildings
        WorldBuilding worldBuildings[] = new WorldBuilding[World.BUILDINGS.size()];
        worldBuildings = World.BUILDINGS.values().toArray(worldBuildings);
        Arrays.sort(worldBuildings, WorldBuilding.BY_NAME);
        // render
    	renderTemplate("Realm/forms/addBuilding.html", player, worldBuildings);
    }
    
    public static void addBuilding(@Required String playerName, @Required String name, @Range(min = 1, max = 5) int level, @Required @Min(0) int productionTimeMinutes, @Required @Min(0) int productionTimeSeconds, @Required boolean simulated, @Required @Min(0) int area, String description, Long[] groups) {
        // Load stuff
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        // Fields validation
        validation.required(productionTimeMinutes).message("validation.required.realm.building.productionTimeMinutes");
        validation.required(productionTimeSeconds).message("validation.required.realm.building.productionTimeSeconds");
        if (validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            validation.keep(); // keep the errors for the next request
        } else {
            // Add building
            Building building = player.addBuilding(name, level, productionTimeMinutes * 60 + productionTimeSeconds, simulated, area, description);
            if(groups != null && groups.length > 0)
            	for(Long gid : groups) {
            		BuildingsGroup group = BuildingsGroup.findById(gid);
            		if(group != null)
            			group.addBuilding(building);
            	}
            // Flash
            flash.success(Messages.get("flash.success.building.add", building.name));
        }
        // render
        index(playerName);
    }

    public static void editBuilding(@Required String playerName, @Required Long buildingId, @Required String name, @Range(min = 1, max = 5) int level, @Required int productionTimeMinutes, @Required int productionTimeSeconds, @Required int area, String description) {
        // load stuff
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        // Fields validation
        validation.required(productionTimeMinutes).message("validation.required.realm.building.productionTimeMinutes");
        validation.required(productionTimeSeconds).message("validation.required.realm.building.productionTimeSeconds");
        if (validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            validation.keep(); // keep the errors for the next request
            Realm.showBuilding(playerName, buildingId);
        }
        // Do update
        Building b = Building.findById(buildingId);
        b.name = name;
        b.level = level;
        b.setProductionTime(productionTimeMinutes, productionTimeSeconds);
        b.area = area;
        b.description = description;
        b.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.building.update", name));
        player.logAction(PlayerAction.LOG_REALM_EDIT_BUILDING, "" + b.id); // LOG
        // render
        showBuilding(playerName, buildingId);
    }

    public static void validateSimulatedBuilding(@Required String playerName, @Required Long buildingId) {
        System.out.println("DO THAT");
    	Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId);
        if (building.simulated) {
            building.simulated = false;
            building.save();
            // Log & Flash
            flash.success(Messages.get("flash.success.building.update", building.name));
            player.logAction(PlayerAction.LOG_REALM_EDIT_BUILDING, "" + building.id); // LOG
        }
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }

    public static void lvlupBuilding(@Required String playerName, @Required Long buildingId) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId);
        if (building.level < 5) {
            building.level = building.level + 1;
            building.save();
            player.logAction(PlayerAction.LOG_REALM_BUILDING_LVLUP, "" + building.id); // LOG
        } else if (player.preferences != null && player.preferences.BUILDING_LEVEL_LOOP) {
        	building.level = 1;
        	building.save();
        }
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }

    public static void switchEnableBuilding(@Required String playerName, @Required Long buildingId) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId);
        if (building.enabled) {
            building.enabled = false;
            player.logAction(PlayerAction.LOG_REALM_BUILDING_DISABLE, "" + building.id); // LOG
        } else {
            building.enabled = true;
            player.logAction(PlayerAction.LOG_REALM_BUILDING_ENABLE, "" + building.id); // LOG
        }
        building.save();
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }

    public static void switchBoostBuilding(@Required String playerName, @Required Long buildingId) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId); 
        if (building.boosted) {
            building.boosted = false;
            player.logAction(PlayerAction.LOG_REALM_BUILDING_BOOST_OFF, "" + building.id); // LOG
        } else {
            building.boosted = true;
            player.logAction(PlayerAction.LOG_REALM_BUILDING_BOOST_ON, "" + building.id); // LOG
        }
        building.save();
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }
    
    /**
     * Update the building description.
     * <br />This method is used with an ajax request triggered by a contenteditable div, and do not
     * render anything (there is no need for a callback with a contenteditable container).
     * @param playerName
     * @param buildingId
     * @param content
     */
    public static void updateBuildingDescription(String playerName, Long buildingId, String content) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	if (!Application.checkauth(player))
    		Controller.notFound();
    	Building b = Building.findById(buildingId);
    	Controller.notFoundIfNull(b);
    	b.description = content;
    	b.save();
    }

    public static void setProductionTime(@Required String playerName, @Required Long buildingId, @Required @Min(0) int productionTimeMinutes, @Required @Min(0) int productionTimeSeconds) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId);
        building.setProductionTime(productionTimeMinutes, productionTimeSeconds);
        player.logAction(PlayerAction.LOG_REALM_BUILDING_SET_PRODUCTION_TIME, "" + building.id); // LOG
        renderTemplate("Realm/_productionTime.html", building);
    }

    public static void setDepositQuantity(@Required String playerName, @Required Long buildingId, @Required @Min(0) int depositQuantity) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId);
        building.setDepositQuantity(depositQuantity);
        // Do we have a timer on it ?
        List<PlayerTimer> timers = player.timers;
        Iterator<PlayerTimer> it = timers.iterator();
        while(it.hasNext()) {
        	PlayerTimer pt = it.next();
        	if(pt.building != null) {
        		if(pt.building.id == buildingId) {
        			pt.resetTimerOnBuilding(depositQuantity);
        			break;
        		}
        	}
        }
        player.logAction(PlayerAction.LOG_REALM_BUILDING_SET_DEPOSIT_QUANTITY, "" + building.id); // LOG
        renderTemplate("Realm/_depositQuantity.html", building);
    }

    public static void setArea(@Required String playerName, @Required Long buildingId, @Required @Min(0) int area) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = Building.findById(buildingId);
        building.area = area;
        building.save();
        player.logAction(PlayerAction.LOG_REALM_BUILDING_SET_AREA, "" + building.id); // LOG
        renderTemplate("Realm/_buildingArea.html", building);
    }

    public static void deleteBuilding(@Required String playerName, @Required Long buildingId) {
        Player p = Player.find(playerName);
        if (!Application.checkauth(p))
            return; // AUTH
        Building bdel = Building.findById(buildingId);
        String bdelname = bdel.name;
        Building deleted = p.deleteBuilding(buildingId);
        flash.success(Messages.get("flash.success.building.delete", bdelname));
        index(playerName);
    }

    public static void deleteSimulation(@Required String playerName) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        player.deleteSimulatedBuildings();
        flash.success(Messages.get("flash.success.simulation.delete"));
        index(playerName);
    }

    public static void duplicateBuilding(@Required String playerName, @Required Long buildingId, boolean simulated) {
        Player player = Player.find(playerName);
        if (!Application.checkauth(player))
            return; // AUTH
        Building building = player.duplicateBuilding(buildingId, simulated);
        renderTemplate("Realm/lists/_buildingLine.html", building, player);
    }

    /**
     * HTML Page Request
     * Renders HTML page with details informations about the given building, belonging to the given user.
     * @param playerName
     * @param buildingId
     */
    public static void showBuilding(@Required String playerName, @Required Long buildingId) {
        // Player
        Player player = Player.find(playerName);
        // Building
        Building building = Building.findById(buildingId);
        // Security
        if(building == null) {
        	flash.success(Messages.get("flash.error.null.building"));
            index(playerName);
        }
        // Available buildings
        WorldBuilding worldBuildings[] = new WorldBuilding[World.BUILDINGS.size()];
        worldBuildings = World.BUILDINGS.values().toArray(worldBuildings);
        // Sort player buildings
        if (player.buildings.size() > 0) {
            Collections.sort(player.buildings, Building.BY_ID);
            Collections.sort(player.buildings, Building.BY_NAME);
        }
        // Add visit
        player.addVisit(request.remoteAddress);
        // Get building history (snapshots)
        List<BuildingSnapshot> history = BuildingSnapshot.getBuildingSnapshots(buildingId);
        Collections.sort(history, BuildingSnapshot.BY_DATE_DESC);
        // render
        render(player, building, worldBuildings, history);
    }

    /**
     * Set the production cycle time
     * @param playerName
     * @param productionTimeDays
     * @param productionTimeHours
     * @param productionTimeMinutes
     * @param productionTimeSeconds
     */
    public static void setCycleTime(@Required String playerName, int productionTimeDays, int productionTimeHours, int productionTimeMinutes, int productionTimeSeconds) {
        Player p = Player.find(playerName);
        Controller.notFoundIfNull(p);
        if (!Application.checkauth(p))
            return; // AUTH
        p.setCycleTime(playerName, productionTimeDays, productionTimeHours, productionTimeMinutes, productionTimeSeconds);
        flash.success(Messages.get("flash.success.preferences.saved"));
        Preferences.cycles(playerName);
    }

    /**
     * Set the boost cycle time
     * @param playerName
     * @param hours
     * @param multiplier
     */
    public static void setBoostCycle(@Required String playerName, int hours, int multiplier) {
    	Player p = Player.find(playerName);
    	Controller.notFoundIfNull(p);
        if (!Application.checkauth(p))
            return; // AUTH
        p.boostCycle = hours*3600;
        p.boostMultiplier = multiplier;
        p.save();
        flash.success(Messages.get("flash.success.preferences.saved"));
        Preferences.cycles(playerName);
    }

//##########################################################################################################
//####################   CALCULATIONS   ####################################################################
//##########################################################################################################
    
    //-----------------------------------------------------------------------------------
    // SHOW PAGES (GET/HTML)
    
    /**
     * Loads the page for TIME based calculations
     * @param playerName
     */
    public static void showTimeCalculations(String playerName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		return;
    	// World Boosts
        WorldBoost boosts[] = new WorldBoost[World.BOOSTS.size()];
        boosts = World.BOOSTS.values().toArray(boosts);
        // Player resources
        List<WorldGood> goods = player.getGoodsRatio();
    	// render
    	renderTemplate("Realm/calculations/time.html", player, boosts, goods);
    }
    
    /**
     * Loads the page for INFLUENCE based calculations
     * @param playerName
     */
    public static void showInfluenceCalculations(String playerName) {
    	Player player = Player.find(playerName);
    	if(player == null)
    		return;
        // Available buildings
        WorldBuilding worldBuildings[] = new WorldBuilding[World.BUILDINGS.size()];
        worldBuildings = World.BUILDINGS.values().toArray(worldBuildings);
        Arrays.sort(worldBuildings, WorldBuilding.BY_NAME);
        // render
    	renderTemplate("Realm/calculations/influence.html", player, worldBuildings);
    }
    
    //-----------------------------------------------------------------------------------
    // RESPONSE METHODS (AJAX)
    
    /**
     * Tell how much time it would take to GET X resources
     * @param playerName
     * @param goodName
     * @param goodQuantity
     */
    public static void howMuchTime(String playerName, String goodName, int goodQuantity) {
        Player p = Player.find(playerName);
        int time = new Long(Math.round(p.howMuchTimeFor(goodName, goodQuantity))).intValue();
        int h = time / 3600;
        int remaining = time % 3600;
        int m = remaining / 60;
        int s = remaining % 60;
        String action = "Realm.howMuchTime";
        p.logAction(PlayerAction.LOG_REALM_CALCULATION_HMT, ""); // LOG
        renderTemplate("Realm/calculations/result/_howmuchtime.html", h, m, s, goodQuantity, goodName, action);
    }

    /**
     * Tell how much time it would take to SPEND X resources, given the current amount
     * @param playerName
     * @param goodName
     * @param goodQuantity
     */
    public static void howMuchTimeSpend(String playerName, String goodName, int goodQuantity) {
        Player p = Player.find(playerName);
        int time = new Long(-Math.round(p.howMuchTimeForSpend(goodName, goodQuantity))).intValue();
        int h = time / 3600;
        int remaining = time % 3600;
        int m = remaining / 60;
        int s = remaining % 60;
        String action = "Realm.howMuchTimeSpend";
        p.logAction(PlayerAction.LOG_REALM_CALCULATION_HMTS, ""); // LOG
        renderTemplate("Realm/calculations/result/_howmuchtime.html", h, m, s, goodQuantity, goodName, action);
    }

    /**
     * Tell how much time it would take to GET X boosts/objects
     * @param playerName
     * @param boostName
     * @param boostQuantity
     */
    public static void howMuchTimeForBoost(String playerName, String boostName, int boostQuantity) {
    	Player p = Player.find(playerName);
        HashMap<String, Integer> gt = p.howMuchTimeForBoost(boostName, boostQuantity);
        List<String> goodstime = new ArrayList();
        Iterator<String> it = gt.keySet().iterator();
        int maxtime = 0;
        boolean canMake = true;
        while(it.hasNext()) {
            String name = it.next();
            int time = gt.get(name);

            // We produce that good
            if (time > 0) {
                int h = time / 3600;
                int remaining = time % 3600;
                int m = remaining / 60;
                int s = remaining % 60;
                Object[] map = { h, m, s, World.BOOSTS.get(boostName).getQuantity(name) * boostQuantity, name };
                goodstime.add(Messages.get("realm.index.calculations.boosts_line", map));
            } else {
                Object[] map = { name, boostName };
                goodstime.add(Messages.get("realm.index.calculations.boosts_lack", map));
                canMake = false;
            }

            if (time > maxtime)
                maxtime = time;
        }

        int h = maxtime / 3600;
        int remaining = maxtime % 3600;
        int m = remaining / 60;
        int s = remaining % 60;

        // Product name & quantity
        boolean hasProduct = false;
        String productName = "";
        int productQuantity = 0;
        WorldBoost wb = World.BOOSTS.get(boostName);
        if (wb.hasProduct()) {
            hasProduct = true;
            productName = wb.getProductName();
            productQuantity = wb.getProductQuantity() * boostQuantity;
        }
        p.logAction(PlayerAction.LOG_REALM_CALCULATION_HMTFB, ""); // LOG
        // Render
        String action = "Realm.howMuchTimeForBoost";
        renderTemplate("Realm/calculations/result/_howmuchtime.html", h, m, s, boostName, boostQuantity, action, goodstime, canMake, hasProduct, productName, productQuantity);
    }

    public static void ratioSimulation(String playerName, String buildingName, int buildingQuantity, int buildingLevel) {
        Player p = Player.find(playerName);
        ArrayList<WorldGood> goods = new ArrayList<WorldGood>();
        HashMap<String, Double> simulatedGoods = new HashMap();

        // Get building influence on production
        WorldBuilding wb = World.BUILDINGS.get(buildingName);
        Iterator<WorldGood> wit = wb.getGoods().iterator();
        while (wit.hasNext()) {
            WorldGood tmp = wit.next();
            double avg = Building.getAverageProductionTime(buildingName);
            if (avg == 0)
                continue;
            double newq = tmp.getQuantity() * p.cycleTime / avg * buildingQuantity * buildingLevel;
            int ix = (int) (newq * 100.0); // scale it
            double newqround = ix / 100.0; // 2 digits after .
            WorldGood wg = new WorldGood(tmp.getName(), tmp.getQuantity(), newqround);
            wg.setImage(tmp.getImage());
            goods.add(wg);
            simulatedGoods.put(wg.getName(), wg.getRealQuantity());
        }

        // Get real production (so the user can compare)
        List<WorldGood> ratio = p.getGoodsRatio();
        HashMap<String, Double> currentGoods = new HashMap();
        HashMap<String, Double> futureGoods = new HashMap();
        Iterator<WorldGood> git = ratio.iterator();
        while (git.hasNext()) {
            WorldGood wg = git.next();
            // If the building doesn't produce taht good, just skip
            if (!simulatedGoods.containsKey(wg.getName()))
                continue;
            // Get real production of that good
            currentGoods.put(wg.getName(), wg.getRealQuantity());
            // Get future production of that good, if we build that building
            double future = wg.getRealQuantity() + simulatedGoods.get(wg.getName());
            int ix = (int) (future * 100.0); // scale it
            double futureround = ix / 100.0; // 2 digits after .
            futureGoods.put(wg.getName(), futureround);
        }
        p.logAction(PlayerAction.LOG_REALM_CALCULATION_RS, ""); // LOG
        renderTemplate("Realm/calculations/result/_ratioSimulation.html", goods, currentGoods, futureGoods);
    }    
    
    public static void globalAction(String playerName, String action, String option, Long[] buildings) {
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	// Remove duplicate (protection from JavaScript bugs & AJAX desynchronisation)
    	Set<Long> uset = new HashSet<Long>(Arrays.asList(buildings));
    	buildings = uset.toArray(buildings);
    	// Process each building
    	for(Long id : buildings) {
    		Building b = Building.findById(id);
    		if(b == null)
    			continue;
    		b.executeAction(player, action, option);
    		b.save();
    	}
    	
    	renderText("GLOBAL ACTIONS: " + action + " / " + option + " / " + buildings.length);
    }

}
