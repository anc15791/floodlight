/**
 * 
 */
package net.floodlightcontroller.ipblacklist;

import java.net.InetAddress;
import java.util.ArrayList;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.firewall.FirewallRule;

/**
 * @author anc
 *
 */
public interface IipblacklistService extends IFloodlightService {

	public ArrayList<InetAddress> getList();
	
	public void addRule(InetAddress ipaddr);
	
	public void deleteRule(InetAddress ipaddr);
}
