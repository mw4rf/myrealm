package models.adventures;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.players.Player;

import org.hibernate.annotations.Type;

import play.data.validation.Required;
import play.db.jpa.Model;
import world.World;

@Entity
public class Adventure extends Model {
	
    @Required
	public String name;

    @Required
	public Date dateStart;

    @Required
	public Date dateEnd;
	
	@Lob
	@Type(type="org.hibernate.type.TextType") 
	public String notes;
	
    @Required
    @ManyToOne
    public Player player; // one player has many adventures

    @ManyToMany
    public List<Player> participants = new ArrayList<Player>();

    @OneToMany(mappedBy = "adventure", cascade = CascadeType.ALL)
    public List<AdventureComment> comments;

	public Adventure(Player player, String name) {
		super();
		this.player = player;
		this.name = name;
	}

    @Override
    public String toString() {
        return this.name;
    }

    public static List<Adventure> findStartInTheFuture() {
        Date today = Calendar.getInstance().getTime();
        return Adventure.find("dateStart > ?", today).fetch();
    }

    public Adventure addComment(String author, String content) {
        AdventureComment ac = new AdventureComment(this, author, content).save();
        this.comments.add(ac);
        this.save();
        return this;
    }

    public boolean isParticipant(Player p) {
        if (this.participants.contains(p))
            return true;
        else
            return false;
    }

    public boolean isParticipant(String playerName) {
        return this.isParticipant(Player.find(playerName));
    }

	public String getImage() {
		try {
			return World.ADVENTURES.get(this.name).getImage();
		} catch (NullPointerException e) {
			return "";
		}
	}
	
    public int getMaxPlayers() {
        return World.ADVENTURES.get(this.name).getPlayers();
    }

    /*
     * public String getNotes() { Charset UTF8 = Charset.forName("UTF-8"); return new String(this.notes.getBytes(),
     * UTF8); }
     */
	
    public static final Comparator<Adventure> BY_ID = new Comparator<Adventure>() {
        @Override
        public int compare(Adventure a1, Adventure a2) {
            return a1.id.compareTo(a2.id);
        }
    };

    public static final Comparator<Adventure> BY_ID_DESC = new Comparator<Adventure>() {
        @Override
        public int compare(Adventure a1, Adventure a2) {
            return a2.id.compareTo(a1.id);
        }
    };

	public static final Comparator<Adventure> BY_NAME = new Comparator<Adventure>() {
		@Override
        public int compare(Adventure a1, Adventure a2) {
			return a1.name.compareTo(a2.name);
		}
	};
	
	public static final Comparator<Adventure> BY_NAME_DESC = new Comparator<Adventure>() {
		@Override
        public int compare(Adventure a1, Adventure a2) {
			return a2.name.compareTo(a1.name);
		}
	};
	
	public static final Comparator<Adventure> BY_DATE_START = new Comparator<Adventure>() {
		@Override
        public int compare(Adventure a1, Adventure a2) {
			return a1.dateStart.compareTo(a2.dateStart);
		}
	};
	
	public static final Comparator<Adventure> BY_DATE_START_DESC = new Comparator<Adventure>() {
		@Override
        public int compare(Adventure a1, Adventure a2) {
			return a2.dateStart.compareTo(a1.dateStart);
		}
	};
	
	public static final Comparator<Adventure> BY_DATE_END = new Comparator<Adventure>() {
		@Override
        public int compare(Adventure a1, Adventure a2) {
			return a1.dateEnd.compareTo(a2.dateEnd);
		}
	};
	
	public static final Comparator<Adventure> BY_DATE_END_DESC = new Comparator<Adventure>() {
		@Override
        public int compare(Adventure a1, Adventure a2) {
			return a2.dateEnd.compareTo(a1.dateEnd);
		}
	};

}
