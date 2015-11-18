package jmedyn4j

import java.util.Map
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager
import com.jme3.math.Vector2f
import com.jme3.scene.Node

import net.engio.mbassy.listener.Handler;
import static CorrectionPlayerControllerTest.EventBus


class CorrectionWorldAppState extends AbstractAppState {
	private final static ConcurrentLinkedQueue clientSideActionBacklog = new ConcurrentLinkedQueue();
	private final static ConcurrentLinkedQueue serverBacklog = new ConcurrentLinkedQueue();
	AssetManager assetManager
	Dyn4JAppState correctionDyn4JAppState
	Node worldNode
	Dyn4JPlayerControl correctionPlayer
	
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
				/*Long diff = serverBacklogAction.time.getTime() - lastTime.getTime()
				Float updateTPF = new Float(diff/1000f)*/
				lastTime = new Date()
				correctionPlayer = RealtimePlayerInWorldAppState.initPlayer(new Vector2f(0f, 0f), correctionDyn4JAppState, worldNode, assetManager)
				correctionDyn4JAppState.update(0)
			} else if (["Jump", "Right", "StopRight", "Left", "StopLeft"].contains(serverBacklogAction.action)) {
				Map clientAction = clientSideActionBacklog.poll()
				while (clientAction != null) { // move to pre-correction
					if (clientAction.cnt < serverBacklogAction.cnt) {
						//println "pre"
						Long diff = clientAction.time.getTime() - lastTime.getTime()
						Float updateTPF = new Float(diff/1000f)
						correctionDyn4JAppState.update(updateTPF)
						lastTime=clientAction.time
						correctionPlayer.setTrvl(clientAction.trvl)
						correctionPlayer.doMove(clientAction.action)
					} else if (clientAction.cnt == serverBacklogAction.cnt) { //correction
						//println "corr from ${serverBacklogAction.cnt}"
						
						Long diff = clientAction.time.getTime() - lastTime.getTime()
						Float updateTPF = new Float(diff/1000f)
						correctionDyn4JAppState.update(updateTPF)
						lastTime=clientAction.time
						correctionPlayer.setTrvl(serverBacklogAction.trvl)
						correctionPlayer.doMove(serverBacklogAction.action)
					} else if (clientAction.cnt > serverBacklogAction.cnt) {
						//println "post ${clientAction.cnt}"
						Long diff = clientAction.time.getTime() - lastTime.getTime()
						Float updateTPF = new Float(diff/1000f)
						correctionDyn4JAppState.update(updateTPF)
						lastTime=clientAction.time
						correctionPlayer.setTrvl(clientAction.trvl )
						correctionPlayer.doMove(clientAction.action)
					}
					clientAction = clientSideActionBacklog.poll()
				}
			} 
			serverBacklogAction = serverBacklog.poll()
		}
		
		if (didCorrection) { // final local stretch with last command given, to try match the exact presence.
			Date now = new Date()
			Long diff = now.getTime() - lastTime.getTime()
			Float updateTPF = new Float((diff/1000f))
			//println "catchup to now by $updateTPF"
			correctionDyn4JAppState.update(updateTPF)
			lastTime=now
			EventBus.publish([actionType:"correct", trvl:correctionPlayer.getTrlv()])
		}
	}
	
	@Handler
	void handleAction(Map action) {
		if (action.actionType == "executedLocalMovement") clientSideActionBacklog.add(action)
		else if (action.actionType == "serverMovement") serverBacklog.add action
		
	}
}
