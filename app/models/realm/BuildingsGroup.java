package models.realm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.players.Player;

import play.db.jpa.Model;

@Entity
public class BuildingsGroup extends Model {
	
	@ManyToOne
	public Player player;
	
	@ManyToMany(mappedBy = "groups")
	public List<Building> buildings = new ArrayList<Building>();
	
	public String name;
	public String fgcolor;
	public String bgcolor;
	
	public Date createdAt;
	public Date updatedAt;
	
	public static BuildingsGroup find(String name) {
		if(name == null || name.isEmpty())
			return null;
		return BuildingsGroup.find("byName", name).first();
	}
	
	public BuildingsGroup(Player player, String name, String fgcolor, String bgcolor) {
		this.player = player;
		this.name = name;
		this.fgcolor = fgcolor;
		this.bgcolor = bgcolor;
		this.createdAt = new Date();
		this.updatedAt = this.createdAt;
	}
	
	public void addBuilding(Building building) {
		this.buildings.add(building);
		this.updatedAt = new Date();
		this.save();
		building.groups.add(this);
		building.save();
	}
	
	public void removeBuilding(Building building) {
		if(!this.buildings.contains(building))
			return;
		this.buildings.remove(building);
		this.updatedAt = new Date();
		building.groups.remove(this);
		this.save();
		building.save();
	}
	
	public boolean hasBuilding(Building building) {
		Iterator<Building> it = this.buildings.iterator();
		while(it.hasNext())
			if(it.next() == building)
				return true;
		return false;
	}
	
}
