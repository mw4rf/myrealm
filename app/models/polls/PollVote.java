package models.polls;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import models.players.Player;

import play.db.jpa.Model;

@Entity
public class PollVote extends Model {

	@ManyToOne
	public Player player;

	@ManyToOne
	public PollOption option;

	public PollVote(Player player, PollOption option) {
		this.player = player;
		this.option = option;
	}

	@Override
	public String toString() {
		return "[" + this.player.name + "] " + this.option.poll.question + " => " + this.option.answer;
	}
}
