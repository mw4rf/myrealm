package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.guilds.Guild;
import models.guilds.GuildEvent;
import models.guilds.GuildLink;
import models.guilds.GuildOfficerNote;
import models.guilds.GuildOldMember;
import models.guilds.GuildWallMessage;
import models.guilds.Membership;
import models.players.Player;
import models.players.PlayerAction;
import models.polls.Poll;
import models.polls.PollOption;
import play.Logger;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.With;
import world.World;
import world.WorldAvatar;
import world.WorldGuild;

@With(Logs.class)
public class Guilds extends Controller {

    public static void index() {

        List<Guild> guilds = Guild.findAll();

        render(guilds);
    }

    /**
     * {@link Guild} public home page.
     * @param guildTag
     */
    public static void home(String guildTag) {
        Guild guild = Guild.findByTag(guildTag);
        // Security
        Controller.notFoundIfNull(guild);
        
        //if(!Guilds.checkOffAuth(guild))
        //	return;

        // Roll O'Day member
        //guild.rollODayMember(); // now done with cron
        Player odaymember = Player.find(guild.odayMemberName);

        // Members
        List<Membership> members = guild.getConfirmedMembers();
        Collections.sort(members, Membership.BY_RANK_DESC);

        // Sort wall messages
        Collections.sort(guild.wall, GuildWallMessage.BY_DATE_DESC);
        // Limit messages to the last 10
        List<GuildWallMessage> wall = guild.wall;
        if (wall.size() > 10)
            wall = guild.wall.subList(0, 10);

        // Is content editable ?
        Player p = Player.find(Application.getSessionLogin());
        boolean isOfficer = false;
        boolean isMember = guild.isMember(p);
        if (isMember && p.membership.isOfficer())
            isOfficer = true;

        boolean contenteditable = isOfficer;

        boolean isGuildMaster = Guilds.checkAdminAuth(guild);

        // Officers notes
        List<GuildOfficerNote> lastofficernotes = new ArrayList<GuildOfficerNote>();
        if(isOfficer) {
        	lastofficernotes = GuildOfficerNote.getOfficerNotesForGuild(guild.id);
        	Collections.sort(lastofficernotes, GuildOfficerNote.BY_DATE_DESC);
            if (lastofficernotes.size() > 5)
            	lastofficernotes = lastofficernotes.subList(0, 5);
        }

        // Polls
        Poll poll = null;
        if(guild.polls != null && guild.polls.size() > 0)
        	poll = guild.polls.get(guild.polls.size() - 1);

        Player voter = Player.find(Application.getSessionLogin());
        if(!guild.isMember(voter))
        	voter = null;

        // Events
        List<GuildEvent> events = guild.getFutureEvents();
        Collections.sort(events, GuildEvent.BY_DATE_START_ASC);

        // Links
        Collections.sort(guild.links, GuildLink.BY_WEIGHT_ASC);

        render(guild, members, wall, odaymember, contenteditable, isMember, isOfficer, isGuildMaster, lastofficernotes, poll, voter, events);
    }

    /**
     * {@link Guild} administration page. Only Founder and Officers can see this page.
     * @param guildTag
     */
    public static void admin(String guildTag) {
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	// Find player
    	Player player = Player.find(Application.getSessionLogin());
    	Controller.notFoundIfNull(player);
    	// Check rights
    	if(!Guilds.checkAdminAuth(guild)) {
    		home(guildTag);
    		return;
    	}
    	// Get guild logos
        WorldGuild guilds[] = new WorldGuild[World.GUILDS.size()];
        guilds = World.GUILDS.values().toArray(guilds);
        // Get links & sort them
        List<GuildLink> links = guild.links;
        Collections.sort(links, GuildLink.BY_WEIGHT_ASC);
    	// Sort collections
        Arrays.sort(guilds, WorldGuild.BY_NAME);
    	Collections.sort(guild.wall, GuildWallMessage.BY_DATE_DESC);
    	// Members
    	List<GuildOldMember> oldMembers = guild.oldmembers;
    	List<Membership> members = guild.getConfirmedMembers();
    	List<Membership> unconfirmedMembers = guild.getUnconfirmedMembers();
    	Collections.sort(oldMembers, GuildOldMember.BY_LEFT_DATE_DESC);
    	Collections.sort(members, Membership.BY_NAME_ASC);
    	Collections.sort(unconfirmedMembers, Membership.BY_JOIN_DATE_DESC);
    	// render
    	render(guilds, guild, links, oldMembers, members, unconfirmedMembers);
    }

