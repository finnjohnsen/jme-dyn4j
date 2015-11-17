package jmedyn4j

import java.util.concurrent.ConcurrentLinkedQueue

import net.engio.mbassy.listener.Handler

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Cylinder
import com.jme3.scene.*
import static CorrectionPlayerControllerTest.EventBus

class RealtimePlayerInWorldAppState extends AbstractAppState {
	private final static ConcurrentLinkedQueue actionQueue = new ConcurrentLinkedQueue();
	AssetManager assetManager
	Dyn4JAppState realtimeDyn4JAppState
	Dyn4JPlayerControl playerControl
	
	Node worldNode
	
	private Boolean joined = false
	
	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager);
		assert realtimeDyn4JAppState
		assert worldNode
		assert assetManager
	}


	Long executedLocalMovementCounter = 0
	@Override
	public void update(float tpf) {
		super.update(tpf)
		//println "$tpf"
		
		Map action = actionQueue.poll()
		if (action != null) {
			if (joined) {
				if (action.action=="Right") {
					playerControl.moveRight()
					EventBus.publishAsync([actionType:"executedLocalMovement", action:action.action, time:new Date(), trvl:playerControl.getTrlv(), cnt:++executedLocalMovementCounter])
				} else if (action.action=="StopRight") {
					playerControl.stopMoveRight()
					EventBus.publishAsync([actionType:"executedLocalMovement", action:action.action, time:new Date(), trvl:playerControl.getTrlv(), cnt:++executedLocalMovementCounter])
				} else if (action.action=="Left") {
					playerControl.moveLeft()
					EventBus.publishAsync([actionType:"executedLocalMovement", action:action.action, time:new Date(), trvl:playerControl.getTrlv(), cnt:++executedLocalMovementCounter])
				} else if (action.action=="StopLeft") {
					playerControl.stopMoveLeft()
					EventBus.publishAsync([actionType:"executedLocalMovement", action:action.action, time:new Date(), trvl:playerControl.getTrlv(), cnt:++executedLocalMovementCounter])
				} else if (action.action=="Jump") {
					if (playerControl.canJump()) {
						playerControl.jump()
						EventBus.publishAsync([actionType:"executedLocalMovement", action:action.action, time:new Date(), trvl:playerControl.getTrlv(), cnt:++executedLocalMovementCounter])
					}
				}
			} else {
				if (action.action == "Join") {
					if (joined==true){
						 println "already joined"
					} else {
						println "joining"
						joined=true
						playerControl=initPlayer(new Vector2f(0f, 0f), realtimeDyn4JAppState, worldNode, assetManager)
						
						EventBus.publishAsync([actionType:"executedLocalMovement", action:action.action, time:new Date(), trvl:playerControl.getTrlv(), cnt:++executedLocalMovementCounter])
					}
				}
			}
		}
	}
	
	
	private static Dyn4JPlayerControl initPlayer(Vector2f location, Dyn4JAppState dyn4JAppState, Node worldNode, AssetManager assetManager) {
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.getAdditionalRenderState().setWireframe(true);
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", ColorRGBA.Red)
		
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
		return playerControl
	}
	
	@Handler
	void handleAction(Map action) {
		if (action.actionType != "localMovement") return;
		actionQueue.add(action)
	}

}
