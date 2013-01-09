package jobs;

import java.io.InputStream;

import play.Logger;
import play.jobs.Job;
import play.jobs.On;

@On("0 0 4 * * ?") // every day at 4 am
public class Backup extends Job<String> {

	/**
	 * This method runs a ruby script calling pg_dump, in order to make a backup of the PosgreSQL database
	 * and compress it in a .gz file.
	 */
	public String doJobWithResult() {
		//ruby rdump.rb --database=playsettlers --full-dump --here
		try {
			Runtime runtime = Runtime.getRuntime();
			Process p = runtime.exec(new String[]{"ruby", "/Users/guillaume/Dropbox/Dev/web/myrealm/pg_dumps/rdump.rb", "--database=playsettlers", "--full-dump", "--here"});
			Logger.info("Databased Backed up");
			// Get the input stream and read from it
		    InputStream in = p.getInputStream();
		    int c;
		    String result = "";
		    while ((c = in.read()) != -1) {
		        result += ((char)c);
		    }
		    in.close();
		    return result;
		} catch(Exception e) {
			Logger.error("DATABASE BACKUP FAILED", e);
			return "Backup failed ! " + e;
		}
	}

}
