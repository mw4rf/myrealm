package irc;

import java.util.Iterator;
import java.util.List;

import models.admin.IrcBot;
import models.admin.IrcMessage;

import org.jibble.pircbot.PircBot;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import play.db.jpa.JPAPlugin;
import play.i18n.Messages;

public class MessageBot extends PircBot {

	public IrcBot bot;

	public static String COMMAND_MESSAGES = "MESSAGES";
	public static String COMMAND_SEND = "SEND";

	public static final String LOCALE = "fr";
	public static final String DATE_FORMAT = "dd/MM/yyyy à HH:mm";

	public MessageBot(IrcBot bot) {
		this.bot = bot;
		// Start bot
		this.setName(bot.nick);
		this.setAutoNickChange(true);
		try {
			this.connect(bot.hostname);
			this.joinChannel("#" + bot.channel);
		} catch (Exception ex) {;}
	}

	/* (non-Javadoc)
	 * @see org.jibble.pircbot.PircBot#onPrivateMessage(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		// Read messages
		if(message.equalsIgnoreCase(MessageBot.COMMAND_MESSAGES)) {
			this.sendUnreadMessages(sender);
		}
		// Add new message
		else if(Tools.startsWithIgnoreCase(message, MessageBot.COMMAND_SEND)) {
			String[] split = message.split(" ");
			String to = split[1];
			String msg = split[2];
			this.sendMessageTo(sender, to, msg);
		}
		// help
		else {
			sendMessage(sender, Messages.getMessage(MessageBot.LOCALE, "irc.bad.syntax", sender));
		}
	}

	/**
	 * When someone (login) joins the channel, check if this person has unread messages, and send them eventually.
	 */
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		this.sendUnreadMessages(sender);
	}

	private void sendUnreadMessages(String login) {
		JPAPlugin.startTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
		List<IrcMessage> list = IrcMessage.getMessagesFor(login);
		Iterator<IrcMessage> it = list.iterator();
		while(it.hasNext()) {
			IrcMessage msg = it.next();
			if(!msg.read && msg.nick.equals(login)) {
				DateTimeFormatter formatter = DateTimeFormat.forPattern(TimerBot.DATE_FORMAT);
				DateTime msgSentAt = new DateTime(msg.sentAt);
				sendMessage(login, "[" + Messages.getMessage(MessageBot.LOCALE, "irc.message.from", msg.sender, formatter.print(msgSentAt)) + "] " + msg.message);
				msg.setRead();
			}
		}
		JPAPlugin.closeTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
	}

	private void sendMessageTo(String sender, String to, String message) {
		JPAPlugin.startTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
		IrcMessage msg = new IrcMessage(sender, to, message, IrcMessage.TYPE_PRIVATE_MESSAGE).save();
		sendMessage(sender, Messages.getMessage(MessageBot.LOCALE, "irc.message.sent", sender));
		JPAPlugin.closeTx(false); // /!\ NEEDED TO USE THE DATABASE /!\
	}







}
