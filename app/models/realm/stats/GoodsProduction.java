package models.realm.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import world.StatsContainer;

public class GoodsProduction {

	public String name;
	
	public double produced;
	public double needed;
	
	public GoodsProduction(String name, double produced, double needed) {
		super();
		this.name = name;
		this.produced = produced;
		this.needed = needed;
	}
	
    public static final Comparator<GoodsProduction> BY_NAME = new Comparator<GoodsProduction>() {
		@Override
        public int compare(GoodsProduction b1, GoodsProduction b2) {
			return b1.name.compareTo(b2.name);
		}
	};
	
	
	
}
