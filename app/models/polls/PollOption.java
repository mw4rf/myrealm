package models.polls;

import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.players.Player;

import play.db.jpa.Model;

@Entity
public class PollOption extends Model {

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL)
    public List<PollVote> votes;

	@ManyToOne
	public Poll poll;

	public String answer;

	public PollOption(String answer) {
		this.answer = answer;
	}

	public double getPercentage() {
		int total = this.poll.countVotes();
		double res = this.votes.size() * 100 / total;
        int ix = (int) (res * 100.0); // scale it
        res = ix / 100.0; // 2 digits after .
        return res;
	}

	public boolean hasVotedFor(Player p) {
		if(p == null || this.votes == null)
			return false;
		Iterator<PollVote> it = this.votes.iterator();
		while(it.hasNext()) {
			PollVote pv = it.next();
			if(pv.player.id == p.id)
				return true;
		}
		return false;
	}

	public boolean hasVotedFor(String playerName) {
		return this.hasVotedFor(Player.find(playerName));
	}

	@Override
	public String toString() {
		return this.answer;
	}

}
