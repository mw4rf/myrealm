package models.market;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.joda.time.DateTime;

import models.players.Player;

import play.db.jpa.Model;

@Entity
public class MarketBoostExchange extends Model {

	@ManyToOne
	public Player receiver;

	@ManyToOne
	public Player giver;

	public Date boostAt;

	public boolean done = false;
	public boolean accepted = false;
	public boolean hasCounterpart = false;

	public String boostName;
	public String buildingName;
	public Integer quantity;

	@OneToMany(mappedBy = "exchange", cascade = CascadeType.ALL)
	public List<MarketBoostComment> comments = new ArrayList<MarketBoostComment>();

	public MarketBoostExchange(Player receiver, String boostName, int quantity, String buildingName, Date boostAt) {
		this.receiver = receiver;
		this.boostAt = boostAt;
		this.buildingName = buildingName;
		this.boostName = boostName;
		this.quantity = quantity;
	}

	public MarketBoostComment postComment(String author, String comment) {
		MarketBoostComment c = new MarketBoostComment(this, author, comment).save();
		this.comments.add(c);
		this.save();
		return c;
	}

	public static List<MarketBoostExchange> getCurrentRequests() {
		return MarketBoostExchange.find("boostAt > ? and done = ? and accepted = ? order by boostAt asc", new Date(), false, false).fetch();
	}

	public boolean hasExpired() {
		DateTime now = new DateTime();
		DateTime at = new DateTime(this.boostAt);
		if(at.isBefore(now))
			return true;
		else
			return false;
	}


}
