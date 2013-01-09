package models.adventures;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.players.Player;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class AdventureComment extends Model {

    public String author;
    public Date publishedAt;

    @ManyToOne
    public Adventure adventure;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String content;

    public AdventureComment(Adventure adventure, String author, String content) {
        this.adventure = adventure;
        this.author = author;
        this.content = content;
        this.publishedAt = new Date();
    }

    public Player getAuthorAsPlayer() {
    	return Player.find(this.author);
    }

    public static List<AdventureComment> findByAuthor(String author) {
        return AdventureComment.find("byAuthor", author).fetch();
    }

    @Override
    public String toString() {
    	return this.summary(100);
    }

    public String getShort() {
    	return this.summary(100);
    }

    public String summary(int numberOfWordsToKeep) {
        String res = "";
        String full = this.content;
        String[] split = full.split(" ");
        if (split.length < numberOfWordsToKeep)
            numberOfWordsToKeep = split.length;
        for (int i = 0; i < numberOfWordsToKeep; i++) {
            res += " " + split[i];
        }
        return res;
    }
    
    public static List<AdventureComment> getLastForPlayer(String playerName, int count) {
        return AdventureComment.find("author = ? order by publishedAt desc", playerName).fetch(count);
    }
    
    public static int countComments(String playerName) {
    	return AdventureComment.findByAuthor(playerName).size();
    }

    public static final Comparator<AdventureComment> BY_DATE_DESC = new Comparator<AdventureComment>() {
        @Override
        public int compare(AdventureComment a1, AdventureComment a2) {
            return a2.publishedAt.compareTo(a1.publishedAt);
        }
    };

    public static final Comparator<AdventureComment> BY_DATE_ASC = new Comparator<AdventureComment>() {
        @Override
        public int compare(AdventureComment a1, AdventureComment a2) {
            return a1.publishedAt.compareTo(a2.publishedAt);
        }
    };

}
