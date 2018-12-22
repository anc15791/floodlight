package net.floodlightcontroller.ipblacklist;

import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;

import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

import org.slf4j.Logger;
import org.projectfloodlight.openflow.protocol.*;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.forwarding.Forwarding;
import net.floodlightcontroller.util.OFMessageUtils;


public class FlowMgr extends Forwarding
{
	private static final FlowMgr INSTANCE = new FlowMgr();

	private FlowMgr(){
		// private constructor - prevent external instantiation
	}

	public static FlowMgr getInstance(){
		return INSTANCE;
	}

	
	public void dropPacket( final IOFSwitch ofSwitch, final FloodlightContext cntx,
			OFPacketIn packetIn, Logger logger)throws NullPointerException{
		
		

		OFPort inPort = OFMessageUtils.getInPort(packetIn); //Ge the port from which the packet came form
        Match m = createMatchFromPacket(ofSwitch, inPort, packetIn, cntx); // From PACKET_IN create a match for which we will write actions.
        List<OFAction> actions = new ArrayList<OFAction>(); // set no action to drop, empty list means drop action.
        


        logger.info("Setting actions");
        OFFlowAdd flowAdd = ofSwitch.getOFFactory().buildFlowAdd() // Get the OFFactory of the switch.
        	    .setBufferId(OFBufferId.NO_BUFFER)
        	    .setHardTimeout(3600)// decides the time for which this entry should remain in the switch even if there is matching traffic arriving.
        	    .setIdleTimeout(10)// This decides how long a flow in the switch should last if it doesn't match any traffic.
        	    .setPriority(32768)
        	    .setMatch(m) // set the match based on PACKET_IN
        	    .setTableId(TableId.of(1))
        	    .setActions(actions)//setting the drop action
        	    .build();

        logger.info("Dropping");
        ofSwitch.write(flowAdd); // Write the flowadd created above back to the switch.
	}

}