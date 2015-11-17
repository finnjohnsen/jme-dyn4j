package jmedyn4j

import java.util.Map
import java.util.concurrent.ConcurrentLinkedQueue;

import net.engio.mbassy.listener.Handler;
import static CorrectionPlayerControllerTest.EventBus
class ServerMovementSimulator {
	
	private final static ConcurrentLinkedQueue serverBacklog = new ConcurrentLinkedQueue();
	
	void update() {
		if (serverBacklog.size() > 0) {
			Map action = serverBacklog.poll()
			if (["Join", "Jump", "Right", "StopRight", "Left", "StopLeft"].contains(action.action)) {
				new Timer().schedule({
					EventBus.publishAsync([actionType:"serverMovement", action:action.action, time:action.time, trvl:action.trvl, cnt:action.cnt])
				} as TimerTask, 400)

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
