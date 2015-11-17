package jmedyn4j

import java.util.Map
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager
import com.jme3.math.Vector2f
import com.jme3.scene.Node

import net.engio.mbassy.listener.Handler;

class CorrectionWorldAppState extends AbstractAppState {
	private final static ConcurrentLinkedQueue clientSideActionBacklog = new ConcurrentLinkedQueue();
	private final static ConcurrentLinkedQueue serverBacklog = new ConcurrentLinkedQueue();
	AssetManager assetManager
	Dyn4JAppState correctionDyn4JAppState
	Node worldNode
	Dyn4JPlayerControl playerControl
	
	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager);
		assert correctionDyn4JAppState
		assert worldNode
		assert assetManager
	}
	
	private static final Boolean processLastServerMessage=true
	
	Date lastTime = new Date()
	@Override
	public void update(float tpf) {
		super.update(tpf)
		if (!clientSideActionBacklog.peek()) {
			 return
		}
		
		Boolean didCorrection = false
		Map serverBacklogAction = serverBacklog.poll()
		
		if (processLastServerMessage){
			if (serverBacklog.size() > 1) println "skipping to last server message, discarding ${serverBacklog.size()} messages"
			while(serverBacklog.peek() != null) {
				serverBacklogAction = serverBacklog.poll()
			}
		}
		
		while(serverBacklogAction != null) {
			didCorrection = true
			//println "processing server backlog"
			if (serverBacklogAction.action == "Join") {
				playerControl = RealtimePlayerInWorldAppState.initPlayer(new Vector2f(0f, 0f), correctionDyn4JAppState, worldNode, assetManager)
				//playerControl.debugging=true
				Long diff = serverBacklogAction.time.getTime() - lastTime.getTime()
				Float updateTPF = new Float(diff/1000f)
				//println "login gets some update time $updateTPF"
				correctionDyn4JAppState.update(updateTPF)
			} else if (["Jump", "Right", "StopRight", "Left", "StopLeft"].contains(serverBacklogAction.action)) {
				Map clientAction = clientSideActionBacklog.poll()
				while (clientAction != null) { // move to pre-correction
					if (clientAction.cnt < serverBacklogAction.cnt) {
						playerControl.setTrlv(clientAction.trvl)
						playerControl.doMove(clientAction.action)
						Long diff = clientAction.time.getTime() - lastTime.getTime()
						Float updateTPF = new Float(diff/1000f)
						//println "local pre-correction step $updateTPF"
						correctionDyn4JAppState.update(updateTPF)
					} else if (clientAction.cnt == serverBacklogAction.cnt) { //correction
						Long diff = clientAction.time.getTime() - lastTime.getTime()
						Float updateTPF = new Float(diff/1000f)
						//println "exact server correction step $updateTPF"
						correctionDyn4JAppState.update(updateTPF)
						lastTime=clientAction.time
						playerControl.setTrlv(serverBacklogAction.trvl)
						playerControl.doMove(serverBacklogAction.action)
					} else if (clientAction.cnt > serverBacklogAction.cnt) {
						Long diff = clientAction.time.getTime() - lastTime.getTime()
						Float updateTPF = new Float(diff/1000f)
						//println "simulating fast-forward serverBacklogAction, by $updateTPF"
						correctionDyn4JAppState.update(updateTPF)
						lastTime=clientAction.time
					
						playerControl.setTrlv(clientAction.trvl )
						playerControl.doMove(clientAction.action)
						//println "local post-correction step $updateTPF"
						//println "fast-forwarding to ${clientAction.cnt}"
					}
					clientAction = clientSideActionBacklog.poll()
				}
			} 
			serverBacklogAction = serverBacklog.poll()
		}
		
		if (didCorrection) { // final local stretch to try find exact now.
			Date now = new Date()
			Long diff = now.getTime() - lastTime.getTime()
			Float updateTPF = new Float(diff/1000f)
			println "catchup to now by $updateTPF"
			correctionDyn4JAppState.update(updateTPF)
			lastTime=now
		}
	}
	
	@Handler
	void handleAction(Map action) {
		if (action.actionType == "executedLocalMovement") clientSideActionBacklog.add(action)
		else if (action.actionType == "serverMovement") serverBacklog.add action
		
	}
}
