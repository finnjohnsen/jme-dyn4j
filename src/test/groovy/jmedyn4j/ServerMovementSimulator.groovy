package jmedyn4j

import static jmedyn4j.EventBusSingletonHolder.*

import java.util.concurrent.ConcurrentLinkedQueue

import net.engio.mbassy.listener.Handler
import java.lang.Math

class ServerMovementSimulator {
	
	private final static ConcurrentLinkedQueue serverBacklogDelayed = new ConcurrentLinkedQueue();
	private final static ConcurrentLinkedQueue serverBacklog = new ConcurrentLinkedQueue();
	

	Double cnt = 0
	Double max = Double.MAX_VALUE
	
	ServerMovementSimulator() {
		new Timer().schedule({
			while (serverBacklogDelayed.peek() != null) {
				Map action =serverBacklogDelayed.poll()
				if (action.action == "Join") serverBacklog.add action
				else {
					if (serverBacklog.size() == 0) serverBacklog.add action
				}
			}
		} as TimerTask, 400, 800) //magic numbers are initial-delay & repeat-interval
	}

	
	void update() {
		Map action = serverBacklog.poll()
		while(action != null) {
			if (["Join", "Jump", "Right", "StopRight", "Left", "StopLeft"].contains(action.action)) {
				EventBus.publishAsync([actionType:"serverMovement", action:action.action, time:action.time, trlv:action.trlv, cnt:action.cnt])
			}
			action = serverBacklog.poll()
		}
	}
	
	Date lastLocal=new Date();
	
	@Handler
	void handleAction(Map action) {
		if (action.actionType == "executedLocalMovement") {
			//println "                                                                                                  ${action.cnt} 0.${action.time.getTime()-lastLocal.getTime()} tpf, then ${action.action} ${action.trlv}"	
			serverBacklogDelayed.add(action)
			lastLocal = action.time
		}
	}

}
