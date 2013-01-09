package models.players;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class PlayerNote extends Model {

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String content;

    public Date updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    public Player player;

    public PlayerNote(Player player, String content) {
        this.player = player;
        this.content = content;
        this.updatedAt = new Date();
    }

    @Override
    public String toString() {
    	return "[" + this.player.name + "] " + " updatedAt: " + this.updatedAt;
    }

}