    public static void sortMembersList(Long guildId, int sorting, int order) {
    	try {
    	Guild guild = Guild.findById(guildId);
    	Controller.notFoundIfNull(guild);
    	List<Membership> members = guild.members;
    	Controller.notFoundIfNull(members);
    	boolean ASC;
    	if(order != 0)
    		ASC = true;
    	else
    		ASC = false;
    	// Sort
    	switch(sorting) {
    		case 1:
    			if(ASC)
    				Collections.sort(members, Membership.BY_RANK_ASC);
    			else
    				Collections.sort(members, Membership.BY_RANK_DESC);
    			break;
    		case 2:
    			if(ASC)
    				Collections.sort(members, Membership.BY_NAME_ASC);
    			else
    				Collections.sort(members, Membership.BY_NAME_DESC);
    			break;
    		case 3:
    			if(ASC)
    				Collections.sort(members, Membership.BY_JOIN_DATE_ASC);
    			else
    				Collections.sort(members, Membership.BY_JOIN_DATE_DESC);
    			break;
    		case 4:
    			if(ASC)
    				Collections.sort(members, Membership.BY_LAST_CONNECTION_ASC);
    			else
    				Collections.sort(members, Membership.BY_JOIN_CONNECTION_DESC);
    			break;
    	}
    	// Show officer notes ?
        Player p = Player.find(Application.getSessionLogin());
        boolean isOfficer = false;
        boolean isMember = guild.isMember(p);
        if (isMember && p.membership.isOfficer())
            isOfficer = true;
    	// render
    	renderTemplate("Guilds/_membersList.html", guild, members, isMember, isOfficer);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

	public static void officerNotes(String guildTag, String playerName) {
    	// Get Player & player's guild
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	// Safety against null pointer exception
    	if(player == null || guild == null)
    		return;
    	// Safety : only officers can view this page
    	Player officer = Player.find(Application.getSessionLogin());
    	if (!guild.isMember(officer) || !officer.membership.isOfficer())
    		return;
    	// Ok !
    	List<GuildOfficerNote> notes = new ArrayList<GuildOfficerNote>();
    		notes = player.membership.officernotes;
        	Collections.sort(notes, GuildOfficerNote.BY_DATE_ASC);
    	// render
    	render(guild, player, officer, notes);
    }

    // FORMS

    public static void addOfficerNote(String guildTag, String playerName, String content) {
    	if(content == null || content.isEmpty())
    		return;
    	// Get Player & player's guild
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	// Safety : only officers can view this page
    	Player officer = Player.find(Application.getSessionLogin());
    	if (!guild.isMember(officer) || !officer.membership.isOfficer())
    		return;
    	// Ok !
    	player.membership.addOfficerNote(officer.name, content);
    	// Flash
    	flash.success(Messages.get("flash.success.officer.note.added"));
    	// render
    	officerNotes(guildTag, playerName);
    }

    /**
     * The {@link Player} will join the {@link Guild}.
     * @param playerName
     * @param guildId
     */
    public static void joinGuild(String playerName, Long guildId) {
    	Player player = Player.find(playerName);
    	Guild guild = Guild.findById(guildId);
    	// Security
    	Controller.notFoundIfNull(player);
    	Controller.notFoundIfNull(guild);
    	// Do the job
    	if(guild.isMember(player))
    		return;
    	if(guild.addPlayer(playerName)) {
        	flash.success(Messages.get("flash.success.guild.join", "[" + guild.tag +"] " + guild.name));
        	player.logAction(PlayerAction.LOG_GUILD_JOIN, "" + guild.id); // LOG
    	} else {
    		flash.error(Messages.get("flash.error.guild.join", "[" + guild.tag +"] " + guild.name, "[" + player.membership.guild.tag +"] " + player.membership.guild.name));
    	}
    	Guilds.home(guild.tag);
    }

    /**
     * Leave the current guild.
     * <br />This method is called by the {@link Player} identified by <b>playerName</b> from his {@link Home} page.
     * <br />The {@link Membership} association between the {@link Player} and the {@link Guild} is deleted.
     * @param playerName
     */
    public static void leaveGuild(String playerName) {
    	// Load data
    	Player player = Player.find(playerName);
    	Guild guild = player.membership.guild;
    	// Security
    	Controller.notFoundIfNull(player);
    	Controller.notFoundIfNull(guild);
    	// Do the job
    	guild.removePlayer(playerName);
    	// Advert
    	flash.success(Messages.get("flash.success.guild.leave", "[" + guild.tag +"] " + guild.name));
    	player.logAction(PlayerAction.LOG_GUILD_LEAVE, "" + guild.id); // LOG
    	// Render
    	Home.guild(playerName);
    }

    /**
     * Creates a new guild and sets the player as its founder.
     * The form is validated: guild NAME and TAG must not 1) be empty, 2) already exist in the database.
     * Redirects to {@link Guilds}.home after guild creation.
     * @param playerName
     * @param guildName
     * @param guildTag
     */
    public static void createGuild(String playerName, String guildName, String guildTag) {
    	// Guild name & tag can't be empty
        validation.required(guildName).message("validation.required.guild.name");
        validation.required(guildTag).message("validation.required.guild.tag");
        // Guild name or tag can't already exist
        Guild testTag = Guild.findByTag(guildTag);
        Guild testName = Guild.findByName(guildName);
        boolean alreadyExist = false;
        if(testTag != null) { // guild already exist
        	flash.error(Messages.get("validation.guild.tag.already.exist"));
        	alreadyExist = true;
        }
        if(testName != null) {
        	flash.error(Messages.get("validation.guild.name.already.exist"));
        	alreadyExist = true;
        }
        // Process validation
        if (validation.hasErrors() || alreadyExist) {
            params.flash(); // add http parameters to the flash scope
            validation.keep(); // keep the errors for the next request
            Home.guild(playerName);
        }
    	// Validation passed: create guild
    	Player player = Player.find(playerName);
    	Controller.notFoundIfNull(player);
    	Guild guild = new Guild(guildName, guildTag).save();
    	// Join guild and be the founder
    	guild.addPlayer(playerName);
    	player.membership.rank = Membership.RANK_4_GUILDMASTER;
    	player.membership.confirmed = true;
    	player.membership.save();
    	// roll
    	guild.cronRollODayMember();
    	// Flash & log
    	flash.put("info", Messages.get("flash.info.guild.create", "[" + guild.tag +"] " + guild.name));
    	player.logAction(PlayerAction.LOG_GUILD_CREATE, "" + guild.id); // LOG
    	// Redirect
    	Guilds.home(guild.tag);
    }

    public static void adminUpdateGuild(String name, String tag, String banner, boolean officersCanAdmin) {
    	Guild guild = Guild.findByTag(tag);
    	Controller.notFoundIfNull(guild);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// Do it
    	guild.name = name;
    	guild.tag = tag;
    	guild.bannerId = banner;
    	guild.officersCanAdmin = officersCanAdmin;
    	guild.save();
    	// redirect
    	admin(tag);
    }

    public static void adminAddMember(Long guildId, String playerName) {
    	Guild guild = Guild.findById(guildId);
    	Controller.notFoundIfNull(guild);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// Check if player exists and has no membership already
    	Player player = Player.find(playerName);
    	if(player == null || player.membership != null)
    		admin(guild.tag);
    	// ok, proceed
    	guild.addPlayer(playerName);
    	admin(guild.tag);
    }

    public static void adminPromoteMember(String guildTag, Long membershipId) {
    	Membership membership = Membership.findById(membershipId);
    	Controller.notFoundIfNull(membership);
    	// Check auth
    	if(!Guilds.checkAdminAuth(membership.guild))
    		home(membership.guild.tag);
    	// do the job
    	membership.promote();
    	// redirect
    	admin(membership.guild.tag);
    }

    public static void adminDemoteMember(String guildTag, Long membershipId) {
    	Membership membership = Membership.findById(membershipId);
    	Controller.notFoundIfNull(membership);
    	// Check auth
    	if(!Guilds.checkAdminAuth(membership.guild))
    		home(membership.guild.tag);
    	// do the job
    	membership.demote();
    	// redirect
    	admin(membership.guild.tag);
    }

    public static void adminConfirmMember(String guildTag, Long membershipId) {
    	Membership membership = Membership.findById(membershipId);
    	Controller.notFoundIfNull(membership);
    	// Check auth
    	if(!Guilds.checkAdminAuth(membership.guild))
    		home(membership.guild.tag);
    	// do the job
    	membership.confirm();
    	// redirect
    	admin(membership.guild.tag);
    }

    public static void adminKickMember(String guildTag, Long membershipId, String reason) {
    	Membership membership = Membership.findById(membershipId);
    	Controller.notFoundIfNull(membership);
    	// Check auth
    	if(!Guilds.checkAdminAuth(membership.guild))
    		home(membership.guild.tag);
    	// do the job
    	Guild g = membership.guild;
    	g.removePlayer(membership.player.name, reason, true);
    	// redirect
    	admin(g.tag);
    }

    public static void adminAddPoll(Long guildId, String question, Date startAt, Date endAt, boolean enable) {
    	Guild guild = Guild.findById(guildId);
    	Controller.notFoundIfNull(guild);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// do it
    	Poll poll = new Poll(question, startAt, endAt);
    	poll.enable = enable;
    	poll.guild = guild;
    	poll.save();
    	guild.polls.add(poll);
    	guild.save();
    	// redirect
    	admin(guild.tag);
    }

    public static void adminUpdatePoll(Long pollId, String question, Date startAt, Date endAt, boolean enable) {
    	Poll poll = Poll.findById(pollId);
    	Guild guild = poll.guild;
    	Controller.notFoundIfNull(guild);
    	Controller.notFoundIfNull(poll);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// do it
    	poll.question = question;
    	poll.startAt = startAt;
    	poll.endAt = endAt;
    	poll.enable = enable;
    	poll.save();
    	// redirect
    	admin(guild.tag);
    }

    public static void adminDeletePoll(Long pollId) {
    	Poll poll = Poll.findById(pollId);
    	Guild guild = poll.guild;
    	Controller.notFoundIfNull(guild);
    	Controller.notFoundIfNull(poll);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// do it
    	guild.polls.remove(poll);
    	guild.save();
    	poll.delete();
    	// redirect
    	admin(guild.tag);
    }

    public static void adminAddPollOption(Long pollId, String answer) {
    	Poll poll = Poll.findById(pollId);
    	Guild guild = poll.guild;
    	Controller.notFoundIfNull(guild);
    	Controller.notFoundIfNull(poll);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// do it
    	PollOption po = new PollOption(answer);
    	po.poll = poll;
    	po.save();
    	poll.options.add(po);
    	poll.save();
    	// redirect
    	admin(guild.tag);
    }

    public static void adminUpdatePollOption(Long optionId, String answer) {
    	PollOption op = PollOption.findById(optionId);
    	Controller.notFoundIfNull(op);
    	Poll poll = op.poll;
    	Controller.notFoundIfNull(poll);
    	Guild guild = poll.guild;
    	Controller.notFoundIfNull(guild);
    	
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// do it
    	op.answer = answer;
    	op.save();
    	// redirect
    	admin(guild.tag);
    }

    public static void adminDeletePollOption(Long optionId) {
    	PollOption op = PollOption.findById(optionId);
    	Controller.notFoundIfNull(op);
    	Poll poll = op.poll;
    	Controller.notFoundIfNull(poll);
    	Guild guild = poll.guild;
    	Controller.notFoundIfNull(guild);

    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// do it
    	poll.options.remove(op);
    	poll.save();
    	op.delete();
    	// redirect
    	admin(guild.tag);
    }

    public static void adminAddLink(String guildTag, String name, String link, String description, int weight) {
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// add link
    	GuildLink lnk = guild.addLink(name, link, description, weight);
    	// redirect
    	admin(guildTag);
    }

    public static void adminUpdateLink(String guildTag, Long linkId, String name, String link, String description, int weight) {
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// get & update link
    	GuildLink lnk = GuildLink.findById(linkId);
    	lnk.name = name;
    	lnk.link = link;
    	lnk.description = description;
    	lnk.weight = weight;
    	lnk.save();
    	// redirect
    	admin(guildTag);
    }

    public static void adminDeleteLink(String guildTag, Long linkId) {
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	// Check auth
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	// delete link
    	GuildLink lnk = GuildLink.findById(linkId);
    	if(guild.links.contains(lnk))
    		guild.links.remove(lnk);
    	if(lnk != null)
    		lnk.delete();
    	guild.save();
    	// redirect
    	admin(guildTag);
    }

    public static void adminAddEvent(String guildTag, String name, Date startAt, Date endAt, String description) {
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	GuildEvent ge = new GuildEvent(guild, name, startAt, endAt, description).save();
    	guild.events.add(ge);
    	guild.save();
    	admin(guildTag);
    }

    public static void adminUpdateEvent(String guildTag, Long id, String name, Date startAt, Date endAt, String description) {
    	GuildEvent ge = GuildEvent.findById(id);
    	Controller.notFoundIfNull(ge);
    	if(!Guilds.checkAdminAuth(ge.guild))
    		home(ge.guild.tag);
    	ge.name = name;
    	ge.startAt = startAt;
    	ge.endAt = endAt;
    	ge.description = description;
    	ge.save();
    	admin(guildTag);
    }

    public static void adminDeleteEvent(String guildTag, Long eventId) {
    	Guild guild = Guild.findByTag(guildTag);
    	Controller.notFoundIfNull(guild);
    	if(!Guilds.checkAdminAuth(guild))
    		home(guild.tag);
    	GuildEvent ge = GuildEvent.findById(eventId);
    	guild.events.remove(ge);
    	guild.save();
    	ge.delete();
    	admin(guildTag);
    }

    public static void registerEvent(String guildTag, String playerName, Long eventId) {
    	Player player = Player.find(playerName);
    	GuildEvent event = GuildEvent.findById(eventId);
    	Controller.notFoundIfNull(player);
    	Controller.notFoundIfNull(event);

    	// Un-Register event
    	if(event.participants.contains(player)) {
    		event.participants.remove(player);
    		player.events.remove(event);
    	}
    	// Register event
    	else {
        	event.participants.add(player);
        	player.events.add(event);
    	}

    	event.save();
    	player.save();

    	home(guildTag);
    }

    // AJAX

    public static void pollVote(Long guildId, Long pollId, Long optionId) {
    	Guild guild = Guild.findById(guildId);
    	Controller.notFoundIfNull(guild);
    	Player voter = Player.find(Application.getSessionLogin());
    	Controller.notFoundIfNull(voter);
    	if(!guild.isMember(voter))
    		return;

    	Poll poll = Poll.findById(pollId);
    	PollOption option = PollOption.findById(optionId);

    	boolean voted = poll.addVote(voter, optionId);

    	renderTemplate("Guilds/_pollFrame.html", poll, voter);
    }

    public static void addWallMessage(Long guildId, String content) {
        String author = Application.getSessionLogin();
        Controller.notFoundIfNull(author);
        Guild guild = Guild.findById(guildId);
        Controller.notFoundIfNull(guild);
        GuildWallMessage message = guild.addWallMessage(author, content);
        //log
        Player player = Player.find(author);
        player.logAction(PlayerAction.LOG_GUILD_ADD_WALLMESSAGE, "" + guild.id); // LOG
        // render
        renderTemplate("Guilds/_wallMessage.html", message);
    }

    public static void updateWelcomeMessage(Long guildId, String content) {
        Player p = Player.find(Application.getSessionLogin());
        Guild g = Guild.findById(guildId);
        Controller.notFoundIfNull(p);
        Controller.notFoundIfNull(g);

        // for officers only
        if (!g.isMember(p) || !p.membership.isOfficer())
            return;

        g.welcomeMessage = content.trim();
        g.save();
    }

    public static void updateRecruitmentPolicy(Long guildId, String content) {
        Player p = Player.find(Application.getSessionLogin());
        Guild g = Guild.findById(guildId);
        Controller.notFoundIfNull(p);
        Controller.notFoundIfNull(g);

        // for officers only
        if (!g.isMember(p) || !p.membership.isOfficer())
            return;

        g.recruitmentPolicy = content.trim();
        g.save();
    }

    public static void toggleRecruitmentStatus(Long guildId) {
        Player p = Player.find(Application.getSessionLogin());
        Guild guild = Guild.findById(guildId);
        Controller.notFoundIfNull(p);
        Controller.notFoundIfNull(guild);

        // for officers only
        if (!guild.isMember(p) || !p.membership.isOfficer())
            return;

        if (guild.recruitmentOpened)
            guild.recruitmentOpened = false;
        else
            guild.recruitmentOpened = true;
        guild.save();

        // render
        renderTemplate("Guilds/_guildRecruitmentStatus.html", guild);
    }

    public static boolean hasGuild(String playerName) {
        return Player.hasGuild(playerName);
    }

    public static Guild getPlayerGuild(String playerName) {
        Player player = Player.find(playerName);
        Controller.notFoundIfNull(player);
        return player.membership.guild;
    }

    // SECURITY

    public static boolean checkAdminAuth(Guild guild) {
    	Player player = Player.find(Application.getSessionLogin());
    	if(player == null)
    		return false;

    	if(guild.officersCanAdmin) {
        	if(!guild.isMember(player) || !player.membership.isOfficer())
        		return false;
    	} else {
        	if(!guild.isMember(player) || !player.membership.isGuildMaster())
        		return false;
    	}
    	return true;
    }

    public static boolean isAuthAndOfficer(Guild guild, Player player) {
    	if(guild == null || player == null || player.membership == null)
    		return false;

    	// Check if this player belongs to this guild
    	if(guild.isMember(player)) {
    		// Check if player is officer
    		if(player.membership.isOfficer()) {
        		// Check if this player has security enabled
        		if(Application.isSecured(player)) {
        			return true;
        		} else {
        			return false; // player is member, is officer, but is NOT secured
        		}
    		} else {
    			return false; // player is member, but is NOT officer
    		}
    	} else {
    		return false; // player is NOT member
    	}
    }


    private static boolean checkOffAuth(Guild guild) {
        // If player is officer, check if he's auth
        Player currentUser = Player.find(Application.getSessionLogin());
        if(currentUser == null)
        	return false;
        if(guild.isMember(currentUser) && currentUser.membership != null && currentUser.membership.isOfficer() && !Guilds.isAuthAndOfficer(guild, currentUser)) {
        	renderTemplate("Guilds/_noRight.html", guild);
        	return false;
        }
        return true;
	}
}
