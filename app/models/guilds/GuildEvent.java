package models.guilds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.players.Player;

import play.db.jpa.Model;

@Entity
public class GuildEvent extends Model {

	@ManyToOne
	public Guild guild;

    @ManyToMany
    public List<Player> participants = new ArrayList<Player>();

	public String name;
	public Date startAt;
	public Date endAt;
	public String description;



	public GuildEvent(Guild guild, String name, Date startAt, Date endAt,
			String description) {
		this.guild = guild;
		this.name = name;
		this.startAt = startAt;
		this.endAt = endAt;
		this.description = description;
	}

	public static GuildEvent findLastByGuild(Guild g) {
		return GuildEvent.find("byGuild", g).first();
	}

	public boolean isParticipant(String playerName) {
		Player player = Player.find(playerName);
		if(player == null)
			return false;
		if(this.participants.contains(player))
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return this.name;
	}

    public static final Comparator<GuildEvent> BY_DATE_START_ASC = new Comparator<GuildEvent>() {
        @Override
        public int compare(GuildEvent a1, GuildEvent a2) {
            return a1.startAt.compareTo(a2.startAt);
        }
    };

    public static final Comparator<GuildEvent> BY_DATE_START_DESC = new Comparator<GuildEvent>() {
        @Override
        public int compare(GuildEvent a1, GuildEvent a2) {
            return a2.startAt.compareTo(a1.startAt);
        }
    };

}
