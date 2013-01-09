package models.admin;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class IrcMessage extends Model {

	public String sender;
	public String nick;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String message;

    public int type = 1;

    @Type(type = "org.hibernate.type.BooleanType")
    public boolean read = false;
    public Date sentAt;
    public Date readAt;

    /* Static Fields */
    public static final int TYPE_PUBLIC_MESSAGE = 1;
    public static final int TYPE_PRIVATE_MESSAGE = 2;
    public static final int TYPE_NOTICE = 3;

	public IrcMessage(String sender, String nick, String message, int type) {
		super();
		this.sender = sender;
		this.nick = nick;
		this.message = message;
		this.type = type;
		this.sentAt = new Date();
	}

	public static List<IrcMessage> getMessagesFor(String nick) {
		return IrcMessage.find("byNick", nick).fetch();
	}

	public void setRead() {
		this.read = true;
		this.readAt = new Date();
		this.save();
	}

	@Override
	public String toString() {
		return this.sender + " => " + this.nick;
	}


}
