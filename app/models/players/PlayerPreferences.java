package models.players;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;

import controllers.Preferences;

import play.db.jpa.Model;
import world.World;

@Entity
public class PlayerPreferences extends Model {
    public String PASSWORD_HASH = "";

    public String AVATARID = "00"; // 00 is anonymous

    public String customstyle = "default";
    
    public String EMAIL = "";
    
	public Boolean SHOW_ADDBUILDING = false;
	public Boolean ENABLE_SECURITY = false;
    public Boolean AUTH_COOKIE = false;

    public Boolean SHOW_ALL_BUILDINGS = false;
    public Boolean SHOW_CALCULATIONS = true;
    public Boolean SHOW_BUILDINGS_DISABLED = true;
    public Boolean SHOW_BUILDINGS_BOOSTED = true;
    public Boolean SHOW_BUILDINGS_SIMULATED = true;
    public Boolean SHOW_ADVISES = true;
    public Boolean SHOW_UNITS = true;
    public Boolean SHOW_BUILDING_DESCRIPTION_IN_LIST = false;
    public Boolean AREAS_IMAGE_MAP = false;
    public Boolean SHOW_AREAS = true;
    public Boolean SHOW_GROUPS = true;
    public Boolean SHOW_PRODUCTION_RELATIVE = true;

    public Boolean UNFOLD_PRODUCTION = true;
    public Boolean UNFOLD_BUILDINGS = true;
    public Boolean UNFOLD_CALCULATIONS = false;
    public Boolean UNFOLD_ADVISES = false;
    public Boolean UNFOLD_UNITS = false;
    public Boolean UNFOLD_BUILDINGS_BOOSTED = false;
    public Boolean UNFOLD_BUILDINGS_DISABLED = false;
    public Boolean UNFOLD_BUILDINGS_SIMULATED = false;
    public Boolean UNFOLD_AREAS = false;
    public Boolean UNFOLD_GROUPS = false;
    public Boolean UNFOLD_PRODUCTION_RELATIVE = true;

    public String BUILDINGS_LIST_TYPE_SORT = "ID";
    public String BUILDINGS_LIST_TYPE_ORDER = "ASC";
    public String BUILDINGS_LIST_AREA_SORT = "ID";
    public String BUILDINGS_LIST_AREA_ORDER = "ASC";
    public String BUILDINGS_LIST_GROUP_SORT = "ID";
    public String BUILDINGS_LIST_GROUP_ORDER = "ASC";
    public String BUILDINGS_LIST_BOOSTED_SORT = "ID";
    public String BUILDINGS_LIST_BOOSTED_ORDER = "ASC";
    public String BUILDINGS_LIST_STOPPED_SORT = "ID";
    public String BUILDINGS_LIST_STOPPED_ORDER = "ASC";
    public String BUILDINGS_LIST_SIMULATED_SORT = "ID";
    public String BUILDINGS_LIST_SIMULATED_ORDER = "ASC";

    public Boolean BUILDING_LEVEL_LOOP = false;

    public Boolean MAIL_NOTIFY_TIMERS_EXPIRING = false;
    public Boolean MAIL_NOTIFY_TIMERS_EXPIRED = false;
    
    public Boolean ENABLE_ACTIVITY_FEED = true;
    
    public Boolean HOME_SHOW_PUBLIC_MENU = true;
    public Boolean HOME_SHOW_STATS = true;
    public Boolean HOME_SHOW_VISITS = true;
    public Boolean HOME_SHOW_LAST_CONNECTION = true;
    public Boolean HOME_SHOW_TIMERS = true;
    public Boolean HOME_SHOW_NEXT_ADVENTURES = true;
    public Boolean HOME_SHOW_LAST_ADVENTURES = true;
    public Boolean HOME_SHOW_LAST_ADVENTURES_COMMENTS = true;
    public Boolean HOME_SHOW_LAST_BUILDINGS = true;
    public Boolean HOME_SHOW_NOTA_BENE = true;
    public Boolean HOME_SHOW_ACTIVITY = true;
    public Boolean HOME_SHOW_FRIENDS = true;
    public Boolean HOME_SHOW_FRIENDS_ACTIVITY = true;
    public Boolean HOME_SHOW_CHANGELOG = true;
    
    /** 
     * <b>Overlay</b>
     */
    public Boolean TIMERS_DEFAULT_REMIND_TYPE1 = false;
    public Boolean TIMERS_DEFAULT_REMIND_TYPE2 = false;
    public Boolean TIMERS_DEFAULT_REMIND_TYPE3 = false;
    public Boolean TIMERS_SHOW_ON_HOME_TYPE1 = false;
    public Boolean TIMERS_SHOW_ON_HOME_TYPE2 = false;
    public Boolean TIMERS_SHOW_ON_HOME_TYPE3 = false;    
    public Integer TIMERS_OVERLAY_OPACITY = 80;

    
    /**
     * <b>Home_Show_Tips</b> : whether to show or hide the Tips advises from the home page.
     */
    public Boolean HOME_SHOW_TIPS = true;
    
