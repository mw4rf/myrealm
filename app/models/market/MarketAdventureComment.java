package models.market;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.guilds.GuildWallMessage;
import models.players.Player;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class MarketAdventureComment extends Model {

	public String author;

	@ManyToOne
	public MarketAdventureSale sale;

	public Date publishedAt;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
	public String content;

    public MarketAdventureComment(String author, String comment, MarketAdventureSale sale) {
    	this.author = author;
    	this.content = comment;
    	this.sale = sale;
    	this.publishedAt = new Date();
    }

    public boolean isAuthorSeller() {
    	return this.sale.isSeller(this.author);
    }

    public boolean isAuthorBuyer() {
    	return this.sale.isBuyer(this.author);
    }

    public Player getAuthorAsPlayer() {
    	return Player.find(this.author);
    }

    public static final Comparator<MarketAdventureComment> BY_DATE_ASC = new Comparator<MarketAdventureComment>() {
        @Override
        public int compare(MarketAdventureComment a1, MarketAdventureComment a2) {
            return a1.publishedAt.compareTo(a2.publishedAt);
        }
    };

    public static final Comparator<MarketAdventureComment> BY_DATE_DESC = new Comparator<MarketAdventureComment>() {
        @Override
        public int compare(MarketAdventureComment a1, MarketAdventureComment a2) {
            return a2.publishedAt.compareTo(a1.publishedAt);
        }
    };

}
