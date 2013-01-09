package models.guilds;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class GuildLink extends Model {

	@ManyToOne
	public Guild guild;

	public String name;
	public String link;

	public String description;
	public Date createdAt;

	public Integer weight = 1;

	public GuildLink(Guild guild, String name, String link, String description, int weight) {
		this.guild = guild;
		this.name = name;
		this.link = link;
		this.description = description;
		this.weight = new Integer(weight);
		this.createdAt = new Date();
	}

	@Override
	public String toString() {
		return this.link;
	}

	public static final Comparator<GuildLink> BY_WEIGHT_ASC = new Comparator<GuildLink>() {
		@Override
        public int compare(GuildLink b1, GuildLink b2) {
			return b1.weight.compareTo(b2.weight);
        }
	};

	public static final Comparator<GuildLink> BY_WEIGHT_DSC = new Comparator<GuildLink>() {
		@Override
        public int compare(GuildLink b1, GuildLink b2) {
			return b2.weight.compareTo(b1.weight);
        }
	};

}
