package notifiers;

import models.players.Player;
import play.i18n.Messages;
import play.mvc.Mailer;

public class PlayerMailers extends Mailer {
	
	public static void sendNewPassword(Player player, String password) {
		setSubject(Messages.get("mail.subject.password.new"));
		addRecipient(player.preferences.EMAIL);
		setFrom(play.Play.configuration.get("myrealm.admin.mail.from"));
		send(player, password); // will send views/Mails/test.html and test.txt
	}

}
