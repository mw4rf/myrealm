package models.players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import models.adventures.Adventure;
import models.adventures.AdventureComment;
import models.guilds.GuildEvent;
import models.guilds.Membership;
import models.market.MarketAdventureOffer;
import models.market.MarketAdventureSale;
import models.market.MarketBoostExchange;
import models.polls.PollVote;
import models.realm.Building;
import models.realm.BuildingsGroup;
import models.realm.snapshots.RealmSnapshot;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import play.db.jpa.JPA;
import play.db.jpa.Model;
import serializers.PlayerWrapper;
import world.BuildSuggestion;
import world.BuildingGoodMap;
import world.StatsContainer;
import world.World;
import world.WorldBoost;
import world.WorldBuilding;
import world.WorldGood;
import world.WorldSoldier;

@Entity
public class Player extends Model {

    // DB TABLES

    public String name;
    public String passwd;

    @Type(type = "org.hibernate.type.BooleanType")
    public boolean isAdmin = false;

    public Date lastConnection = new Date();

    public Date lastVisit = new Date();
    public Long visits = new Long(0);
    public String lastVisitor = "";

    public int cycleTime = 3600; // default: 1h

    // boosts
    public int boostMultiplier = 2;
    public int boostCycle = 24*3600;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Building> buildings;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    public List<BuildingsGroup> groups = new ArrayList<BuildingsGroup>();

    @OneToMany
    public List<Adventure> adventures;

    @ManyToMany(mappedBy = "participants")
    public List<Adventure> participations;

    @ManyToMany(mappedBy = "participants")
    public List<GuildEvent> events;

    @OneToOne(cascade = CascadeType.ALL)
    public PlayerPreferences preferences;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    public Membership membership;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    public List<PlayerAction> actions = new ArrayList<PlayerAction>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    public List<PlayerTimer> timers = new ArrayList<PlayerTimer>();

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    public PlayerNote note;

    @OneToMany(fetch = FetchType.LAZY)
    public List<Player> friends = new ArrayList<Player>();
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    public List<RealmSnapshot> snapshots = new ArrayList<RealmSnapshot>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    public List<PollVote> votes = new ArrayList<PollVote>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    public List<MarketAdventureSale> adventureSales = new ArrayList<MarketAdventureSale>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    public List<MarketAdventureOffer> adventureOffers = new ArrayList<MarketAdventureOffer>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    public List<MarketBoostExchange> boostsToReceive = new ArrayList<MarketBoostExchange>();

    @OneToMany(mappedBy = "giver", cascade = CascadeType.ALL)
    public List<MarketBoostExchange> boostsToGive = new ArrayList<MarketBoostExchange>();

    //-----------------------------------------------------------------------------------------

    public Player(String name) {
        super();
        this.name = Player.validatePlayerName(name);
    }

    public static Player find(String playerName) {
    	if(playerName == null || playerName.isEmpty())
    		return null;
        playerName = Player.validatePlayerName(playerName);
        return Player.find("byName", playerName).first();
    }

    public static List<PlayerWrapper> search(String playerName) {
        playerName = Player.validatePlayerName(playerName);
        // Bug workaround : seems to work only if the first letter is lowercase
        playerName = playerName.toLowerCase();
        List<Player> pl = Player.find("byNameLike", "%" + playerName + "%").fetch();
        List<PlayerWrapper> res = new ArrayList<PlayerWrapper>();
        for(Iterator<Player> it = pl.iterator() ; it.hasNext() ; )
        	res.add(new PlayerWrapper(it.next()));
        return res;
    }

    public boolean hasGuild() {
        if(this.membership == null)
        	return false;
        else
        	return true;
    }

