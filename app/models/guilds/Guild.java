package models.guilds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import models.players.Player;
import models.polls.Poll;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;

import play.db.jpa.Model;
import world.World;

@Entity
public class Guild extends Model {

    public String name = "";
    public String tag = "";

    public String bannerId = "00";

    public Date createdAt = new Date();

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    public List<Membership> members = new ArrayList<Membership>();

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    public List<GuildOldMember> oldmembers = new ArrayList<GuildOldMember>();

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    public List<GuildWallMessage> wall = new ArrayList<GuildWallMessage>();

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    public List<GuildLink> links = new ArrayList<GuildLink>();

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    public List<GuildEvent> events = new ArrayList<GuildEvent>();

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String welcomeMessage = "";

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String recruitmentPolicy = "";
    @Type(type = "org.hibernate.type.BooleanType")
    public boolean recruitmentOpened = false;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String generalPolicy = "";

    public Date odayMemberTime;
    public String odayMemberName;

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    public List<Poll> polls = new ArrayList<Poll>();

    @Type(type = "org.hibernate.type.BooleanType")
    public boolean officersCanAdmin = false;

    //----------------------------------

    public Guild(String name, String tag) {
    	this.name = name;
    	this.tag = tag;
    	this.createdAt = new Date();
    }

    public static Guild findByTag(String guildTag) {
    	if(guildTag != null && !guildTag.isEmpty()) 
    		return Guild.find("byTag", guildTag).first();
    	else
    		return null;
    }
    
    public static Guild findByName(String guildName) {
    	if(guildName != null && !guildName.isEmpty()) 
    		return Guild.find("byName", guildName).first();
    	else
    		return null;
    }

    public String getBanner() {
        if (this.bannerId == null || this.bannerId.isEmpty())
            return World.GUILDS.get("00").getImage();
        else
            return World.GUILDS.get(this.bannerId).getImage();
    }

    public GuildWallMessage addWallMessage(String author, String content) {
        Date publishedAt = new Date();
        GuildWallMessage gwm = new GuildWallMessage(author, publishedAt, this, content).save();
        this.wall.add(gwm);
        this.save();
        return gwm;
    }

    public GuildLink addLink(String name, String link, String description, int weight) {
    	GuildLink lnk = new GuildLink(this, name, link, description, weight).save();
    	this.links.add(lnk);
    	this.save();
    	System.out.println(lnk);
    	return lnk;
    }

    public boolean addPlayer(String playerName) {
        Player player = Player.find(playerName);

        // Check if already member
        if(this.isMember(player))
        	return false;

        // Check again, because 2 Memberships for the same player cause a nasty DB error for all users
        Membership test = Membership.findByPlayer(playerName);
        if(test != null)
        	return false;

        Membership m = new Membership(this, player).save();
        this.members.add(m);
        this.save();
        player.membership = m;
        player.save();
        return true;
    }

    public void removePlayer(String playerName) {
    	this.removePlayer(playerName, "?", false);
    }

    public void removePlayer(String playerName, String reason, boolean kicked) {
        Player player = Player.find(playerName);
        if (this.isMember(player)) {
        	// Create old membership
        	GuildOldMember om = new GuildOldMember(player.membership, reason, kicked).save();
        	this.oldmembers.add(om);
        	this.save();
        	// Destroy current membership
            this.members.remove(player.membership);
            Membership mb = player.membership;
            player.membership = null;
            player.save();
            this.save();
            mb.delete();
        }
    }

    public void setPlayerRank(String playerName, int rank) {
        Player player = Player.find(playerName);
        if (this.isMember(player)) {
            if (rank == Membership.RANK_1_MEMBER || rank == Membership.RANK_2_SENIOR_MEMBER || rank == Membership.RANK_3_OFFICER || rank == Membership.RANK_4_GUILDMASTER) {
                player.membership.rank = rank;
                player.membership.save();
            }
        }
    }

    public boolean isMember(Player p) {
    	if(p == null || p.membership == null)
    		return false;

        if (p.hasGuild() && p.membership.guild.id == this.id)
            return true;
        else
            return false;
    }

    public Player getRandomMember() {
        // Count the number of players in the guild
        int max = this.members.size();
        if(max < 1)
        	return null;
        // Get a random number
        Random randGen = new Random();
        int rand = randGen.nextInt(max);
        // Get that player
        Membership ms = this.members.get(rand);
        return ms.player;
    }

    @Deprecated
    public void rollODayMember() {
        // Protect from null pointer exception
        if (this.odayMemberTime == null || this.odayMemberName == null) {
            this.odayMemberTime = new Date();
            this.odayMemberName = this.getRandomMember().name;
            this.save();
            return;
        }
        // Do we need to roll ?
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date()); // now
        cal2.setTime(this.odayMemberTime);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        // Let's roll :)
        if (!sameDay) {
            this.odayMemberName = this.getRandomMember().name;
            this.odayMemberTime = new Date();
            this.save();
        }
    }

    public boolean cronRollODayMember() {
    	// Protect from null pointer exception
        if (this.odayMemberTime == null || this.odayMemberName == null) {
            this.odayMemberTime = new Date();
            Player member = this.getRandomMember();
        	if(member == null) 
        		return false;
            this.odayMemberName = member.name;
            this.save();
            return true;
        }
        // Do we need to roll ?
        DateTime now = new DateTime();
        DateTime last = new DateTime(this.odayMemberTime);
        if(now.isAfter(last)) {
        	int diff = Hours.hoursBetween(last, now).getHours();
            if(diff > 23) { // if we set 24, we'll have to run the job at 0:00 and 0:01 the day after, and that's not good
                // roll
            	Player member = this.getRandomMember();
            	if(member == null) 
            		return false;
                this.odayMemberName = member.name;
                this.odayMemberTime = new Date();
                this.save();
                return true;
            }
        }
        return false;
    }

	public List<GuildEvent> getFutureEvents() {
		List<GuildEvent> events = new ArrayList();
		Iterator<GuildEvent> it = this.events.iterator();
		while(it.hasNext()) {
			GuildEvent e = it.next();
			Date now = new Date();
			if(e.startAt.before(now) && e.endAt.after(now))
				events.add(e);
		}
		return events;
	}

	public List<Membership> getConfirmedMembers() {
		return Membership.find("guild = ? and confirmed = ?", this, true).fetch();
	}

	public List<Membership> getUnconfirmedMembers() {
		return Membership.find("guild = ? and confirmed = ?", this, false).fetch();
	}

    @Override
    public String toString() {
        return this.name;
    }

	public static final Comparator<Guild> BY_NAME = new Comparator<Guild>() {
		@Override
        public int compare(Guild b1, Guild b2) {
			return b1.name.compareTo(b2.name);
        }
	};

}
