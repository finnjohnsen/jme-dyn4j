package jmedyn4j

import static jmedyn4j.EventBusSingletonHolder.*

import java.util.concurrent.ConcurrentLinkedQueue

import net.engio.mbassy.listener.Handler

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Cylinder


class CorrectionWorldAppState extends AbstractAppState {
	private final static ConcurrentLinkedQueue clientSideActionBacklog = new ConcurrentLinkedQueue();
	private final static ConcurrentLinkedQueue serverActionBacklog = new ConcurrentLinkedQueue();
	AssetManager assetManager
	Dyn4JAppState correctionDyn4JAppState
	Node worldNode
	private Dyn4JPlayerControl correctionPlayer
	
	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager);
		assert correctionDyn4JAppState
		assert worldNode
		assert assetManager
	}
	
	Date lastCorrectionTime
	@Override
	public void update(float tpf) {
		super.update(tpf)
		Map serverAction
		synchronized(this) {
			if (serverActionBacklog.peek()?.action == "Join") {
				Map joinAction = serverActionBacklog.poll()
				latestProcessedAction = joinAction.cnt
				correctionPlayer = initPlayer(new Vector2f(0f, 0f), correctionDyn4JAppState, worldNode, assetManager)
				correctionPlayer.debugging=true
				//correctionDyn4JAppState.update(0)
				return;
			}
			
			if (serverActionBacklog.peek() == null || clientSideActionBacklog.peek() == null) return
			while(clientSideActionBacklog.peek() != null) {
				Map clientAction = clientSideActionBacklog.peek()
				if (clientAction.cnt < serverActionBacklog.peek().cnt) { 
					clientSideActionBacklog.poll()
				} else break;
			}
			if (clientSideActionBacklog.peek() == null || clientSideActionBacklog.size() < 2) return
			serverAction = serverActionBacklog.poll()
		}
		
		Map lastCorrection
		println ""
		println "*** Correcting ***"
		Long start = new Date().getTime()
		if (["Jump", "Right", "StopRight", "Left", "StopLeft"].contains(serverAction.action)) {
			lastCorrection = processActions(serverAction)
		} 

		if (lastCorrection!=null) {
			
			Double timeDiffMillis = new Date().getTime() - lastCorrection.time.getTime()
			int ticks = Math.round(timeDiffMillis/(tpf*1000))
			println "trying to get to now, ticks: $ticks, move: ${correctionPlayer.getMove()}"
			if (ticks>1) {
				(1..ticks-1).each {
					correctionDyn4JAppState.update(tpf)
				}
				correctionDyn4JAppState.update(tpf)
			} else correctionDyn4JAppState.update(tpf)
//			Float updateTPF = new Float((now.getTime() - lastTime.getTime())/1000f)
//			println "FF to now ${updateTPF}"
			//println "FF Complete ${correctionPlayer.getMove()} for $updateTPF, from ${correctionPlayer.getTrlv()}"
//			correctionDyn4JAppState.update(1/60)
//			Map trlv = correctionPlayer.getTrlv()
			//EventBus.publish([actionType:"correct", trlv:trlv])
		}
		println "*** Correcting Finished ***"
		println ""
	}
	
	private Long latestProcessedAction = 0
	private final Float tpf = 1/60;
	//Long correctionBatch = 0
	private Map processActions(Map serverAction) {
		correctionPlayer.setMove("StopLeft")
		correctionPlayer.setMove("StopRight")
		Map clientAction = clientSideActionBacklog.poll()
		Map previousClientAction
		while (clientAction != null) {
			latestProcessedAction = clientAction.cnt
			println "${clientAction.cnt}. ClientAction : $clientAction"
			if (clientAction.cnt < serverAction.cnt) { // dont do any stepping with these.
				//correctionPlayer.setMove(clientAction.action)
				//println "${clientAction.cnt}. PreCorr set to ${clientAction.action} "
			} else if (clientAction.cnt == serverAction.cnt) { //the actual correction
				correctionPlayer.setTrlv(serverAction.trlv)
				correctionPlayer.setMove(serverAction.action)
				println "${clientAction.cnt}. Correction set to ${serverAction.action}"
				previousClientAction = clientAction
			} else if (clientAction.cnt > serverAction.cnt) { //after the actual correction
				println "Running ${previousClientAction.physTick} ticks which is ${correctionPlayer.getMove()}"
				
				Double timeDiffMillis = clientAction.time.getTime() - previousClientAction.time.getTime()
				int ticks = Math.round(timeDiffMillis/(1/60*1000))
				(1..ticks).each {
					correctionDyn4JAppState.update(tpf)
				}

				println "${clientAction.cnt}. Post-Correction setting to ${clientAction.action}"
				correctionPlayer.setMove(clientAction.action)
				previousClientAction=clientAction
			}
			Map p = clientAction
			clientAction = clientSideActionBacklog.poll()
			if (clientAction == null) return p
		}
	}
	

	
	Map currAction
	@Handler
	void handleAction(Map action) {
		if (action.actionType == "executedLocalMovement") {
			currAction=action
			clientSideActionBacklog.add(action)
		} else if (action.actionType == "serverMovement")  {
			synchronized(this) {
				if (action.cnt <= latestProcessedAction) {
					//println "${new Date()}. discarding server message ${action.cnt}"
				} else {
					serverActionBacklog.add action
				}
			}
		} else if (action.actionType == "physTick") {
			if (currAction==null) return
			if (currAction.physTick) {
				currAction.physTick+=1
			} else currAction.physTick=1
		}
	}
	
	private Dyn4JPlayerControl initPlayer(Vector2f location, Dyn4JAppState dyn4JAppState, Node worldNode, AssetManager assetManager) {
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		
		mat.setColor("Color", ColorRGBA.Red)
		//mat.getAdditionalRenderState().setWireframe(true);
		
		Geometry cylGeom = new Geometry("Cylinder", new Cylinder(20, 50, 0.3f, 1.8f))
		cylGeom.setMaterial(mat)
		
		Quaternion roll = new Quaternion()
		roll.fromAngleAxis(new Float(FastMath.PI/2), Vector3f.UNIT_X );
		cylGeom.setLocalRotation(roll)
		
		Node capsuleNode = new Node()
		capsuleNode.attachChild(cylGeom)
		capsuleNode.setLocalTranslation(location.x, location.y, 0f)
		
		worldNode.attachChild(capsuleNode)
		Dyn4JPlayerControl playerControl = new Dyn4JPlayerControl(0.3, 1.80, 90)
		capsuleNode.addControl(playerControl)
		dyn4JAppState.add(capsuleNode)
		//println "created player control $playerControl"
		return playerControl
	}
	
}
