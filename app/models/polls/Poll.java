package models.polls;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.guilds.Guild;
import models.players.Player;

import org.hibernate.annotations.Type;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Poll extends Model {

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    public List<PollOption> options;

    @ManyToOne
    public Guild guild;

    @Required
    public String question;

    public Date startAt;
	public Date endAt;

    @Type(type = "org.hibernate.type.BooleanType")
    public boolean enable = true;

	public Poll(String question, Date startAt, Date endAt) {
		this.question = question;
		if(startAt == null)
			this.startAt = new Date();
		else
			this.startAt = startAt;
		if(endAt == null) {
			this.endAt = new Date();
			this.endAt.setTime(this.startAt.getTime() + 1 * 24 * 60 * 60 * 1000); // 1 day later
		}
		else
			this.endAt = endAt;
	}

	public PollOption addOption(String answer) {
		PollOption po = new PollOption(answer).save();
		this.options.add(po);
		this.save();
		return po;
	}

	public boolean addVote(Player voter, Long optionId) {
		PollOption po = PollOption.findById(optionId);
		if(!this.options.contains(po))
			return false;
		if(voter == null)
			return false;

		PollVote pv = new PollVote(voter, po).save();
		po.votes.add(pv);
		po.save();
		return true;
	}

	public boolean hasVoted(Player player) {
		Iterator<PollVote> it = player.votes.iterator();
		while(it.hasNext()) {
			PollVote pv = it.next();
			if(pv.option.poll.id == this.id)
				return true;
		}
		return false;
	}

	public boolean hasVoted(String playerName) {
		return this.hasVoted(Player.find(playerName));
	}

	public int countVotes() {
		int total = 0;
		Iterator<PollOption> it = this.options.iterator();
		while(it.hasNext()) {
			PollOption po = it.next();
			if(po.votes != null)
				total += po.votes.size();
		}
		return total;
	}

	@Override
	public String toString() {
		return this.question;
	}
}
