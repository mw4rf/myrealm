package jobs;

import notifiers.AdminMailers;
import play.jobs.Every;
import play.jobs.Job;

@Every("12h")
public class AdminNotifier extends Job {

	@Override
	public void doJob() throws Exception {
		AdminMailers.activityReport();
	}
	
	
	
}
