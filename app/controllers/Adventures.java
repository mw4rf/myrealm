package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.adventures.Adventure;
import models.adventures.AdventureComment;
import models.players.Player;
import models.players.PlayerAction;
import play.Logger;
import play.data.validation.Required;
import play.i18n.Messages;
import play.modules.log4play.LogStream;
import play.modules.paginate.ModelPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.With;
import play.mvc.Http.Request;
import world.World;
import world.WorldAdventure;

@With(Logs.class)
public class Adventures extends Controller {

	public static void index(String playerName) {
		// Get player
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// if(!Application.checkauth(player)) return; // AUTH

        ModelPaginator<Adventure> adventures = new ModelPaginator(Adventure.class, "player_id=?", player.id).orderBy("dateStart desc");
        adventures.setPageSize(20);

		// Available adventures
		WorldAdventure worldAdventures[] = new WorldAdventure[World.ADVENTURES.size()];
		worldAdventures = World.ADVENTURES.values().toArray(worldAdventures);
		Arrays.sort(worldAdventures, WorldAdventure.BY_NAME);

		// Today date
		@SuppressWarnings("unused")
		Date today = Calendar.getInstance().getTime();

		// Add visit
		player.addVisit(request.remoteAddress);
		// render
		render(player, adventures, worldAdventures, today);
	}

	public static void sortedIndex(String playerName, String sorting,
			String showonly, String selforparticipations, String limit) {
		// Get player
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);

		List<Adventure> adventuresList;
		if (showonly.isEmpty()) {
			if (selforparticipations.equals("SELF"))
				adventuresList = player.adventures;
			else if (selforparticipations.equals("PARTICIPATIONS"))
				adventuresList = player.participations;
			else
				adventuresList = player.getAllAdventures();
		} else {
			if (selforparticipations.equals("SELF"))
				adventuresList = player.getSelfAdventuresByName(showonly);
			else if (selforparticipations.equals("PARTICIPATIONS"))
				adventuresList = player.getParticipationAdventuresByName(showonly);
			else
				adventuresList = player.getAllAdventuresByName(showonly);
		}

		// Limit
		if (!limit.isEmpty()) {
			int ilimit = Integer.parseInt(limit);
			if (adventuresList.size() > ilimit)
				adventuresList = adventuresList.subList(adventuresList.size() - ilimit,
						adventuresList.size());
		}

		// Sorting
		if (adventuresList.size() > 0)
			if (sorting.equals("name-asc"))
				Collections.sort(adventuresList, Adventure.BY_NAME);
			else if (sorting.equals("name-desc"))
				Collections.sort(adventuresList, Adventure.BY_NAME_DESC);
			else if (sorting.equals("dstart-asc"))
				Collections.sort(adventuresList, Adventure.BY_DATE_START);
			else if (sorting.equals("dstart-desc"))
				Collections.sort(adventuresList, Adventure.BY_DATE_START_DESC);
			else if (sorting.equals("dend-asc"))
				Collections.sort(adventuresList, Adventure.BY_DATE_END);
			else if (sorting.equals("dend-desc"))
				Collections.sort(adventuresList, Adventure.BY_DATE_END_DESC);

		// Available adventures
		WorldAdventure worldAdventures[] = new WorldAdventure[World.ADVENTURES
				.size()];
		worldAdventures = World.ADVENTURES.values().toArray(worldAdventures);
		Arrays.sort(worldAdventures, WorldAdventure.BY_NAME);

		// Today date
		@SuppressWarnings("unused")
		Date today = Calendar.getInstance().getTime();

		// Add visit
		player.addVisit(request.remoteAddress);
		
		// Paginate
		ValuePaginator<Adventure> adventures = new ValuePaginator<Adventure>(adventuresList);
		adventures.setPageSize(20);
		
