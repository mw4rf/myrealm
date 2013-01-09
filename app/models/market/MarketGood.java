package models.market;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class MarketGood extends Model {

	public String goodName;
	public double goodQuantity;

	@ManyToOne
	public MarketAdventureOffer offer;

}
