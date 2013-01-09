package notifiers;

import models.players.PlayerTimer;
import play.i18n.Messages;
import play.mvc.Mailer;

public class TimerMailers extends Mailer {
	
	/**
	 * Mails a {@link PlayerTimer} owner to tell her one of her timers will soon expire.
	 * @param timer
	 */
	public static void expiringTimers(PlayerTimer timer) {
		setSubject(Messages.get("mail.subject.timer.expiring"));
		addRecipient(timer.player.preferences.EMAIL);
		setFrom(play.Play.configuration.get("myrealm.admin.mail.from"));
		send(timer);
	}
	
	/**
	 * Mails a {@link PlayerTimer} owner to tell her one of her timers has expired.
	 * @param timer
	 */
	public static void expiredTimers(PlayerTimer timer) {
		setSubject(Messages.get("mail.subject.timer.expiring"));
		addRecipient(timer.player.preferences.EMAIL);
		setFrom(play.Play.configuration.get("myrealm.admin.mail.from"));
		send(timer);
	}

}
