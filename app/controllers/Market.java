package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import models.market.MarketAdventureComment;
import models.market.MarketAdventureOffer;
import models.market.MarketAdventureSale;
import models.market.MarketBoostComment;
import models.market.MarketBoostExchange;
import models.players.Player;
import models.players.PlayerAction;
import models.realm.Building;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;
import world.World;
import world.WorldAdventure;
import world.WorldBoost;

@With(Logs.class)
public class Market extends Controller {

	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//------------ PUBLIC PAGES
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Public Market index page
	 */
	public static void index() {
		Player player = Player.find(Application.getSessionLogin());
		if(player == null)
			render();
		else
			render(player);
	}
	
	/**
	 * ADVENTURES public market
	 */
	public static void adventuresPublic() {
		// Current offers
		List<MarketAdventureSale> sales = MarketAdventureSale.find("startAt < NOW() AND endAt > NOW() AND itemQuantity > 0 ORDER BY endAt desc").fetch();
		// Show add sale form ? only for registered players
		boolean canBuyAndSell = false;
		Player player = Player.find(Application.getSessionLogin());
		if(player != null)
			canBuyAndSell = true;
		// Flash
		if(!canBuyAndSell)
			flash.put("info", Messages.get("flash.info.market.must.login"));
		// render
		render("Market/adventures/public/index.html", sales, canBuyAndSell, player);
	}
	
	/**
	 * ADVENTURES private market = List of player's SALES
	 */
	public static void adventuresPrivateSales(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Current sales
		List<MarketAdventureSale> sales = MarketAdventureSale.find("seller = ? ORDER BY endAt desc", player).fetch();
		// render
		render("Market/adventures/private/sales.html", sales, player);
	}
	
	/**
	 * ADVENTURES private market = List of player's PURCHASES
	 */
	public static void adventuresPrivatePurchases(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Current sales
		List<MarketAdventureOffer> offers = MarketAdventureOffer.find("buyer = ? ORDER BY madeAt desc", player).fetch();
		// render
		render("Market/adventures/private/purchases.html", offers, player);
	}
	
	/**
	 * ADVENTURES add new sale
	 */
	public static void adventuresSale(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Available adventures
		WorldAdventure worldAdventures[] = new WorldAdventure[World.ADVENTURES.size()];
		worldAdventures = World.ADVENTURES.values().toArray(worldAdventures);
		Arrays.sort(worldAdventures, WorldAdventure.BY_NAME);
		// render
		render("Market/adventures/forms/addSale.html", player, worldAdventures);
	}

	/**
	 * Index page for Boost Exchanges
	 */
	public static void boosts() {
		// Get player
		boolean canBuyAndSell = false;
		Player player = Player.find(Application.getSessionLogin());
		if(player != null) {
			canBuyAndSell = true;
		} else {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
		}

		// World Boosts
		List<WorldBoost> worldBoosts = new ArrayList<WorldBoost>();
		Iterator<WorldBoost> it = World.BOOSTS.values().iterator();
		while(it.hasNext()) {
			WorldBoost wb = it.next();
			if(wb.isBoost())
				worldBoosts.add(wb);
		}

		// Get player's buildings
		SortedSet<String> buildingTypes;
		try {
			buildingTypes = new TreeSet<String>(player.getBuildingsSet().keySet());
		} catch(Exception e) {
			buildingTypes = new TreeSet<String>(World.BOOSTS.keySet());
		}
		

		// Get player requests
		List<MarketBoostExchange> boostsToReceive = player.boostsToReceive;

		// Get player boosts to give
		List<MarketBoostExchange> boostsToGive = player.boostsToGive;

		// get public requests
		List<MarketBoostExchange> publicRequests = MarketBoostExchange.getCurrentRequests();

		// render
		render(player, worldBoosts, buildingTypes, boostsToReceive, boostsToGive, publicRequests);
	}

	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//------------ BOOSTS EXCHANGE
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static void boost(@Required Long id) {
		MarketBoostExchange exchange = MarketBoostExchange.findById(id);
		// get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		boolean isReceiver = false;
		boolean isGiver = false;
		if(exchange.giver != null && exchange.giver.name.equals(player.name))
			isGiver = true;
		else if(exchange.receiver.name.equals(player.name))
			isReceiver = true;

		// Get player's buildings
		SortedSet<String> buildingTypes = new TreeSet<String>(player.getBuildingsSet().keySet());

		// Comments
		List<MarketBoostComment> comments = exchange.comments;
		Collections.sort(comments, MarketBoostComment.BY_DATE_ASC);

		render(exchange, player, isGiver, isReceiver, buildingTypes, comments);
	}

