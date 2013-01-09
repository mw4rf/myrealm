package models.guilds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;

import models.players.Player;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Membership extends Model {

    @OneToOne(fetch = FetchType.LAZY)
    public Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    public Guild guild;

    public Date joinedAt;

    @Required
    public int rank = 1;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    public List<GuildOfficerNote> officernotes = new ArrayList<GuildOfficerNote>();

    @Type(type = "org.hibernate.type.BooleanType")
    public boolean confirmed = false;

    // Ranks (no column for those)
    public static final int RANK_1_MEMBER = 1;
    public static final int RANK_2_SENIOR_MEMBER = 2;
    public static final int RANK_3_OFFICER = 3;
    public static final int RANK_4_GUILDMASTER = 4;

    public Membership(Guild guild, Player player) {
        this.guild = guild;
        this.player = player;
        this.joinedAt = new Date();
    }

    public static Membership findByPlayer(String playerName) {
        return Membership.find("byPlayer", Player.find(playerName)).first();
    }

    public boolean promote() {
    	if(this.rank >= Membership.RANK_3_OFFICER)
    		return false;

    	this.rank++;
    	this.save();
    	return true;
    }

    public boolean demote() {
    	if(this.rank == Membership.RANK_4_GUILDMASTER || this.rank == Membership.RANK_1_MEMBER)
    		return false;

    	this.rank--;
    	this.save();
    	return true;
    }

	public void confirm() {
		this.confirmed = true;
		this.joinedAt = new Date();
		this.save();
	}

    public boolean hasMemberRank() {
        if (this.rank == Membership.RANK_1_MEMBER)
            return true;
        else
            return false;
    }

    public boolean isSeniorMember() {
        if (this.rank == Membership.RANK_2_SENIOR_MEMBER)
            return true;
        else
            return false;
    }

    public boolean isOfficer() {
        if (this.rank >= Membership.RANK_3_OFFICER)
            return true;
        else
            return false;
    }

    public boolean isGuildMaster() {
        if (this.rank == Membership.RANK_4_GUILDMASTER)
            return true;
        else
            return false;
    }

    public void addOfficerNote(String officer, String content) {
    	GuildOfficerNote note = new GuildOfficerNote(this, officer, content).save();
    	System.out.println("note content is " + note.content + " -- content is " + content);
    	this.officernotes.add(note);
    	this.save();
    }

    @Override
    public String toString() {
        return "[" + this.guild.tag + "] " + this.player.name;
    }

	public static final Comparator<Membership> BY_NAME_ASC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return b1.player.name.compareTo(b2.player.name);
        }
	};

	public static final Comparator<Membership> BY_NAME_DESC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return b2.player.name.compareTo(b1.player.name);
        }
	};

	public static final Comparator<Membership> BY_RANK_ASC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return new Integer(b1.rank).compareTo(new Integer(b2.rank));
        }
	};

	public static final Comparator<Membership> BY_RANK_DESC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return new Integer(b2.rank).compareTo(new Integer(b1.rank));
        }
	};

	public static final Comparator<Membership> BY_JOIN_DATE_ASC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return b1.joinedAt.compareTo(b2.joinedAt);
        }
	};

	public static final Comparator<Membership> BY_JOIN_DATE_DESC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return b2.joinedAt.compareTo(b1.joinedAt);
        }
	};

	public static final Comparator<Membership> BY_LAST_CONNECTION_ASC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return b1.player.lastConnection.compareTo(b2.player.lastConnection);
        }
	};

	public static final Comparator<Membership> BY_JOIN_CONNECTION_DESC = new Comparator<Membership>() {
		@Override
        public int compare(Membership b1, Membership b2) {
			return b2.player.lastConnection.compareTo(b1.player.lastConnection);
        }
	};
}
