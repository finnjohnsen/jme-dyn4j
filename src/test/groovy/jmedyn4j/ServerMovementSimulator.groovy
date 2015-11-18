package jmedyn4j

import static jmedyn4j.CorrectionPlayerControllerTest.EventBus

import java.util.concurrent.ConcurrentLinkedQueue

import net.engio.mbassy.listener.Handler
import java.lang.Math

class ServerMovementSimulator {
	
	private final static ConcurrentLinkedQueue serverBacklog = new ConcurrentLinkedQueue();
	
	Date lastSent = new Date()

	Double cnt = 0
	Double max = Double.MAX_VALUE
	void update() {
		if (serverBacklog.size() > 0 && 
			(new Date().getTime() - lastSent.getTime() > 300)) {
			if (cnt++ >= max) return;
			lastSent=new Date()
			Map action = serverBacklog.poll()
			if (["Join", "Jump", "Right", "StopRight", "Left", "StopLeft"].contains(action.action)) {
				EventBus.publishAsync([actionType:"serverMovement", action:action.action, time:action.time, trvl:action.trvl, cnt:action.cnt])
			}
		}
	}
	
	@Handler
	void handleAction(Map action) {
		if (action.actionType == "executedLocalMovement") {
			serverBacklog.add(action)
		}
	}

}
