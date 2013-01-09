package world;

import java.util.Random;

public class Tip {

	protected int id = 0;
	protected String value = "";
	protected String link = null;
	
	/**
	 * Constructor
	 * @param id
	 * @param value
	 */
	public Tip(int id, String value, String link) {
		this.id = id;
		this.value = value;
		this.link = link;
	}
	
	/**
	 * Returns the value (i.e. text) of the tip
	 * @return
	 */
	public String getTipValue() {
		String res = this.value.replaceAll("%s", "<a href=\"" +  this.link + "\">" + this.link + "</a>");
		return res;
	}
	
	/**
	 * Return the unique id of the tip
	 * @return
	 */
	public int getTipId() {
		return this.id;
	}
	
	/**
	 * Return the {@link String} value of this object.
	 */
	@Override
	public String toString() {
		return "#" + this.id;
	}
	
	/**
	 * Returns a random Tip.
	 * @return
	 */
	public static Tip getRandomTip() {
		int count = World.TIPS.size();
		Random rand = new Random();
		int id = rand.nextInt(count) + 1; // the first tip is id#1 but nextInt() starts at 0
		return World.TIPS.get(id);
	}
	
}
