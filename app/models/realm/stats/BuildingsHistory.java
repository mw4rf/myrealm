package models.realm.stats;

import java.util.ArrayList;
import java.util.List;

import world.StatsContainer;

public class BuildingsHistory {
	
	public String name;
	
	public List<StatsContainer> count = new ArrayList<StatsContainer>();
	public List<StatsContainer> averageLevel = new ArrayList<StatsContainer>();
	
}
