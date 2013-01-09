package controllers;

import play.Logger;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;

public class Logs extends Controller {

	@Before
    static void LOG_REQUEST() {
		String ajax = "";
		if(request.isAjax())
			ajax = "AJAX";
    	String log = request.method + " " + request.action + " (" + request.contentType + ajax + ") " + request.routeArgs
    			+ "\n" + request.remoteAddress + " " + request.headers.get("user-agent")
    			+ "\n" + request.host + request.url;
    	Logger.info(log);
    }

	@Finally
    static void LOG_RESPONSE(Throwable e) {
    	String ajax = "";
		if(request.isAjax())
			ajax = "AJAX";
    	String log = request.method + " " + request.action + " (" + request.contentType + ajax + ") "
    			+ " :: " + request.remoteAddress;
		if( e == null ){
			Logger.info(log);
		}else{
			Logger.error("EXCEPTION -- " + log, e);
		}
    }

    @Catch(value = Throwable.class, priority = 1)
    static void LOG_EXCEPTION(Throwable throwable) {
        Logger.error("EXCEPTION %s", throwable);
    }

}
