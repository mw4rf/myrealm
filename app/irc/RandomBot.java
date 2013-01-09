package irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import models.admin.IrcBot;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class RandomBot extends PircBot {
	
	public IrcBot bot = null;
	
	public String channel;
	
	public static final int DELAY_BEFORE_JOIN = 3000; // in milliseconds
	public static final String LOCALE = "fr";
	public static final String DATE_FORMAT = "dd/MM/yyyy à HH:mm";

	public static final String COMMAND_RAND = "!rand";
	public static final String COMMAND_HELP = "!help";
	
	/**
	 * Skip bots and founders : nick prefixed with ~ or &
	 */
	public static final boolean skip_self = true;
	public static final boolean skip_founder = true;
	public static final boolean skip_ops = true;
	public static final boolean skip_bots = true;
	public static final boolean skip_unvoiced = true;
	
	public RandomBot(IrcBot bot) {
		this.bot = bot;
		this.channel = bot.channel;
		// Start bot
		this.setName(bot.nick);
		this.setAutoNickChange(true);
		try {
			this.connect(bot.hostname);
			// this.identify(bot.password); // does NOT work
			this.sendMessage("NickServ", " IDENTIFY " + bot.password);
			// Join after a delay, in order to let the bot ident with nickserv
	        Timer timer = new Timer();
	        timer.schedule( new TimerTask() {
				@Override
				public void run() {
					joinChannel("#" + channel);
				}
			}, RandomBot.DELAY_BEFORE_JOIN);
		} catch(Exception ex) {;}
	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		boolean debug = false;
		boolean verbose = false;
		if(message.toLowerCase().contains("debug"))
			debug = true;
		if(message.toLowerCase().contains("verbose") || message.toLowerCase().contains(" v"))
			verbose = true;
		
		if(debug) sendMessage(channel, "DEBUG -- sendMessage()");
		if(Tools.startsWithIgnoreCase(message, RandomBot.COMMAND_RAND)) {
			User[] users = this.getUsers(channel);
			HashMap<String, User> usersmap = new HashMap();
			ArrayList<User> userslist = new ArrayList();
			for(User u : users) {
				if(debug) sendMessage(channel, "Je vois " + u.getNick());
				usersmap.put(u.getNick(), u);
				// Skip self
				if(RandomBot.skip_self && u.getNick().equals(this.getNick())) {
					if(debug)
						sendMessage(channel, "[Exclusion] " + u.getNick() + " excluded. Reason: I can't pick myself :)");
					continue;
				}
				if(RandomBot.skip_ops && u.isOp()) {
					if(debug)
						sendMessage(channel, "[Exclusion] " + u.getNick() + " excluded. Reason: channel OP");
					continue;
				}
				if(RandomBot.skip_founder && u.getNick().startsWith("~")) {
					if(debug)
						sendMessage(channel, "[Exclusion] " + u.getNick() + " excluded. Reason: channel OWNER");
					continue;
				}
				if(RandomBot.skip_bots && u.getNick().startsWith("&")) {
					if(debug)
						sendMessage(channel, "[Exclusion] " + u.getNick() + " excluded. Reason: channel BOT");
					continue;
				}
				if(RandomBot.skip_unvoiced && !u.hasVoice()) {
					if(debug)
						sendMessage(channel, "[Exclusion] " + u.getNick() + " excluded. Reason: unvoiced");
					continue;
				}
				// Add user :)
				userslist.add(u);
			}
			if(debug) sendMessage(channel, "DEBUG -- " + userslist.size() + " non-op users here");
			// User is OP
			if(userslist.size() > 0) {
				if(debug) sendMessage(channel, "DEBUG -- " + userslist.size() + " userslist.size() > 0 condition passed");
				if(usersmap.get(sender).isOp()) {
					if(debug) sendMessage(channel, "DEBUG -- " + userslist.size() + " usermap(sender).isOp() condition passed");
					try {
						if(debug || verbose) sendMessage(channel, "Qui vais-je choisir ... ?");
						if(verbose)
							for(User u : userslist)
								sendMessage(channel, "Je vois " + u.getNick());
						if(debug) {
							sendMessage(channel, "ALL USERS: " + usersmap.keySet().toString());
							sendMessage(channel, "ELIGIBLE USERS: " + userslist.toArray().toString());
						}
						// pick user !
						Random rand = new Random();
						int n = rand.nextInt(userslist.size());
						if(debug) sendMessage(channel, "DEBUG -- rand generated " + n);
						if(debug || verbose)
							sendMessage(channel, "Je choisis " + userslist.get(n).getNick() + " !");
						else
							sendMessage(channel, userslist.get(n).getNick());
					} catch(Exception e) {
						sendMessage(channel, "Oops... something went wrong :(");
					}
					if(debug)
						sendMessage(channel, "Job Done!");
					return;
				} else {
					if(debug) sendMessage(channel, "DEBUG -- command sender is not OP -- cancelling!");
					if(verbose) sendMessage(sender, "Seuls les OP peuvent m'utiliser. Désolé.");
					this.deVoice(channel, sender);
					return;
				}
			} else {
				if(debug) sendMessage(channel, "DEBUG -- user list empty... nobody to be chosen !");
				return;
			}
		}
		else if(Tools.startsWithIgnoreCase(message, RandomBot.COMMAND_HELP)) {
			sendMessage(sender, "Bonjour. Je suis un bot et mon but est de choisir aléatoirement un utilisateur, parmi les membres d'un salon.");
			sendMessage(sender, "Voici les commandes que je comprends :");
			sendMessage(sender, "     !rand           [Tire au sort un utilisateur]");
			sendMessage(sender, "     !rand v         [Tire au sort un utilisateur et affiche des informations supplémentaires]");
			sendMessage(sender, "     !rand           [Tire au sort un utilisateur et affiche encore plus d'informations]");
			sendMessage(sender, "Sont exclus du tirage au sort : moi-même (" + this.getNick() + "), les OP (@), les fondateurs du salon (~), les autres bots (&), et les utilisateurs unvoiced (-v)");
			sendMessage(sender, "Vous devez être OP sur le salon pour m'utiliser.");
			sendMessage(sender, "Si vous voulez exclure un utilisateur du tirage au sort, il suffit de le devoice : /devoice toto");
		}
		else if(debug) 
			sendMessage(channel, "DEBUG -- unknown command");
	}

	@Override
	protected void onJoin(String channel, String sender, String login,
			String hostname) {
		// We join a channel
		if(sender.equals(this.getNick())) {
			// Not the good channel : leave
			if(!channel.equalsIgnoreCase("#" + this.bot.channel))
				this.partChannel(channel);
		}
		// Someone else joins
		else {
			this.voice(channel, sender);
		}

	}
	
	

}
