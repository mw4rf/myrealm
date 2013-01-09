package models.guilds;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class GuildOldMember extends Model {

	@ManyToOne
	public Guild guild;

	String playerName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
	String leftReason;

	@Type(type = "org.hibernate.type.BooleanType")
	boolean kicked;

	Date joinedAt;
	Date leftAt;

	public GuildOldMember(Membership m, String leftReason, boolean kicked) {
		this.playerName = m.player.name;
		this.leftReason = leftReason;
		this.kicked = kicked;
		this.joinedAt = m.joinedAt;
		this.leftAt = new Date();
		this.guild = m.guild;
	}

	public static final Comparator<GuildOldMember> BY_NAME_ASC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return b1.playerName.compareTo(b2.playerName);
        }
	};

	public static final Comparator<GuildOldMember> BY_NAME_DESC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return b2.playerName.compareTo(b1.playerName);
        }
	};

	public static final Comparator<GuildOldMember> BY_KICKED_ASC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return new Boolean(b1.kicked).compareTo(b2.kicked);
        }
	};

	public static final Comparator<GuildOldMember> BY_KICKED_DESC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return new Boolean(b2.kicked).compareTo(b1.kicked);
        }
	};

	public static final Comparator<GuildOldMember> BY_JOIN_DATE_ASC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return b1.joinedAt.compareTo(b2.joinedAt);
        }
	};

	public static final Comparator<GuildOldMember> BY_JOIN_DATE_DESC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return b2.joinedAt.compareTo(b1.joinedAt);
        }
	};

	public static final Comparator<GuildOldMember> BY_LEFT_DATE_ASC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return b1.leftAt.compareTo(b2.leftAt);
        }
	};

	public static final Comparator<GuildOldMember> BY_LEFT_DATE_DESC = new Comparator<GuildOldMember>() {
		@Override
        public int compare(GuildOldMember b1, GuildOldMember b2) {
			return b2.leftAt.compareTo(b1.leftAt);
        }
	};

}
