package jobs;
import irc.IRC;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import models.admin.IrcBot;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.log4play.PlayWebSocketLogAppender;
import world.World;

@OnApplicationStart(async = true)
public class IrcLoader extends Job {

    @Override
    public void doJob() {
        // LOAD BOTS
        List<IrcBot> BOTS = IrcBot.findAll();
        Iterator<IrcBot> it = BOTS.iterator();
        while(it.hasNext()) {
        	IrcBot bot = it.next();
        	bot.start();
        	Logger.info("[JOB] IrcLoader : " + bot.toString() + " started.");
        }
        Logger.info("IRC BOTS LOADED");
    }


}