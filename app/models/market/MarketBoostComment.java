package models.market;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.players.Player;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class MarketBoostComment extends Model {

	@ManyToOne
	public MarketBoostExchange exchange;

	public String author;

	public Date publishedAt;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
	public String content;

    public MarketBoostComment(MarketBoostExchange exchange, String author, String comment) {
    	this.exchange = exchange;
    	this.author = author;
    	this.content = comment;
    	this.publishedAt = new Date();
    }

    public boolean isAuthorSeller() {
    	return this.exchange.giver.name.equals(this.author);
    }

    public boolean isAuthorBuyer() {
    	return this.exchange.receiver.name.equals(this.author);
    }

    public Player getAuthorAsPlayer() {
    	return Player.find(this.author);
    }

    public static final Comparator<MarketBoostComment> BY_DATE_ASC = new Comparator<MarketBoostComment>() {
        @Override
        public int compare(MarketBoostComment a1, MarketBoostComment a2) {
            return a1.publishedAt.compareTo(a2.publishedAt);
        }
    };

    public static final Comparator<MarketBoostComment> BY_DATE_DESC = new Comparator<MarketBoostComment>() {
        @Override
        public int compare(MarketBoostComment a1, MarketBoostComment a2) {
            return a2.publishedAt.compareTo(a1.publishedAt);
        }
    };
}