    public static boolean hasGuild(String playerName) {
        Player player = Player.find(playerName);
        if(player == null)
        	return false;
        return player.hasGuild();
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Increments the visits counter for this player if: 1) 1 hour has passed since the last visit ; 
     * OR 2) the visitor IP is different (it's someone else visiting the player's profile).
     * @param visitor
     */
    public void addVisit(String visitor) {
        // Init fields, in case they are null (JPA is so buggy with FS backend, those fields should NOT be empty as they are initialized, but they happen to be.
        if (this.lastVisit == null)
            this.lastVisit = new Date();
        if (this.lastVisitor == null)
            this.lastVisitor = "";
        if (this.visits == null)
            this.visits = new Long(0);
        if (this.lastConnection == null)
            this.lastConnection = new Date();
        // Time interval between the last visit and now
        //long tdiff = (new Date().getTime() - this.lastVisit.getTime()) / (3600 * 1000); // in HOURS -- pure Java
        int tdiff = new Duration(new DateTime(this.lastVisit), new DateTime()).toPeriod().getHours(); // JodaTime API
        // If it's been LESS than 1 hour, and it's the same visitor : do not log again
        if (tdiff < 1)
            if (visitor.equals(this.lastVisitor))
                return;
        // If it's need more than 1 hour, or it's a new visitor (difference IP address)
        this.visits++;
        this.lastVisitor = visitor;
        this.lastVisit = new Date();
        this.save();
    }

    /**
     * Defines the "lastConnection" variable, in the player's profile, to NOW.
     */
    public void setLastConnection() {
        this.lastConnection = new Date();
        this.save();
    }

    public static String validatePlayerName(String playerName) {
    	if(playerName == null || playerName.isEmpty())
    		return null;
        playerName = playerName.replace("'", "");
        playerName = playerName.replace("`", "");
        playerName = playerName.replace("\"", "");
        playerName = playerName.replace(" ", "_");
        playerName = playerName.replace("$", "");
        playerName = playerName.replace("\\", "");
        playerName = playerName.replace("/", "");
        playerName = playerName.replace("[", "");
        playerName = playerName.replace("]", "");
        playerName = playerName.replace("(", "");
        playerName = playerName.replace(")", "");
        playerName = playerName.replace("{", "");
        playerName = playerName.replace("}", "");
        playerName = playerName.replace("%", "");
        playerName = playerName.replace("=", "");
        playerName = playerName.replace("+", "");
        playerName = playerName.replace("@", "");
        playerName = playerName.replace("#", "");
        playerName = playerName.replace("&", "");

        return playerName;
    }

    public Player addFriend(String playerName) {
        if (playerName.isEmpty())
            return null;
        Player friend = Player.find(playerName);
        // Can't add yourself
        if (playerName.equals(this.name))
            return null;
        // Can't add twice the same
        if (this.friends.contains(friend))
            return null;
        // Ok, add the friend if not null
        if (friend != null) {
            this.friends.add(friend);
            this.logAction(PlayerAction.LOG_FRIENDS_ADD, playerName); // LOG
            this.save();
        }
        return friend;
    }

    public void removeFriend(String playerName) {
        Player friend = Player.find(playerName);
        // Ok, remove the friend if not null
        if (friend != null && this.friends.contains(friend)) {
            this.friends.remove(friend);
            this.logAction(PlayerAction.LOG_FRIENDS_REMOVE, playerName); // LOG
            this.save();
        }
    }

    public List<Player> getFriendOf() {
    	Query query = JPA.em().createNativeQuery("SELECT * FROM player WHERE player.id IN (SELECT player_id FROM player_player WHERE friends_id = '" + this.id + "')", Player.class);
    	List<Player> friendOf = query.getResultList();
        return friendOf;
    }

    public List<PlayerAction> getFriendsActivity() {
        List<PlayerAction> actions = new ArrayList();
        Iterator<Player> it = this.friends.iterator();
        while (it.hasNext()) {
            Player p = it.next();
            actions.addAll(p.actions);
        }
        Collections.sort(actions, PlayerAction.BY_ID_DESC);
        return actions;
    }

    public PlayerAction logAction(String action, String details) {
        PlayerAction pa = new PlayerAction(this, action, details).save();
        this.actions.add(pa);
        return pa;
    }

    public String formatCycleTime() {
        int h = this.cycleTime / 3600;
        int remaining = this.cycleTime % 3600;
        int m = remaining / 60;
        int s = remaining % 60;
        return h + "h " + m + "min " + s + "s";
    }

    public HashMap<String,Integer> formatTime(int seconds) {
    	int h = seconds / 3600;
    	int remaining = seconds % 3600;
    	int m = remaining / 60;
    	int s = remaining % 60;
    	HashMap<String,Integer> map = new HashMap();
    	map.put("h", h);
    	map.put("m", m);
    	map.put("s", s);
    	return map;
    }

    public void setCycleTime(String playerName, int productionTimeDays, int productionTimeHours, int productionTimeMinutes, int productionTimeSeconds) {
        int s = (productionTimeDays * 24 * 3600) + (productionTimeHours * 3600) + (productionTimeMinutes * 60) + productionTimeSeconds;
        this.cycleTime = s;
        this.logAction(PlayerAction.LOG_REALM_SET_CYCLETIME, this.formatCycleTime()); // LOG
        this.save();
    }

    public PlayerNote updateNote(String content) {
        if (note == null) {
            PlayerNote note = new PlayerNote(this, content).save();
            this.note = note;
            this.save();
        } else {
            this.note.content = content;
            this.note.save();
        }
        return note;
    }
    
    /**
     * Take a snapshot of this player's realm. 
     * The {@link RealmSnapshot} object builds and saves itself.
     * This method need a lot a server resources ; it should not be called directly, by only by a cronjob.
     * @return boolean false if the realm is empty (player has no building), true otherwise
     */
    public boolean takeRealmSnapshot() {
    	if(this.buildings.size() > 0) {
    		RealmSnapshot snap = new RealmSnapshot(this);
    		this.snapshots.add(snap);
    		this.save();
    		this.logAction(PlayerAction.LOG_SNAPSHOT_TAKEN, snap.toString()); // LOG
    		return true;
    	}
    	return false;
    }

    public Adventure addAdventure(String name, Date dateStart, Date dateEnd, String notes) {
        Adventure a = new Adventure(this, name);
        a.dateStart = dateStart;
        a.dateEnd = dateEnd;
        a.notes = notes;
        if (a.dateStart != null && a.dateEnd != null) {
            a.save();
            this.adventures.add(a);
            this.save();
            this.logAction(PlayerAction.LOG_ADVENTURES_ADD_ADVENTURE, "" + a.id); // LOG
        }
        return a;
    }

    public Adventure deleteAdventure(Long adventureId) {
        Adventure adventure = Adventure.findById(adventureId);
        if (this.adventures.contains(adventure)) {
            this.adventures.remove(adventure);
            this.save();
            adventure.delete();
            this.logAction(PlayerAction.LOG_ADVENTURES_DELETE_ADVENTURE, "" + adventure.name); // LOG
        }
        return adventure;
    }

    public Building addBuilding(String name, int level, int productionTime, boolean simulated, int area, String description) {
        if (productionTime == 0)
            productionTime = 1;
        Building b = new Building(name, level, productionTime);
        b.simulated = simulated;
        b.area = area;
        b.description = description;
        b.save();
        this.buildings.add(b);
        this.save();
        this.logAction(PlayerAction.LOG_REALM_ADD_BUILDING, "" + b.id); // LOG
        return b;
    }

    public Building duplicateBuilding(Long buildingId, boolean simulated) {
        Building bmodel = Building.findById(buildingId);
        if(bmodel.simulated)
        	simulated = bmodel.simulated;
        Building b = this.addBuilding(bmodel.name, bmodel.level, bmodel.productionTime, simulated, bmodel.area, bmodel.description);
        // Groups
        Iterator<BuildingsGroup> it = bmodel.groups.iterator();
        while(it.hasNext()) {
        	BuildingsGroup g = it.next();
        	b.groups.add(g);
        	b.save();
        	g.buildings.add(b);
        	g.save();
        }
        // Log
        this.logAction(PlayerAction.LOG_REALM_ADD_BUILDING, "" + b.id); // LOG
        return b;
    }

    public Building deleteBuilding(Long buildingId) {
        Building b = Building.findById(buildingId);
        if (this.buildings.contains(b)) {
            this.buildings.remove(b);
            this.save();
            b.delete();
            this.logAction(PlayerAction.LOG_REALM_DELETE_BUILDING, b.name); // LOG
        }
        return b;
    }

    public Player deleteSimulatedBuildings() {
        ArrayList<Long> ids = new ArrayList();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (b.simulated)
                ids.add(b.id);
        }
        Iterator<Long> idsit = ids.iterator();
        while (idsit.hasNext()) {
            this.deleteBuilding(idsit.next());
        }
        this.logAction(PlayerAction.LOG_REALM_DELETE_SIMULATED_BUILDINGS, ""); // LOG
        this.save();
        return this;
    }

    /**
     * Returns the count of SIMULATED buildings in the realm.
     * @return int count
     */
    public int countSimulatedBuildings() {
        int count = 0;
        Iterator<Building> it = buildings.iterator();
        while (it.hasNext()) {
            if (it.next().simulated)
                count++;
        }
        return count;
    }
    
    /**
     * Returns the count of STOPPED buildings in the realm.
     * @return int count
     */
    public int countDisabledBuildings() {
        int count = 0;
        Iterator<Building> it = buildings.iterator();
        while (it.hasNext()) {
            if (!it.next().enabled)
                count++;
        }
        return count;
    }

    /**
     * Returns the count of BOOSTED buildings in the realm.
     * @return int count
     */
    public int countBoostedBuildings() {
        int count = 0;
        Iterator<Building> it = buildings.iterator();
        while (it.hasNext()) {
            if (it.next().boosted)
                count++;
        }
        return count;
    }

    /**
     * Returns the count of buildings in the given Area.
     * @param area : 1-9
     * @return int count
     */
    public int countBuildingsByArea(int area) {
    	int count = 0;
    	Iterator<Building> it = buildings.iterator();
    	while(it.hasNext()) {
    		if(it.next().area == area)
    			count++;
    	}
    	return count;
    }

    public HashMap<String, List> getBuildingsSet() {
        HashMap<String, List> map = new HashMap();
        // Order buildings by name
        Collections.sort(this.buildings, Building.BY_NAME);
        // Loop building names, fill the map
        Iterator<String> bnit = World.BUILDINGS.keySet().iterator();
        while (bnit.hasNext()) {
            String bname = bnit.next();
            List<Building> bl = this.getBuildingsByName(bname);
            if (bl.size() > 0)
                map.put(bname, bl);
        }
        return map;
    }

    public List<Building> getBuildingsByName(String buildingName) {
        ArrayList bl = new ArrayList();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (b.name.equals(buildingName)) {
                bl.add(b);
            }
        }
        return bl;
    }

    public List<Building> getBuildingsByArea(int area) {
        ArrayList bl = new ArrayList();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (b.area == area) {
                bl.add(b);
            }
        }
        return bl;
    }

    public boolean hasBuilding(String buildingName) {
        if (getBuildingsByName(buildingName).size() > 0)
            return true;
        else
            return false;
    }

    public HashMap<String, WorldGood> getGoodsRatioAsHashMap() {
        HashMap<String, WorldGood> pratio = new HashMap();
        Iterator<WorldGood> prit = this.getGoodsRatio().iterator();
        while (prit.hasNext()) {
            WorldGood wg = prit.next();
            pratio.put(wg.getName(), wg);
        }
        return pratio;
    }

    public HashMap<String,BuildingGoodMap> getNeedsRatios() {
    	HashMap<String,BuildingGoodMap> map = new HashMap();
    	List<WorldGood> needs = this.getNeededGoods();
    	Iterator<WorldGood> it = needs.iterator();
    	while(it.hasNext()) {
    		WorldGood wg = it.next();
    		BuildingGoodMap bgm = new BuildingGoodMap(wg);
    		HashMap<String,Double> innermap = new HashMap();
    		// Get total need -- this is 100%
    		double totalNeed = wg.getRealQuantity();
    		// Get buildings needing that good
    		Iterator<WorldBuilding> wbit = World.GOODS.get(wg.getName()).getNeededBy().values().iterator();
    		while(wbit.hasNext()) {
    			WorldBuilding wb = wbit.next();
    			// Get player's buildings
    			Iterator<Building> pbit = this.buildings.iterator();
    			double bNeeds = 0;
    			while(pbit.hasNext()) {
    				Building b = pbit.next();
    				// Get need
    				if(b.name.equals(wb.getName())) {
    					// is it stopped ?
    					if(!b.enabled)
    						continue;
    					// Get raw need
    					int rawN = World.BUILDINGS.get(wb.getName()).getRawNeed(wg.getName());
    					// is it boosted ?
    					if(b.boosted)
    						rawN *= this.boostMultiplier;
    					// Calc real need
    	                double quantityByCycle = rawN * b.level * this.cycleTime;
    	                double realQuantity = quantityByCycle / b.productionTime;
    	                // add it
    	                bNeeds += realQuantity;
    				}
    			}
    			double percent = bNeeds * 100 / totalNeed;
    	        int ix = (int) (percent * 100.0); // scale it
    	        percent = ix / 100.0; // 2 digits after .
    			innermap.put(wb.getName(), percent);
    		}
			bgm.setNeedsRatios(innermap);
			map.put(wg.getName(), bgm);
    	}
    	return map;
    }

    public List<StatsContainer> getNeedsRatiosForGood(String goodName) {
    	ArrayList<StatsContainer> list = new ArrayList();
    	List<WorldGood> needs = this.getNeededGoods();
    	Iterator<WorldGood> it = needs.iterator();
    	while(it.hasNext()) {
    		WorldGood wg = it.next();
    		if(!wg.getName().equals(goodName))
    			continue;
    		// Get total need -- this is 100%
    		double totalNeed = wg.getRealQuantity();
    		// Get buildings needing that good
    		Iterator<WorldBuilding> wbit = World.GOODS.get(wg.getName()).getNeededBy().values().iterator();
    		while(wbit.hasNext()) {
    			WorldBuilding wb = wbit.next();
    			// Get player's buildings
    			Iterator<Building> pbit = this.buildings.iterator();
    			double bNeeds = 0;
    			while(pbit.hasNext()) {
    				Building b = pbit.next();
    				// Get need
    				if(b.name.equals(wb.getName())) {
    					// is it stopped ?
    					if(!b.enabled)
    						continue;
    					// Get raw need
    					int rawN = World.BUILDINGS.get(wb.getName()).getRawNeed(wg.getName());
    					// is it boosted ?
    					if(b.boosted)
    						rawN *= this.boostMultiplier;
    					// Calc real need
    	                double quantityByCycle = rawN * b.level * this.cycleTime;
    	                double realQuantity = quantityByCycle / b.productionTime;
    	                // add it
    	                bNeeds += realQuantity;
    				}
    			}
    			double percent = bNeeds * 100 / totalNeed;
    	        int ix = (int) (percent * 100.0); // scale it
    	        percent = ix / 100.0; // 2 digits after .
    	        // insert
    	        StatsContainer st = new StatsContainer();
    	        st.name = wb.getName();
    	        st.value = percent;
    			list.add(st);
    		}
    	}
    	return list;
    }

    public List<WorldSoldier> getSoldiersRatio(boolean withBread) {
        List<WorldSoldier> ratio = new ArrayList<WorldSoldier>();
        // Get a hashmap of goods from the player's ratio
        HashMap<String, WorldGood> pratio = this.getGoodsRatioAsHashMap();
        // Loop soldiers
        Iterator<WorldSoldier> wbit = World.SOLDIERS.values().iterator();
        while (wbit.hasNext()) {
            WorldSoldier wb = wbit.next();
            WorldSoldier soldier = new WorldSoldier(wb.getName());
            soldier.setImage(wb.getImage());
            // Loop needs of the soldier
            Iterator<WorldGood> bnit = wb.getGoods().iterator();
            while (bnit.hasNext()) {
                WorldGood wg = bnit.next();
                // For each good needed, get the real ratio
                // Continue if the player doesn't know yet that good
                if (pratio.get(wg.getName()) == null) {
                    WorldGood good = new WorldGood(wg.getName(), wg.getQuantity());
                    good.setRealQuantity(0);
                    soldier.addGood(good);
                    continue;
                }
                // Get the production ratio of that good
                double goodratio = pratio.get(wg.getName()).getRealQuantity();
                // Then, get the quantity needed by this soldier
                int neededQ = wg.getQuantity();
                // And do the math to get the quantity by cycle
                double realQ = goodratio / neededQ;
                // Add a new good to the soldier
                WorldGood good = new WorldGood(wg.getName(), wg.getQuantity());
                good.setImage(wg.getImage());
                good.setRealQuantity(realQ);
                soldier.addGood(good); // now, use the boost.getRealQuantity() method to get the boost production ratio
            }
            // Special need : Settlers
            double settlersNormalComing = this.cycleTime / (15 * 60); // fixed production time = 15 min
            double settlersFromBread;
            // If player doesn't produce "Pain", a null pointer exception is thrown
			try {
				settlersFromBread = 0;
				if (withBread) {
				    // Bread quantity available each cycle
				    double bAVQ = pratio.get("Pain").getRealQuantity();
				    // 1 settler = 25 bread
				    settlersFromBread = bAVQ / 25;
				}
			} catch (Exception e) {
				settlersFromBread = 0;
			}
            double settlersRQ = settlersNormalComing + settlersFromBread;
            WorldGood settler = new WorldGood("Settler", 1);
            settler.setRealQuantity(settlersRQ);
            settler.setImage("images/goods/settler.png");
            soldier.addGood(settler);

            // Add only non-empty soldiers (= that can be produced)
            if (soldier.getRealQuantity() > 0)
                ratio.add(soldier);
        }
        return ratio;
    }

    public List<WorldBoost> getBoostsRatio() {
        List<WorldBoost> ratio = new ArrayList<WorldBoost>();
        // Get a hashmap of goods from the player's ratio
        HashMap<String, WorldGood> pratio = this.getGoodsRatioAsHashMap();
        // Loop boosts
        Iterator<WorldBoost> wbit = World.BOOSTS.values().iterator();
        while (wbit.hasNext()) {
            WorldBoost wb = wbit.next();
            WorldBoost boost = new WorldBoost(wb.getName());
            boost.setImage(wb.getImage());
            boost.setProductName(wb.getProductName());
            boost.setProductQuantity(wb.getProductQuantity());
            // Loop needs of the boost
            Iterator<WorldGood> bnit = wb.getGoods().iterator();
            while (bnit.hasNext()) {
                WorldGood wg = bnit.next();
                // For each good needed, get the real ratio
                // Continue if the player doesn't know yet that good
                if (pratio.get(wg.getName()) == null) {
                    WorldGood good = new WorldGood(wg.getName(), wg.getQuantity());
                    good.setRealQuantity(0);
                    boost.addGood(good);
                    continue;
                }
                // Get the production ratio of that good
                double goodratio = pratio.get(wg.getName()).getRealQuantity();
                // Then, get the quantity needed by this boost
                int neededQ = wg.getQuantity();
                // And do the math to get the quantity by cycle
                double realQ = goodratio / neededQ;
                // Add a new good to the boost
                WorldGood good = new WorldGood(wg.getName(), wg.getQuantity());
                good.setImage(wg.getImage());
                good.setRealQuantity(realQ);
                boost.addGood(good); // now, use the boost.getRealQuantity() method to get the boost production ratio
            }
            // Add only non-empty boosts (= that can be produced)
            if (boost.getRealQuantity() > 0)
                ratio.add(boost);
        }
        return ratio;
    }

    public List<WorldGood> getProducedGoods() {
        return this.getProducedGoods(this.buildings);
    }

    public List<WorldGood> getProducedGoods(List<Building> buildingslist) {
    	HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = buildingslist.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.enabled)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();

                // Positive quantities
                if (gmodel.getQuantity() < 0) {
                    continue;
                }

                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }
        }
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getNeededGoods() {
    	return this.getNeededGoods(this.buildings);
    }

    public List<WorldGood> getNeededGoods(List<Building> buildingslist) {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = buildingslist.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.enabled)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();

                // Positive quantities
                if (gmodel.getQuantity() > 0) {
                    continue;
                }

                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }
        }
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getProducedGoodsBySimulation() {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.enabled)
                continue;
            if (!b.simulated)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();

                // Positive quantities
                if (gmodel.getQuantity() < 0) {
                    continue;
                }

                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }
        }
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getNeededGoodsBySimulation() {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.enabled)
                continue;
            if (!b.simulated)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();

                // Positive quantities
                if (gmodel.getQuantity() > 0) {
                    continue;
                }

                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }
        }
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getGoodsRatioForSimulatedBuildings() {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.enabled)
                continue;
            if (!b.simulated)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();
                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }//wit
        }//it
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getGoodsRatioFromBoost() {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.enabled)
                continue;
            if (!b.boosted)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();
                if (gmodel.getQuantity() < 0)
                    continue;
                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                realQuantity = realQuantity * (this.boostMultiplier - 1);
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }//wit
        }//it
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getGoodsRatioForDisabledBuildings() {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (b.enabled)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();
                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }//wit
        }//it
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getGoodsRatioForBuildingType(String buildingName) {
        HashMap<String, WorldGood> ratio = new HashMap();
        Iterator<Building> it = this.buildings.iterator();
        while (it.hasNext()) {
            Building b = it.next();
            if (!b.name.equals(buildingName))
                continue;
            if (!b.enabled)
                continue;
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();
                if (b.productionTime == 0)
                    b.productionTime = 1;
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // Good already exist
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage());
                ratio.put(gmodel.getName(), newgood);
            }//wit
        }//it
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        return lwg;
    }

    public List<WorldGood> getGoodsRatio() {
    	return this.getGoodsRatio(this.buildings);
    }

    public List<WorldGood> getGoodsRatio(List<Building> buildingslist) {
        HashMap<String, WorldGood> ratio = new HashMap(); // ratios container, unique key
        Iterator<Building> it = buildingslist.iterator();
        while (it.hasNext()) {
            Building b = it.next(); // Building instance
            // No production for disabled buildings : continue with the following building
            if (!b.enabled)
                continue;
            // Protection from error (division by 0)
            if (b.productionTime == 0)
                b.productionTime = 1;
            // Get the building model from the server
            WorldBuilding wb = World.BUILDINGS.get(b.name);
            Iterator<WorldGood> wit = wb.getGoods().iterator();
            // Loop goods produced/needed by that building
            while (wit.hasNext()) {
                WorldGood gmodel = wit.next();
                // Quantity of that good produced or needed by cycle
                double quantityByCycle = gmodel.getQuantity() * b.level * this.cycleTime;
                double realQuantity = quantityByCycle / b.productionTime;
                // If the building has a boost, multiply its output by the boost mulitplier
                if (b.boosted && realQuantity > 0)
                    realQuantity = realQuantity * this.boostMultiplier;
                // If the good already exist, get its quantity and add the new quantity
                if (ratio.containsKey(gmodel.getName())) {
                    realQuantity = ratio.get(gmodel.getName()).getRealQuantity() + realQuantity;
                }
                // Create a new WorldGood object, and add it to the map
                
                WorldGood newgood = new WorldGood(gmodel.getName(), gmodel.getQuantity(), realQuantity);
                newgood.setImage(gmodel.getImage()); // to display
                ratio.put(gmodel.getName(), newgood);
            }//wit iterator
        }//it iterator
        List<WorldGood> lwg = new ArrayList<WorldGood>(ratio.values());
        // order by quantity, descending
        Collections.sort(lwg, WorldGood.BY_QUANTITY);
        // return
        return lwg;
    }

    public List<BuildSuggestion> makeProductionAdvises() {
        ArrayList<BuildSuggestion> list = new ArrayList<BuildSuggestion>();
        List<WorldGood> ratio = this.getGoodsRatio();
        // Loop Goods
        Iterator<WorldGood> it = ratio.iterator();
        while (it.hasNext()) {
            WorldGood wg = it.next();
            // If the realQuantity is < 0, we need to build more buildings to produce that good !
            if (wg.getRealQuantity() < 0) {
                double need = wg.getRealQuantity();
                // What building produced that good ?
                BuildingGoodMap bgm = World.GOODS.get(wg.getName());
                Iterator<WorldBuilding> bgmit = bgm.getProducedBy().values().iterator();
                while (bgmit.hasNext()) {
                    WorldBuilding wb = bgmit.next();
                    // How much does 1 building lvl 1 produces in 1 cycle ?
                    int prod = wb.getGood(wg.getName()).getQuantity();
                    double cycle = Building.getAverageProductionTime(wb.getName());
                    double realprod = prod * this.cycleTime / cycle;
                    // Then how many buildings are needed ?
                    int cb = (int) (need / realprod);
                    if (cb < 0)
                        cb = -cb;
                    if (cb < 1)
                        cb = 1;
                    // Create container
                    BuildSuggestion bs = new BuildSuggestion(wb, cb);
                    bs.setLackingGood(new WorldGood(wg.getName(), wg.getQuantity(), -wg.getRealQuantity()));
                    list.add(bs);
                }
            }
            // If the realQuantity > 0, we may be able to build more buildings that need that good !
            else {
                // Get buildings in need of that good
                ArrayList<String> nb = new ArrayList();
                Iterator<WorldBuilding> wbit = World.BUILDINGS.values().iterator();
                while (wbit.hasNext()) {
                    WorldBuilding wbtemp = wbit.next();
                    // Does the building need that good ?
                    if (wbtemp.doesNeed(wg.getName())) {
                        // What's the real need (= influence) of that building ?
                        WorldGood wgtmp = wbtemp.getGood(wg.getName());
                        double avg = Building.getAverageProductionTime(wbtemp.getName());
                        if (avg == 0)
                            continue;
                        double influence = wgtmp.getQuantity() * this.cycleTime / avg;
                        // If the real need acceptable, given the current production ?
                        if (-influence <= wg.getRealQuantity()) {
                            // How many new building could we build, before reaching the production limit ?
                            int limit = (int) (wg.getRealQuantity() / -influence);
                            // make suggestion
                            BuildSuggestion bs = new BuildSuggestion(wbtemp, limit);
                            bs.setExcessGood(new WorldGood(wg.getName(), wg.getQuantity(), wg.getRealQuantity()));
                            list.add(bs);
                        }
                    }

                }

            }
        }
        return list;
    }

    /**
     * Calculates the time we need to boost all buildings producing a good in deficit, to produce enough of that good to maintain the buildings in need of that good running for
     * a given period of time.
     * @return HashMap<String,Double> : <Good Name, Time in Second to boost>
     */
    public HashMap<String,Double> buildBoostsAdvises() {
    	int boost = this.boostMultiplier;
    	int seconds = this.boostCycle;
    	HashMap<String,Double> result = new HashMap();
    	List<WorldGood> ratio = this.getGoodsRatio();
    	Iterator<WorldGood> it = ratio.iterator();
    	while(it.hasNext()) {
    		WorldGood wg = it.next();
    		if(wg.getRealQuantity() > 0)
    			continue;
    		// What do we produce ?
    		double production = 0;
    		Iterator<WorldGood> pit = this.getProducedGoods().iterator();
    		while(pit.hasNext()) {
    			WorldGood pwg = pit.next();
    			if(pwg.getName().equals(wg.getName())) {
    				production = pwg.getRealQuantity();
    				break;
    			}
    		}
    		// What do we need ?
    		double need = 0;
    		Iterator<WorldGood> nit = this.getNeededGoods().iterator();
    		while(nit.hasNext()) {
    			WorldGood pwg = nit.next();
    			if(pwg.getName().equals(wg.getName())) {
    				need = pwg.getRealQuantity();
    				break;
    			}
    		}
    		need = - need * seconds / this.cycleTime;
    		double time = ( ( need * this.cycleTime / production) - seconds ) / ( boost - 1 ); // just don't ask...
    		//return
    		if(!Double.isInfinite(time) && time <= seconds && time >= 1 )
    			result.put(wg.getName(), time);
    		else if ( time >= 1 )
    			result.put(wg.getName(), new Double(-1));
    		//System.out.println(wg.getName() + " PROD:" + ( production * seconds / this.cycleTime ) + " NEED:" + need + " TIME:" + time + " ---------- " + need + " / " + (boost + 1) + " * " + production);
    	}
    	return result;
    }

    public double howMuchTimeFor(String goodName, int quantity) {
        List<WorldGood> goods = getGoodsRatio();
        WorldGood wg = new WorldGood(goodName, 0);
        Iterator<WorldGood> it = goods.iterator();
        while (it.hasNext()) {
            WorldGood wgi = it.next();
            if (wgi.getName().equals(goodName)) {
                wg = wgi;
                break;
            }
        }
        if (wg.getRealQuantity() == 0)
            return 0;
        double rq = quantity * this.cycleTime / wg.getRealQuantity();
        return rq;
    }

    public double howMuchTimeForSpend(String goodName, int quantity) {
        List<WorldGood> goods = getGoodsRatio();
        WorldGood wg = new WorldGood(goodName, 0);
        Iterator<WorldGood> it = goods.iterator();
        while (it.hasNext()) {
            WorldGood wgi = it.next();
            if (wgi.getName().equals(goodName)) {
                wg = wgi;
                break;
            }
        }
        if (wg.getRealQuantity() == 0)
            return 0;
        double rq = quantity / wg.getRealQuantity() * this.cycleTime;
        return rq;
    }

    public HashMap<String, Integer> howMuchTimeForBoost(String boostName, int boostQuantity) {
		WorldBoost bModel = World.BOOSTS.get(boostName);
		List<WorldGood> ratio = this.getGoodsRatio();
		HashMap<String,Integer> needs = new HashMap();
        // Get all needed goods, and time needed to produce them
		Iterator<WorldGood> bmit = bModel.getGoods().iterator();
		while(bmit.hasNext()) {
			WorldGood bGood = bmit.next();
			Iterator<WorldGood> it = ratio.iterator();
            boolean goodFound = false;
			while(it.hasNext()) {
				WorldGood wg = it.next();
				if(wg.getName().equals(bGood.getName())) {
                    // Skip if this good is in deficit
                    if (wg.getRealQuantity() < 0)
                        continue;
                    goodFound = true;
					// How much of that good do we need ?
					int qneed = bGood.getQuantity() * boostQuantity;
					// Real production by cycle
					double prod = wg.getRealQuantity();
					// Then, how much time for ?
					int rq = (int) (qneed * this.cycleTime / prod);
					// add to map
					needs.put(wg.getName(), rq);
				}
			}
            if (!goodFound) {
                needs.put(bGood.getName(), 0);
            }
		}
        return needs;
	}

    public List<String> getDistinctAdventureNames() {
        List<String> result = new ArrayList();
        List<Adventure> advs = this.getAllAdventures();
        Iterator<Adventure> it = advs.iterator();
        while (it.hasNext()) {
            Adventure adv = it.next();
            if (!result.contains(adv.name))
                result.add(adv.name);
        }
        return result;
    }

    public ArrayList<Adventure> getAllAdventures() {
        ArrayList<Adventure> adv = new ArrayList<Adventure>();
        // Add self adventures
        adv.addAll(this.adventures);
        // Add participations
        adv.addAll(this.participations);
        return adv;
    }

    public ArrayList<Adventure> getAllAdventuresByName(String advName) {
        ArrayList<Adventure> advlist = new ArrayList<Adventure>();
        advlist.addAll(this.getSelfAdventuresByName(advName));
        advlist.addAll(this.getParticipationAdventuresByName(advName));
        return advlist;
    }

    public ArrayList<Adventure> getSelfAdventuresByName(String advName) {
        ArrayList<Adventure> advlist = new ArrayList<Adventure>();
        // Add self adventures
        Iterator<Adventure> sit = this.adventures.iterator();
        while (sit.hasNext()) {
            Adventure adv = sit.next();
            if (adv.name.equals(advName))
                advlist.add(adv);
        }
        return advlist;
    }

    public ArrayList<Adventure> getParticipationAdventuresByName(String advName) {
        ArrayList<Adventure> advlist = new ArrayList<Adventure>();
        // Add participation adventures
        Iterator<Adventure> pit = this.participations.iterator();
        while (pit.hasNext()) {
            Adventure adv = pit.next();
            if (adv.name.equals(advName))
                advlist.add(adv);
        }
        return advlist;
    }

    @Deprecated
    public ArrayList<AdventureComment> getAllAdventureComments() {
        ArrayList<AdventureComment> comments = new ArrayList<AdventureComment>();
        Collections.sort(this.adventures, Adventure.BY_DATE_START_DESC);
        Iterator<Adventure> it = this.adventures.iterator();
        while (it.hasNext()) {
            Adventure a = it.next();
            Iterator<AdventureComment> cit = a.comments.iterator();
            while (cit.hasNext()) {
                AdventureComment ac = cit.next();
                if (ac.author.equals(this.name)) {
                    comments.add(ac);
                }
            }
        }
        return comments;
    }

    public static final Comparator<Player> BY_NAME = new Comparator<Player>() {
        @Override
        public int compare(Player p1, Player p2) {
            return p1.name.compareTo(p2.name);
        }
    };

    public static final Comparator<Player> BY_NAME_IGNORE_CASE = new Comparator<Player>() {
        @Override
        public int compare(Player p1, Player p2) {
            return p1.name.compareToIgnoreCase(p2.name);
        }
    };

}
