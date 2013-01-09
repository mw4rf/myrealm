package models.realm.stats;

import java.util.ArrayList;
import java.util.List;

import world.StatsContainer;

public class BuildingsStatus {

	public String name;
	
	public List<StatsContainer> stopped = new ArrayList<StatsContainer>();
	public List<StatsContainer> boosted = new ArrayList<StatsContainer>();
	
}
