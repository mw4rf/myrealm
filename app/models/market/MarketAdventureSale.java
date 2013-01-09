package models.market;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

import models.players.Player;

import play.db.jpa.Model;
import world.World;
import world.WorldAdventure;

@Entity
public class MarketAdventureSale extends Model {

	@ManyToOne
	public Player seller;

	@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
	public List<MarketAdventureOffer> offers = new ArrayList<MarketAdventureOffer>();
	
	@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
	public List<MarketAdventureComment> comments = new ArrayList<MarketAdventureComment>();

	public Date startAt;
	public Date endAt;

	public String itemName;
	public int itemQuantity;
	public int itemOriginalQuantity;
	public int type = 1;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
	public String description;

	// not mapped
	public static final int TYPE_ADVENTURE = 1;
	public static final int TYPE_RESSOURCE = 2;

	public MarketAdventureSale(Player seller, Date startAt,
			Date endAt, String itemName, int itemQuantity, String description, int type) {
		super();
		this.seller = seller;
		this.startAt = startAt;
		this.endAt = endAt;
		this.itemName = itemName;
		this.itemQuantity = itemQuantity;
		this.itemOriginalQuantity = itemQuantity;
		this.description = description;
		this.type = type;
	}
	
	public MarketAdventureComment addComment(String author, String comment) {
		MarketAdventureComment mac = new MarketAdventureComment(author, comment, this).save();
		this.comments.add(mac);
		this.save();
		return mac;
	}

	public boolean isAdventure() {
		Iterator<WorldAdventure> it = World.ADVENTURES.values().iterator();
		while(it.hasNext())
			if(it.next().getName().equals(this.itemName))
				return true;
		return false;
	}

	public WorldAdventure getWorldAdventure() {
		return World.ADVENTURES.get(this.itemName);
	}

	public boolean isSeller(String playerName) {
		if(this.seller.name.equals(playerName))
			return true;
		else
			return false;
	}

	public boolean isBuyer(String playerName) {
		Iterator<MarketAdventureOffer> it = this.offers.iterator();
		while(it.hasNext())
			if(it.next().buyer.name.equals(playerName))
				return true;
		return false;
	}

	public MarketAdventureOffer getOffer(String playerName) {
		Iterator<MarketAdventureOffer> it = this.offers.iterator();
		while(it.hasNext()) {
			MarketAdventureOffer offer = it.next();
			if(offer.buyer.name.equals(playerName))
				return offer;
		}
		return null;
	}

	@Override
	public String toString() {
		return this.itemName + "(" + this.seller.name + ")";
	}

}
