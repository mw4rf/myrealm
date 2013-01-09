package models.guilds;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.players.Player;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class GuildWallMessage extends Model {

    public String author;
    public Date publishedAt;

    @ManyToOne
    public Guild guild;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String content;

    public GuildWallMessage(String author, Date publishedAt, Guild guild, String content) {
        super();
        this.author = author;
        this.publishedAt = publishedAt;
        this.guild = guild;
        this.content = content;
    }

    public Player getAuthorAsPlayer() {
        return Player.find(this.author);
    }

    public int getAuthorRank() {
        Player p = Player.find(this.author);
        if (p == null)
            return 0; // not member of the guild
        if (!this.guild.isMember(p))
            return 0;
        if (p.membership.isGuildMaster())
            return 4;
        if (p.membership.isOfficer())
            return 3;
        if (p.membership.isSeniorMember())
            return 2;
        if (p.membership.hasMemberRank())
            return 1;
        return 0;
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

    public static final Comparator<GuildWallMessage> BY_DATE_ASC = new Comparator<GuildWallMessage>() {
        @Override
        public int compare(GuildWallMessage a1, GuildWallMessage a2) {
            return a1.publishedAt.compareTo(a2.publishedAt);
        }
    };

    public static final Comparator<GuildWallMessage> BY_DATE_DESC = new Comparator<GuildWallMessage>() {
        @Override
        public int compare(GuildWallMessage a1, GuildWallMessage a2) {
            return a2.publishedAt.compareTo(a1.publishedAt);
        }
    };

}
