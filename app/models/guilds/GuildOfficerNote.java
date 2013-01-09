package models.guilds;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.players.Player;

import org.hibernate.annotations.Type;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class GuildOfficerNote extends Model {

    @Required
    @ManyToOne
    public Membership member; // one player has many notes

    public String officer;

    public Long guildId;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String content;

    public Date publishedAt;

	public GuildOfficerNote(Membership member, String officer, String content) {
		super();
		this.member = member;
		this.officer = officer;
		this.content = content;
		this.publishedAt = new Date();
		this.guildId = member.guild.id;
	}

	public static List<GuildOfficerNote> getOfficerNotesForGuild(long guildId) {
		return GuildOfficerNote.find("byGuildId", guildId).fetch();
	}

	public Player getOfficer() {
		Player p = Player.find(officer);
		return p;
	}

	public Player getPlayer() {
		return member.player;
	}

    public String getSummary() {
    	return this.toString();
    }

    @Override
    public String toString() {
    	if(content != null && content.length() > 100)
    		return content.substring(0, 100) + "...";
    	else
    		return content;
    }

    public static final Comparator<GuildOfficerNote> BY_DATE_DESC = new Comparator<GuildOfficerNote>() {
        @Override
        public int compare(GuildOfficerNote a1, GuildOfficerNote a2) {
            return a2.publishedAt.compareTo(a1.publishedAt);
        }
    };

    public static final Comparator<GuildOfficerNote> BY_DATE_ASC = new Comparator<GuildOfficerNote>() {
        @Override
        public int compare(GuildOfficerNote a1, GuildOfficerNote a2) {
            return a1.publishedAt.compareTo(a2.publishedAt);
        }
    };


}
