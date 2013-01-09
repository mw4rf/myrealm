package serializers;

import models.players.Player;

public class PlayerWrapper {
	
	Long id;
	String name;
	
	public PlayerWrapper(Player p) {
		this.id = p.id;
		this.name = p.name;
	}

}
