package world;

import java.util.ArrayList;
import java.util.List;

import models.realm.Building;

public class BuildingSet extends ArrayList<Building> {

	
	private String name;
	
	public BuildingSet(String name) {
		this.name = name;
	}
	
	public String getBuildingName() {
		return this.name;
	}
	
}
