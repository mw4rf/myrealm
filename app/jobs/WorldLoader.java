package jobs;

import models.admin.IrcBot;
import irc.IRC;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import world.World;

@OnApplicationStart
public class WorldLoader extends Job {
	
	public String uuid = "";
	
	public WorldLoader(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * This is the main method launched on server start. It MUST run before the server starts serving HTTP requests
	 * and rendering pages. Indeed, it creates a new instance of {@link World} class. {@link World} class has
	 * a very important constructor : it call several private methods loading all the game data from XML files.
	 */
	@Override
	public String doJobWithResult() {
		String result = "";
		Logger.info("Starting WorldLoader Job.");
		Cache.set("JobStatus_" + uuid + "_done", false);
		Cache.set("JobStatus_" + uuid + "_message", "<p>Starting WorldLoader Job.</p>");
		Cache.set("JobStatus_" + uuid + "_percent", 0);
		Cache.set("JobStatus_" + uuid + "_current", "Loading World from XML.");
        try {
        	// LOAD WORLD
            World w = new World();
            // Informations
            String res = "World has " + World.BUILDINGS.size() + " buildings, " 
            			+ World.GOODS.size() + " goods, " 
            			+ World.BOOSTS.size() + " boosts, " 
            			+ World.ADVENTURES.size() + " adventures, " 
            			+ World.AVATARS.size() + " avatars, " 
            			+ World.GUILDS.size() + " guild banners, " 
            			+ World.SOLDIERS.size() + " soldiers, "
            			+ World.CUSTOMSTYLES.size() + " custom CSS Styles, "
            			+ World.TIPS.size() + " tips.";
            Logger.info(res);
            Logger.info("WORLD LOADED");
            System.out.println("WORLD LOADED");
    		Cache.set("JobStatus_" + uuid + "_done", true);
    		Cache.set("JobStatus_" + uuid + "_message", "<p style='color:green;'>World Loaded. Job done.</p><p>" + res + "</p>" + Cache.get("JobStatus_" + uuid + "_message"));
    		Cache.set("JobStatus_" + uuid + "_percent", 100);
    		Cache.set("JobStatus_" + uuid + "_current", "World Loaded");
        } catch(Exception e) {
            Logger.info("FATAL ERROR: World failed to load!");
            System.out.println("FATAL ERROR: World failed to load!");
    		Cache.set("JobStatus_" + uuid + "_message", "<p style='color:red;'>FATAL ERROR: World failed to load!</p>" + Cache.get("JobStatus_" + uuid + "_message"));
			Cache.set("JobStatus_" + uuid + "_percent", 0);
			Cache.set("JobStatus_" + uuid + "_current", "FATAL ERROR!");
			Cache.set("JobStatus_" + uuid + "_done", true);
        }
        return result;
	}
	
}