		// render
		renderTemplate("Adventures/_adventuresList.html", player, adventures,
				worldAdventures, today);
	}

	public static void add(String playerName) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// Available adventures
		WorldAdventure worldAdventures[] = new WorldAdventure[World.ADVENTURES
				.size()];
		worldAdventures = World.ADVENTURES.values().toArray(worldAdventures);
		Arrays.sort(worldAdventures, WorldAdventure.BY_NAME);

		// Today date
		@SuppressWarnings("unused")
		Date today = Calendar.getInstance().getTime();
		render("Adventures/forms/add.html", player, today, worldAdventures);
	}

	public static void addAdventure(@Required String playerName,
			@Required String name, @Required Date dateStart,
			@Required Date dateEnd, String notes) {
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		if (!Application.checkauth(player))
			return; // AUTH

		// Fields validation
		validation.required(name)
				.message("validation.required.adventures.name");
		validation.required(dateStart).message(
				"validation.required.adventures.dateStart");
		validation.required(dateEnd).message(
				"validation.required.adventures.dateEnd");
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			Adventures.index(playerName);
		}
		// do it
		Adventure a = player.addAdventure(name, dateStart, dateEnd, notes);
		// Flash & Log
		flash.success(Messages.get("flash.success.adventure.add", playerName,
				name));
		player.logAction(PlayerAction.LOG_ADVENTURES_ADD_ADVENTURE, "" + a.id); // LOG
		// render
		showAdventure(playerName, a.id);
	}

	public static void addNextAdventure(@Required String playerName,
			@Required String name, @Required Date dateStart,
			@Required Date dateEnd, String notes) {
		// Fields validation
		validation.required(name)
				.message("validation.required.adventures.name");
		validation.required(dateStart).message(
				"validation.required.adventures.dateStart");
		validation.required(dateEnd).message(
				"validation.required.adventures.dateEnd");
		validation.future(dateStart).message(
				"validation.future.adventures.dateStart");
		validation.future(dateEnd).message(
				"validation.future.adventures.dateEnd");
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			Application.nextAdventuresList();
		} else {
			// do it
			Player player = Player.find(Application.getSessionLogin());
			Controller.notFoundIfNull(player);
			Adventure a = player.addAdventure(name, dateStart, dateEnd, notes);
			// Flash & Log
			flash.success(Messages.get("flash.success.adventure.add",
					playerName, name));
			player.logAction(PlayerAction.LOG_ADVENTURES_ADD_ADVENTURE, ""
					+ a.id); // LOG
			// render
			showAdventure(player.name, a.id);
		}
	}

	public static void showAdventure(String playerName, Long adventureId) {
		// Player
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		// if(!Application.checkauth(player)) return; // AUTH
		// Adventure
		Adventure adventure = Adventure.findById(adventureId);
		// Available adventures
		WorldAdventure worldAdventures[] = new WorldAdventure[World.ADVENTURES
				.size()];
		worldAdventures = World.ADVENTURES.values().toArray(worldAdventures);
		Arrays.sort(worldAdventures, WorldAdventure.BY_NAME);
		// Show edit links ?
		boolean canWrite = false;
		if (!Application.isAnonymous())
			canWrite = true;
		// User
		String userName = Application.getSessionLogin();
		// Sorts comments by date (old to recend)
		Collections.sort(adventure.comments, AdventureComment.BY_DATE_ASC);
		// Add visit
		player.addVisit(request.remoteAddress);
		// render
		render(player, userName, adventure, worldAdventures, canWrite);
	}

	public static void editAdventure(String playerName, Long adventureId,
			@Required String name, @Required Date dateStart,
			@Required Date dateEnd, String notes) {
		// Load stuff
		Adventure adventure = Adventure.findById(adventureId);
		Player ctrl = Player.find(playerName);
		Controller.notFoundIfNull(ctrl);
		Controller.notFoundIfNull(adventure);
		if (!Application.checkauth(ctrl) && !adventure.isParticipant(ctrl))
			return; // AUTH
		// Fields validation
		validation.required(name)
				.message("validation.required.adventures.name");
		validation.required(dateStart).message(
				"validation.required.adventures.dateStart");
		validation.required(dateEnd).message(
				"validation.required.adventures.dateEnd");
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			showAdventure(playerName, adventureId);
		}
		// Do update
		adventure.name = name;
		adventure.dateStart = dateStart;
		adventure.dateEnd = dateEnd;
		adventure.notes = notes;
		adventure.save();
		// Log & flash
		adventure.player.logAction(PlayerAction.LOG_ADVENTURES_EDIT_ADVENTURE,
				"" + adventure.id); // LOG
		flash.success(Messages.get("flash.success.adventure.update"));
		// Render
		showAdventure(playerName, adventureId);
	}

	public static void deleteAdventure(String playerName, Long adventureId) {
		// Load stuff
		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		if (!Application.checkauth(player))
			return; // AUTH
		Adventure adventure = player.deleteAdventure(adventureId);
		// Flash
		flash.success(Messages.get("flash.success.adventure.delete",
				adventure.name));
		// Render
		Adventures.index(playerName);
	}

	public static void addParticipant(String playerName, Long adventureId,
			String participant) {
		if (playerName.isEmpty())
			return;

		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		if (!Application.checkauth(player))
			return; // AUTH

		Adventure adventure = Adventure.findById(adventureId);
		if (adventure == null)
			return;

		Player par = Player.find(participant);
		if (par == null)
			par = new Player(participant).save();

		// Can't add yourself
		if (playerName.equals(participant))
			return;
		// Can add twice the same person
		if (adventure.participants.contains(par))
			return;

		adventure.participants.add(par);
		adventure.save();

		if (par.participations == null)
			par.participations = new ArrayList<Adventure>();

		par.participations.add(adventure);
		par.save();

		adventure.player.logAction(PlayerAction.LOG_ADVENTURES_ADD_PARTICIPANT,
				"" + adventure.id); // LOG
		par.logAction(PlayerAction.LOG_ADVENTURES_JOIN, "" + adventure.id); // LOG

		renderTemplate("Adventures/_participants.html", adventure, player);
	}

	public static void removeParticipant(String playerName, Long adventureId,
			String participant) {
		if (playerName.isEmpty())
			return;

		Player player = Player.find(playerName);
		Controller.notFoundIfNull(player);
		if (!Application.checkauth(player))
			return; // AUTH

		Adventure adventure = Adventure.findById(adventureId);
		if (adventure == null)
			return;

		System.out.println("got there  " + participant);

		Player par = Player.find(participant);
		if (par == null)
			return;

		boolean res = adventure.participants.remove(par);
		System.out.println("participant removed : " + res);
		adventure.save();

		par.participations.remove(adventure);
		par.save();

		adventure.player.logAction(
				PlayerAction.LOG_ADVENTURES_REMOVE_PARTICIPANT, ""
						+ adventure.id); // LOG
		par.logAction(PlayerAction.LOG_ADVENTURES_LEAVE, "" + adventure.id); // LOG

		renderTemplate("Adventures/_participants.html", adventure, player);
	}

	public static void addComment(Long adventureId, String author, String text) {
		Adventure adv = Adventure.findById(adventureId);
		Controller.notFoundIfNull(adventureId);
		adv.addComment(author, text);

		AdventureComment comment = adv.comments.get(adv.comments.size() - 1);

		Player authorp = Player.find(author);
		Controller.notFoundIfNull(authorp);
		if (authorp != null)
			authorp.logAction(PlayerAction.LOG_ADVENTURE_ADD_COMMENT, ""
					+ adv.id); // LOG

		// Show edit links ?
		boolean canWrite = false;
		if (Application.isAuth(comment.author)
				|| (Application.isAuth(Application.getSessionLogin()) && comment.adventure
						.isParticipant(Application.getSessionLogin())))
			canWrite = true;

		// showAdventure(adv.player.name, adv.id);
		String display = "block";
		renderTemplate("Adventures/_comment.html", comment, display, canWrite);
	}

	public static void editComment(Long commentId, String text) {
		AdventureComment comment = AdventureComment.findById(commentId);
		Controller.notFoundIfNull(comment);
		Player authorp = Player.find(comment.author);
		Controller.notFoundIfNull(authorp);

		if (!Application.checkauth(authorp))
			return; // AUTH

		comment.content = text;
		comment.save();

		authorp.logAction(PlayerAction.LOG_ADVENTURES_EDIT_COMMENT, ""
				+ comment.adventure.id); // LOG

		// Show edit links ?
		boolean canWrite = false;
		if (Application.isAuth(comment.author)
				|| (Application.isAuth(Application.getSessionLogin()) && comment.adventure
						.isParticipant(Application.getSessionLogin())))
			canWrite = true;

		// display
		String display = "none";
		renderTemplate("Adventures/_comment.html", comment, display, canWrite);
	}

	public static void nextAdventuresList() {
    	Player player = Player.find(Application.getSessionLogin());
    	Controller.notFoundIfNull(player);
    	Date today = new Date();
		// Available adventures
		List<WorldAdventure> worldAdventures = new ArrayList<WorldAdventure>();
		Iterator<WorldAdventure> it = World.ADVENTURES.values().iterator();
		while(it.hasNext()) {
			WorldAdventure wo = it.next();
			if(wo.isMultiplayer())
				worldAdventures.add(wo);
		}
		// Registered adventures
        List<Adventure> adventuresList = Adventure.findStartInTheFuture();
        Collections.sort(adventuresList, Adventure.BY_DATE_START);
		ValuePaginator<Adventure> adventures = new ValuePaginator<Adventure>(adventuresList);
		adventures.setPageSize(20);
        render("Adventures/nextAdventures.html", worldAdventures, adventures, player, today);
	}

}
