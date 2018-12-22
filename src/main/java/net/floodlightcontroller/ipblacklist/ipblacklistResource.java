package net.floodlightcontroller.ipblacklist;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import net.floodlightcontroller.firewall.FirewallRule;
import net.floodlightcontroller.firewall.IFirewallService;

public class ipblacklistResource extends ServerResource{

	protected static Logger log= LoggerFactory.getLogger(IPBlacklist.class);
	
	@Get("json")
	public ArrayList<InetAddress> retrieve() {
		IipblacklistService ipblacklist =
				(IipblacklistService)getContext().getAttributes().
				get(IipblacklistService.class.getCanonicalName());

		return ipblacklist.getList();
	}
	
	
	
	@Post
	public String store(String fmJson) {
		IipblacklistService ipblacklist =
				(IipblacklistService)getContext().getAttributes().
				get(IipblacklistService.class.getCanonicalName());

		InetAddress ip = jsonToFirewallRule(fmJson);
		if (ip == null) {
			return "{\"status\" : \"Error! Could not parse ip address.\"}";
		}
		
		String status = null;
		ipblacklist.addRule(ip);
		status = "ip added";
		return ("{\"status\" : \"" + status + "\", \"ip\" : \""+ip.getHostAddress() + "\"}");

		
	}
	
	
	
	@Delete
	public String remove(String fmJson) {
		IipblacklistService ipblacklist =
				(IipblacklistService)getContext().getAttributes().
				get(IipblacklistService.class.getCanonicalName());
		
		InetAddress ip = jsonToFirewallRule(fmJson);
		if (ip == null) {
			//TODO compose the error with a json formatter
			return "{\"status\" : \"Error! Could not parse ip address.\"}";
		}
		
		String status = null;
		ipblacklist.deleteRule(ip);
		status = "ip deleted";
		return ("{\"status\" : \"" + status + "\", \"ip\" : \""+ip.getHostAddress() + "\"}");
		
	}
	
	
	public static InetAddress jsonToFirewallRule(String fmJson) {
		InetAddress ipaddr=null;
		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp;
		
		
		try {
			try {
				jp = f.createParser(fmJson);
			} catch (JsonParseException e) {
				throw new IOException(e);
			}

			jp.nextToken();
			if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected START_OBJECT");
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
					throw new IOException("Expected FIELD_NAME");
				}

				String n = jp.getCurrentName();
				jp.nextToken();
				if (jp.getText().equals("")) {
					continue;
				}


				if (n.equalsIgnoreCase("ip")) {
					try {
						ipaddr = InetAddress.getByName(jp.getText());
					} catch (IllegalArgumentException e) {
						log.error("Unable to parse ip: {}", jp.getText());
					} catch (UnknownHostException e) {
						log.error("Unable to convert ip to InetAddress: {}", jp.getText());
					}
					
				}
			}
		} catch (IOException e) {
			log.error("Unable to parse JSON string: {}", e);
		}

		return ipaddr;
		

	}
	
}
