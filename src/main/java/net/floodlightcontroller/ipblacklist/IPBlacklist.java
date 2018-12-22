package net.floodlightcontroller.ipblacklist;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.EthType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.topology.ITopologyService;

public class IPBlacklist implements IFloodlightModule, IOFMessageListener, IipblacklistService {

	private static IFloodlightProviderService mProvider;
	protected static Logger logger;
	private static ArrayList<InetAddress> blacklist; 
	protected IRestApiService restApi;
	StringWriter sw;
	PrintWriter pw;
	
	@Override
	public String getName() {
		return IPBlacklist.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return( type.equals( OFType.PACKET_IN ) && name.equals( "forwarding" ) );
		
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch( msg.getType() ) {
		case PACKET_IN: // Handle incoming packets here				  
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
			// If the packet isn’t IPv4, ignore.
			if( eth.getEtherType() != EthType.IPv4 ){
				return Command.CONTINUE;
			}
			IPv4 ipv4 = (IPv4) eth.getPayload();
			InetAddress ipAddr;

			// Get the IP address					
			ipAddr = ipv4.getDestinationAddress().toInetAddress();
			logger.info("Destination IP: {} ",ipAddr.toString());
			ipAddr = (InetAddress)ipAddr;
            //Drop if IP is in blacklist. We will perform Command.STOP so that no one else modifies our drop action.    
			if( checkBlacklist(ipAddr)  ){
				logger.info("black list ip matched, dropping: {} ",ipAddr.toString());
				FlowMgr.getInstance().dropPacket( sw,cntx, (OFPacketIn)msg,logger );
				return Command.STOP; // Done with this packet,							 
			}					
			//Forward the packet if IP not in blacklist.
            //We use Command.CONTINUE because there can be other modules that will want to change the forwarding behaviour as well.
			logger.info("Destination IP not in blacklist,forwarding packet. ",ipAddr.toString());
			return Command.CONTINUE;								 

		default: break; // If not a PACKET_IN, just return
		}		 
		return Command.CONTINUE;	
	}

	private boolean checkBlacklist(InetAddress ipAddr) {
		
		if(blacklist.isEmpty()) {
			logger.info("empty blacklist");
			return false;
		}
		for(InetAddress s:blacklist) {
			if(s.equals(ipAddr)) {
				return true;	
			}		
		}
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IipblacklistService.class);
	    return l;

	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	    m.put(IipblacklistService.class, this);
	    return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l =
		        new ArrayList<Class<? extends IFloodlightService>>();
		    
		l.add(IFloodlightProviderService.class);
        l.add(ITopologyService.class);	
        l.add(IRestApiService.class);
		return l;
	} 

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		mProvider = context.getServiceImpl(IFloodlightProviderService.class );
		logger = LoggerFactory.getLogger(IPBlacklist.class);
		blacklist = new ArrayList<InetAddress>();
		restApi = context.getServiceImpl(IRestApiService.class);
		sw= new StringWriter();
		pw= new PrintWriter(sw);
		try {
			blacklist=BlacklistMgr.GetIpList();
		} catch (FileNotFoundException | UnknownHostException e1) {
			e1.printStackTrace(pw);
			String sStackTrace = sw.toString(); // stack trace as a string
			logger.info(sStackTrace);
		}
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		mProvider.addOFMessageListener( OFType.PACKET_IN, this );
		restApi.addRestletRoutable(new ipblacklistWebRoutable());

	}

	@Override
	public ArrayList<InetAddress> getList() {
		// TODO Auto-generated method stub
		return blacklist;
	}

	@Override
	public void addRule(InetAddress ipaddr) {
		try {
			
			blacklist=BlacklistMgr.GetIpList();
			if(blacklist.contains(ipaddr))
				return;
			
			blacklist.add(ipaddr);
			BlacklistMgr.AddtoList(ipaddr.getHostAddress());
			
			
			
		} catch (IOException e1) {
			e1.printStackTrace(pw);
			String sStackTrace = sw.toString(); // stack trace as a string
			logger.info(sStackTrace);
		}
		
	}

	@Override
	public void deleteRule(InetAddress ipaddr) {
		try {
			
			blacklist=BlacklistMgr.GetIpList();
			if(!blacklist.contains(ipaddr))
				return;
			
			blacklist.remove(ipaddr);
			BlacklistMgr.RemoveFromList(blacklist);
			
			
			
		} catch (IOException e1) {
			e1.printStackTrace(pw);
			String sStackTrace = sw.toString(); // stack trace as a string
			logger.info(sStackTrace);
		}
		
		
	}

}