    /**
     * <b>Do_Not_Track</b> : disable Google Analytics for the given profile, when set to true.
     */
    public Boolean DO_NOT_TRACK = false;
    
    /**
     * <u>Groups</u> : show a condensed or a full list of groups.
     * <br />The <b>condensed</b> list show the group name, and the number of buildings in it
     * <br />The <b>simple</b> list show additional details, such as the groups creation date
     */
    public Boolean SHOW_CONDENSED_GROUPS = false;
    
    /**
     * <u>Building Line</u>: show or hide the <b>description</b> sub-line.
     * <br />Show = true ; Hide = false ; default = false
     */
    public Boolean LINE_SHOW_DESCRIPTION = false;
    /**
     * <u>Building Line</u>: show or hide the <b>groups</b> sub-line.
     * <br />Show = true ; Hide = false ; default = false
     */
    public Boolean LINE_SHOW_GROUPS = false;
    
    /**
     * <u>Building line</u>: show or hide the <b>deposit</b>, if any, sub-line.
     * <br />Show = true ; Hide = false ; default = true
     */
    public Boolean LINE_SHOW_DEPOSIT = true;
    
	public PlayerPreferences() {
		super();
	}

	public String getAvatar() {
		if(this.AVATARID == null || this.AVATARID.isEmpty())
			return World.AVATARS.get("00").getImage();
		else
			return World.AVATARS.get(this.AVATARID).getImage();
	}
	
	/**
	 * Returns a random password with the given length.
	 * Available chars are :
	 * abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890,?;.:/!%=+-_()[]&@
	 * @param length
	 * @return the password
	 */
	public static String makeRandomPassword(int length) {
		// Char map to pick from
		char[] charmap = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k',
				'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
				'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M',
				'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
				'2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '@', '!',
				'?', ',', ';', '.', ':', '/', '%', '(', ')', '[', ']', '=',
				'&', '_'};
		// Logic
		Random r = new Random();
		StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < length; i++) {
	      sb.append(charmap[r.nextInt(charmap.length)]);
	    }
	    String res = sb.toString();
	    return res;
	}

	/**
	 * Returns a secured hash of the given password.
	 * @param clearPassword
	 * @return
	 */
	public static String hashPassword(String clearPassword) {
		byte[] toHash = clearPassword.getBytes();
        byte[] MD5Digest = null;
        StringBuilder hashString = new StringBuilder();

        // get MD5 sum
        MessageDigest algo;
		try {
			algo = MessageDigest.getInstance("MD5");
	        algo.reset();
	        algo.update(toHash);
	        MD5Digest = algo.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return clearPassword;
		}
        // Get hexadecimal digest
        for (int i = 0; i < MD5Digest.length; i++) {
            String hex = Integer.toHexString(MD5Digest[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        // return
        return hashString.toString();
	}
	
	/**
	 * Returns <b>true</b> if this player has set an email address, <b>false</b> if not.
	 * @return true/false
	 */
	public boolean hasMailAddress() {
		if(this.EMAIL == null || this.EMAIL.isEmpty())
			return false;
		else
			return true;
	}

	@Override
	public String toString() {
		return new Long(this.id).toString();
	}
	
	/**
	 * Update the given preference with the given value.
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean updatePreference(String key, String value) {
		boolean ok = true;
		try {
			// Get the field
			Field f = this.getClass().getField(key.toUpperCase());
			// Update the field
			if(Boolean.class.isAssignableFrom(f.getType()))
				f.set(this, Boolean.parseBoolean(value));
			else if(Integer.class.isAssignableFrom(f.getType()))
				f.set(this, Integer.parseInt(value));
			else if(String.class.isAssignableFrom(f.getType()))
				f.set(this, value);
				
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
			ok = false;
		} catch(IllegalAccessException e) {
			e.printStackTrace();
			ok = false;
		}
		return ok;
	}
	
	/**
	 * Return the opacity of the timers reminders overlay frame, in a format that
	 * can be used in a CSS tag "opacity: 0.X;"
	 * @return
	 */
	public String getOverlayOpacity() {
		String res;
		try {
			res = "" + (double) this.TIMERS_OVERLAY_OPACITY / 100;
		} catch(Exception e) {
			res = "1";
		}
		return res;
	}

}