	/**
	 * Add a new boost request
	 * @param boostName
	 * @param quantity
	 * @param buildingName
	 * @param boostAt
	 */
	public static void addBoostExchange(@Required String boostName, @Required int quantity, @Required String buildingName, @Required Date boostAt) {
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// Do it
		MarketBoostExchange mbe = new MarketBoostExchange(player, boostName, quantity, buildingName, boostAt).save();
		player.boostsToReceive.add(mbe);
		player.save();
		// redirect
		boosts();
	}

	/**
	 * Add a new boost request, as a counterpart of an accepted offer
	 * Only Giver can do that
	 * @param exchangeId
	 * @param boostName
	 * @param quantity
	 * @param buildingName
	 * @param boostAt
	 */
	public static void addBoostExchangeCounterpart(@Required Long exchangeId, @Required String boostName, @Required int quantity, @Required String buildingName, @Required Date boostAt) {
		MarketBoostExchange mbe = MarketBoostExchange.findById(exchangeId);
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null || !player.boostsToGive.contains(mbe)) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// New exchange
		MarketBoostExchange ct = new MarketBoostExchange(player, boostName, quantity, buildingName, boostAt);
		ct.giver = mbe.receiver;
		ct.hasCounterpart = true;
		ct.accepted = true;
		ct.save();
		mbe.hasCounterpart = true;
		mbe.save();
		player.boostsToReceive.add(ct);
		ct.giver.boostsToGive.add(ct);
		player.save();
		ct.giver.save();
		// redirect
		boost(exchangeId);
	}

	/**
	 * Delete a boost exchange request
	 * Only Receiver can do that
	 * @param id
	 */
	public static void removeBoostExchange(@Required Long id) {
		MarketBoostExchange mbe = MarketBoostExchange.findById(id);
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null || mbe == null || !player.boostsToReceive.contains(mbe)) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// do it
		player.boostsToReceive.remove(mbe);
		player.save();
		mbe.delete();
		// redirect
		boosts();
	}

	/**
	 * Accept a public boost exchange request
	 * Only Giver can do that
	 * @param id
	 */
	public static void acceptBoostExchange(@Required Long id) {
		MarketBoostExchange mbe = MarketBoostExchange.findById(id);
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null || mbe == null || player.boostsToReceive.contains(mbe)) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// do it
		mbe.giver = player;
		mbe.accepted = true;
		mbe.save();
		player.boostsToGive.add(mbe);
		player.save();
		// redirect
		boost(id);
	}

	/**
	 * Mark boost exchange as done. 
	 * Both Giver and Receiver can do that.
	 * @param id
	 */
	public static void setBoostExchangeDone(@Required Long id) {
		MarketBoostExchange mbe = MarketBoostExchange.findById(id);
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null || mbe == null || ( !player.boostsToReceive.contains(mbe) && !player.boostsToGive.contains(mbe) ) ) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// do it
		mbe.done = true;
		mbe.save();
		// redirect
		boosts();
	}

	/**
	 * Withdraw from an accepted boost exchange. The request goes back to public market.
	 * Only Receiver can do that.
	 * @param id
	 */
	public static void withdrawBoostExchange(@Required Long id) {
		MarketBoostExchange mbe = MarketBoostExchange.findById(id);
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null || mbe == null || player.boostsToReceive.contains(mbe) || !player.boostsToGive.contains(mbe)) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// do it
		mbe.giver = null;
		mbe.accepted = false;
		mbe.save();
		player.boostsToGive.remove(mbe);
		player.save();
		// redirect
		boosts();
	}

	/**
	 * Add a new message in the boost exchange forum (details page)
	 * @param exchangeId
	 * @param comment
	 */
	public static void addBoostExchangeComment(@Required Long exchangeId, String comment) {
		MarketBoostExchange mbe = MarketBoostExchange.findById(exchangeId);
		// Get player
		Player player = Player.find(Application.getSessionLogin());
		if(player == null || mbe == null) {
			flash.error(Messages.get("flash.error.market.must.login"));
			Application.index();
			return;
		}
		// add comment
		mbe.postComment(Application.getSessionLogin(), comment);
		// redirect
		boost(exchangeId);
	}

	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//------------ ADVENTURES
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * See ADVENTURE sale details
	 * @param id
	 */
	public static void adventureDetails(Long id) {
		// get the sale
		MarketAdventureSale sale = MarketAdventureSale.findById(id);
		// Show add sale form ? only for registered players
		boolean canBuyAndSell = false;
		String playerName = Application.getSessionLogin();
		if(Player.find(playerName) != null)
			canBuyAndSell = true;
		// Comments
		List<MarketAdventureComment> comments = sale.comments;
		Collections.sort(comments, MarketAdventureComment.BY_DATE_ASC);
		// render
		Player player = Player.find(Application.getSessionLogin());
		if(player == null)
			render("Market/adventures/details.html", sale, comments, canBuyAndSell);
		else
			render("Market/adventures/details.html", sale, comments, canBuyAndSell, player);
	}

	/**
	 * Create a new ADVENTURE sale
	 * @param playerName
	 * @param itemName
	 * @param itemQuantity
	 * @param startAt
	 * @param endAt
	 * @param description
	 */
	public static void adventuresAddSale(String playerName, String itemName, int itemQuantity, Date startAt, Date endAt, String description) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		
		MarketAdventureSale sale = new MarketAdventureSale(player, startAt, endAt, itemName, itemQuantity, description, MarketAdventureSale.TYPE_ADVENTURE).save();
		player.adventureSales.add(sale);
		player.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.market.adventure.add"));
        player.logAction(PlayerAction.LOG_MARKET_ADVENTURE_ADD, "" + sale.id); // LOG
        
        // render
		Market.adventuresPrivateSales(playerName);
	}

	/**
	 * Buy ADVENTURE
	 * @param id
	 */
	public static void buyAdventure(Long id) {
		MarketAdventureSale sale = MarketAdventureSale.findById(id);
		Player player = Player.find(Application.getSessionLogin());
		// security
		if(player == null)
			return;
		// buy
		MarketAdventureOffer offer = new MarketAdventureOffer(player, sale).save();
		sale.offers.add(offer);
		sale.save();
		player.adventureOffers.add(offer);
		player.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.market.adventure.buy", sale.itemName));
        player.logAction(PlayerAction.LOG_MARKET_ADVENTURE_BUY, "" + sale.id); // LOG
		// render
		adventureDetails(id);
	}

	/**
	 * Accept ADVENTURE sale offer
	 * The seller accept to sell to this buyer.
	 * @param id
	 */
	public static void acceptAdventureOffer(Long id) {
		MarketAdventureOffer offer = MarketAdventureOffer.findById(id);
		Player player = Player.find(Application.getSessionLogin());
		// security
		if(!offer.sale.isSeller(player.name))
			return;
		// accept
		offer.sold = true;
		offer.answeredAt = new Date();
		offer.save();
		offer.sale.itemQuantity--;
		offer.sale.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.market.adventure.accept", player.name));
        player.logAction(PlayerAction.LOG_MARKET_ADVENTURE_ACCEPT, "" + offer.sale.id); // LOG
		// render
        adventureDetails(offer.sale.id);
	}

	/**
	 * Reject ADVENTURE sale offer
	 * The seller refuses to sell to this buyer.
	 * @param id
	 */
	public static void rejectAdventureOffer(Long id) {
		MarketAdventureOffer offer = MarketAdventureOffer.findById(id);
		Player player = Player.find(Application.getSessionLogin());
		// security
		if(!offer.sale.isSeller(player.name))
			return;
		// accept
		offer.sold = false;
		offer.answeredAt = new Date();
		offer.save();
        // Log & Flash
        flash.success(Messages.get("flash.success.market.adventure.reject", player.name));
        player.logAction(PlayerAction.LOG_MARKET_ADVENTURE_REJECT, "" + offer.sale.id); // LOG
		// render
        adventureDetails(offer.sale.id);
	}

	/**
	 * Withdraw ADVENTURE sale offer
	 * The buyer withdraws, he doesn't buy anymore.
	 * @param id
	 */
	public static void withdrawAdventureOffer(Long id) {
		MarketAdventureOffer offer = MarketAdventureOffer.findById(id);
		Player player = Player.find(Application.getSessionLogin());
		// security
		if(!offer.sale.isBuyer(player.name))
			return;
		// accept
		MarketAdventureSale sale = offer.sale;
		sale.offers.remove(offer);
		sale.save();
		offer.buyer.adventureOffers.remove(offer);
		offer.buyer.save();
		offer.delete();
        // Log & Flash
        flash.success(Messages.get("flash.success.market.adventure.withdraw"));
        player.logAction(PlayerAction.LOG_MARKET_ADVENTURE_WITHDRAW, "" + offer.sale.id); // LOG
		// render
        adventureDetails(sale.id);
	}

	public static void addComment(Long saleId, String comment) {
		MarketAdventureSale sale = MarketAdventureSale.findById(saleId);
		sale.addComment(Application.getSessionLogin(), comment);
        // Log & Flash
        //flash.success(Messages.get("flash.success.market.adventure.withdraw"));
        //player.logAction(PlayerAction.LOG_MARKET_ADVENTURE_WITHDRAW, "" + offer.sale.id); // LOG
		// render
		adventureDetails(saleId);
	}

}
