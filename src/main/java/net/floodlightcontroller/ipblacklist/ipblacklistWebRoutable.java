package net.floodlightcontroller.ipblacklist;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.firewall.FirewallRulesResource;
import net.floodlightcontroller.restserver.RestletRoutable;

public class ipblacklistWebRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("/ipblacklist/json",ipblacklistResource.class);

        return router;
	}

	@Override
	public String basePath() {
		return "/wm/ipblacklist";
	}

}
