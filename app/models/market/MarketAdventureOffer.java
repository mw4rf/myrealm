package models.market;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

import models.players.Player;

import play.db.jpa.Model;

@Entity
public class MarketAdventureOffer extends Model {

	@ManyToOne
	public Player buyer;

	@ManyToOne
	public MarketAdventureSale sale;

	@Type(type = "org.hibernate.type.BooleanType")
	public boolean sold = false;

	public Date madeAt;
	public Date answeredAt;

	public MarketAdventureOffer(Player buyer, MarketAdventureSale sale) {
		super();
		this.buyer = buyer;
		this.sale = sale;
		this.madeAt = new Date();
	}

	public boolean hasAnswer() {
		if(this.answeredAt != null)
			return true;
		else
			return false;
	}



}
