package models.realm.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import world.StatsContainer;
import world.WorldGood;

public class GoodEvolution {

	public String name;
	
	public List<StatsContainer> produced = new ArrayList<StatsContainer>();
	public List<StatsContainer> needed = new ArrayList<StatsContainer>();
	public List<StatsContainer> ratio = new ArrayList<StatsContainer>();
	
}
