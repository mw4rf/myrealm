package models.players;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.realm.Building;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;

import play.data.validation.InFuture;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class PlayerTimer extends Model {

	@ManyToOne
	public Player player;

	@Required
	public Date startAt;

	@Required
	@InFuture
	public Date endAt;

	@Required
	public String name = "";
	public String description = "";

	public boolean remind = false;

	@Required
	public int type = 0;

	@OneToOne
	public Building building = null;

	// Not in DB
	public static final int TIMER_ON_DURATION = 1;
	public static final int TIMER_ON_DATE = 2;
	public static final int TIMER_ON_BUILDING = 3;

	/**
	 * Constructor for timers on duration
	 * @param player
	 * @param name
	 * @param startAt
	 * @param duration
	 */
	public PlayerTimer(Player player, String name, Date startAt, int duration) {
		this.player = player;
		this.startAt = startAt;
		DateTime start = new DateTime(startAt);
		this.endAt = start.plusSeconds(
				duration + new DateTime().getSecondOfMinute()).toDate();
		this.name = name;
	}

	/**
	 * Constructor for timers on date
	 * @param player
	 * @param name
	 * @param startAt
	 * @param endAt
	 */
	public PlayerTimer(Player player, String name, Date startAt, Date endAt) {
		this.player = player;
		this.startAt = startAt;
		this.endAt = endAt;
		this.name = name;
	}

	/**
	 * Constructor for timers on buildings
	 * @param player
	 * @param building
	 * @param remainingQuantity
	 */
	public PlayerTimer(Player player, Building building, int remainingQuantity) {
		this.player = player;
		this.building = building;

		// How many cycles before expiration ??
		int needByCycle = building.level;
		int cycles = remainingQuantity / needByCycle;
		// How many seconds by cycle ?
		int secondsByCycle = building.productionTime;
		// Finally, how many seconds before expiration ?
		int seconds = secondsByCycle * cycles;
		// When will that be ?
		DateTime expiration = new DateTime().plusSeconds(seconds);
		this.startAt = new Date(); // now
		this.endAt = expiration.toDate();

		// Timer name
		this.name = building.name;
	}
	
	/**
	 * Returns a list of all the timers expiring in the NEXT X seconds.
	 * @param seconds
	 * @return {@link List} of {@link PlayerTimer}
	 */
	public static List<PlayerTimer> getExpiringTimers(int seconds) {
		DateTime endtime = new DateTime().plusSeconds(seconds);
		List<PlayerTimer> list = PlayerTimer.find("endAt > ? and endAt < ?", new Date(), endtime.toDate()).fetch();
		return list;
	}
	
	/**
	 * Returns a list of all the timers expired SINCE X seconds.
	 * @param seconds
	 * @return {@link List} of {@link PlayerTimer}
	 */
	public static List<PlayerTimer> getExpiredTimers(int seconds) {
		DateTime endtime = new DateTime().minusSeconds(seconds);
		List<PlayerTimer> list = PlayerTimer.find("endAt > ? and endAt < ?", endtime.toDate(), new Date()).fetch();
		return list;
	}

	public void resetTimerOnBuilding(int remainingQuantity) {
		if (this.building == null)
			return;
		Building building = this.building;
		// How many cycles before expiration ??
		int needByCycle = building.level;
		int cycles = remainingQuantity / needByCycle;
		// How many seconds by cycle ?
		int secondsByCycle = building.productionTime;
		// Finally, how many seconds before expiration ?
		int seconds = secondsByCycle * cycles;
		// When will that be ?
		DateTime expiration = new DateTime().plusSeconds(seconds);
		this.startAt = new Date(); // now
		this.endAt = expiration.toDate();
		this.save();
	}

	public static List<PlayerTimer> getTimersByType(Player player, int type, boolean remind) {
		// Security
		if (player.timers == null || player.timers.size() < 1)
			return null;
		if (type != PlayerTimer.TIMER_ON_DURATION
				&& type != PlayerTimer.TIMER_ON_DATE
				&& type != PlayerTimer.TIMER_ON_BUILDING)
			return null;
		// Fetch
		List<PlayerTimer> list = PlayerTimer.find("byPlayerAndTypeAndRemind", player,
				type, remind).fetch();
		// Order
		Collections.sort(list, PlayerTimer.BY_END_DATE_ASC);
		// return
		return list;
	}
	
	public static List<PlayerTimer> getTimersByType(Player player, int type) {
		return PlayerTimer.getTimersByType(player, type, true);
	}

	public Duration getDuration() {
		DateTime st = new DateTime(this.startAt);
		DateTime ed = new DateTime(this.endAt);
		Duration dur = new Duration(st, ed);
		return dur;
	}

	public HashMap<String, Integer> getTimeBeforeExpiration() {
		DateTime now = new DateTime();
		DateTime end = new DateTime(this.endAt);
		HashMap<String, Integer> exp = new HashMap();

		if (now.isAfter(end)) {
			exp.put("w", 0);
			exp.put("d", 0);
			exp.put("h", 0);
			exp.put("m", 0);
			exp.put("s", 0);
			return exp;
		}

		Duration dur = new Interval(now, end).toDuration();
		Period per = dur.toPeriod();

		exp.put("w", per.getWeeks());
		exp.put("d", per.getDays());
		exp.put("h", per.getHours());
		exp.put("m", per.getMinutes());
		exp.put("s", per.getSeconds());
		return exp;
	}

	public HashMap<String, Integer> getTimeAfterExpiration() {
		DateTime now = new DateTime();
		DateTime end = new DateTime(this.endAt);
		HashMap<String, Integer> exp = new HashMap();

		if (now.isBefore(end)) {
			exp.put("w", 0);
			exp.put("d", 0);
			exp.put("h", 0);
			exp.put("m", 0);
			exp.put("s", 0);
			return exp;
		}

		Duration dur = new Interval(end, now).toDuration();
		Period per = dur.toPeriod();

		exp.put("w", per.getWeeks());
		exp.put("d", per.getDays());
		exp.put("h", per.getHours());
		exp.put("m", per.getMinutes());
		exp.put("s", per.getSeconds());
		return exp;
	}

	public boolean hasExpired() {
		DateTime now = new DateTime();
		DateTime end = new DateTime(this.endAt);
		if (now.isAfter(end))
			return true;
		else
			return false;
	}

	public boolean doesExpireSoon() {
		DateTime now = new DateTime();
		DateTime end = new DateTime(this.endAt);
		DateTime start = new DateTime(this.startAt);
		if (now.isAfter(end))
			return false;
		Duration r = new Interval(start, end).toDuration();
		Duration d = new Interval(now, end).toDuration();
		if (d.getMillis() < r.getMillis() / 10)
			return true;
		else
			return false;
	}

	public static final Comparator<PlayerTimer> BY_END_DATE_ASC = new Comparator<PlayerTimer>() {
		@Override
		public int compare(PlayerTimer b1, PlayerTimer b2) {
			return b1.endAt.compareTo(b2.endAt);
		}
	};

	public static final Comparator<PlayerTimer> BY_END_DATE_DESC = new Comparator<PlayerTimer>() {
		@Override
		public int compare(PlayerTimer b1, PlayerTimer b2) {
			return b2.endAt.compareTo(b1.endAt);
		}
	};

	public static final Comparator<PlayerTimer> BY_START_DATE_ASC = new Comparator<PlayerTimer>() {
		@Override
		public int compare(PlayerTimer b1, PlayerTimer b2) {
			return b1.startAt.compareTo(b2.startAt);
		}
	};

	public static final Comparator<PlayerTimer> BY_START_DATE_DESC = new Comparator<PlayerTimer>() {
		@Override
		public int compare(PlayerTimer b1, PlayerTimer b2) {
			return b2.startAt.compareTo(b1.startAt);
		}
	};

	public static final Comparator<PlayerTimer> BY_TYPE_ASC = new Comparator<PlayerTimer>() {
		@Override
		public int compare(PlayerTimer b1, PlayerTimer b2) {
			return new Integer(b1.type).compareTo(new Integer(b2.type));
		}
	};

	public static final Comparator<PlayerTimer> BY_TYPE_DESC = new Comparator<PlayerTimer>() {
		@Override
		public int compare(PlayerTimer b1, PlayerTimer b2) {
			return new Integer(b2.type).compareTo(new Integer(b1.type));
		}
	};

}
