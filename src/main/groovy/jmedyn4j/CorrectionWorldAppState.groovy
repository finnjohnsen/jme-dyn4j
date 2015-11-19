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
	private final static ConcurrentLinkedQueue serverActionBacklog = new ConcurrentLinkedQueue();
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
	
	
	Date lastTime = new Date()
	@Override
	public void update(float tpf) {
		super.update(tpf)
		Map serverAction
		synchronized(this) {
			if (serverActionBacklog.peek()?.action == "Join") {
				Map joinAction = serverActionBacklog.poll()
				latestProcessedAction = joinAction.cnt
				lastTime = new Date()
				correctionPlayer = RealtimePlayerInWorldAppState.initPlayer(new Vector2f(0f, 0f), correctionDyn4JAppState, worldNode, assetManager)
				correctionDyn4JAppState.update(0)
				return;
			}
			
			if (serverActionBacklog.peek() == null || clientSideActionBacklog.peek() == null) return
			while(clientSideActionBacklog.peek() != null) {
				Map clientAction = clientSideActionBacklog.peek()
				if (clientAction.cnt < serverActionBacklog.peek().cnt) { 
					clientSideActionBacklog.poll()
				} else break;
			}
			if (clientSideActionBacklog.peek() == null) return
			serverAction = serverActionBacklog.poll()
		}
		
		Long correctionCnt
		if (["Jump", "Right", "StopRight", "Left", "StopLeft"].contains(serverAction.action)) {
			correctionCnt = processActions(serverAction)
		} 

		if (correctionCnt!=null) {
			Date now = new Date()
			Float updateTPF = new Float((now.getTime() - lastTime.getTime())/1000f)
			correctionDyn4JAppState.update(updateTPF)
			lastTime = now
			Map trlv = correctionPlayer.getTrlv()
			EventBus.publish([actionType:"correct", trlv:trlv])
		}
	}

	//Long correctionBatch = 0
	private Long processActions(Map serverAction) {
		Long correctionCnt = null
		Map clientAction = clientSideActionBacklog.poll()
		Map previousClientAction
		while (clientAction != null) {
			latestProcessedAction = clientAction.cnt
			if (clientAction.cnt < serverAction.cnt) { // do nothing with these
			} else if (clientAction.cnt == serverAction.cnt) { //the actual correction
				lastTime = clientAction.time
				correctionPlayer.setMove(clientAction.action)
				correctionPlayer.setTrlv(serverAction.trlv)
				//correctionDyn4JAppState.update(0)
				correctionCnt=clientAction.cnt
			} else if (clientAction.cnt > serverAction.cnt) { //after the actual correction
				Float tpf = new Float((clientAction.time.getTime() - lastTime.getTime())/1000)
				correctionDyn4JAppState.update(tpf)
				lastTime = clientAction.time
				correctionPlayer.setMove(clientAction.action)
				correctionPlayer.setTrlv(clientAction.trlv)
				correctionCnt=clientAction.cnt
			}
			previousClientAction = clientAction
			clientAction = clientSideActionBacklog.poll()
		}
		return correctionCnt;
	}
	
	private Long latestProcessedAction = 0
	
	@Handler
	void handleAction(Map action) {
		if (action.actionType == "executedLocalMovement") {
			clientSideActionBacklog.add(action)
		} else if (action.actionType == "serverMovement")  {
			synchronized(this) {
				if (action.cnt <= latestProcessedAction) {
					//println "${new Date()}. discarding server message ${action.cnt}"
				} else {
					serverActionBacklog.add action
				}
			}
		}
	}
}
