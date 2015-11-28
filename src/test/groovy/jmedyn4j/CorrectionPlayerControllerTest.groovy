package jmedyn4j
import java.util.concurrent.ConcurrentLinkedQueue
import org.dyn4j.collision.Fixture
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Capsule
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Transform

import com.jme3.app.SimpleApplication
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.AnalogListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Cylinder
import com.jme3.scene.shape.Sphere
import com.jme3.system.AppSettings
import com.jme3.scene.*
import static jmedyn4j.EventBusSingletonHolder.*

class CorrectionPlayerControllerTest extends SimpleApplication {
	static final Float Z_THICKNESS=0.5f
	
	
	/* 
	 byzanz-record --duration=5 --x=2080 --y=560 --width=320 --height=350 test.gif
	 */
	public static void main(String... args) {
		CorrectionPlayerControllerTest main = new CorrectionPlayerControllerTest()
		//main.setDisplayFps(false)
		main.setDisplayStatView(false)
		main.setShowSettings(false)
		AppSettings settings = new AppSettings(true)
		settings.setResolution(500, 500)
		settings.setVSync(true)
		settings.setTitle("JME-DYN4J-TEST")
		main.setSettings(settings)
		println "starting"
		main.start()


	}
	
	@Override
	public void destroy() {
		super.destroy();
		System.exit(0)
	}
	
	Dyn4JAppState realtimeDyn4JAppState

	@Override
	public void update() {
		super.update();
		if (sim != null) sim.update()
	}

	ServerMovementSimulator sim
	@Override
	public void simpleInitApp() {
		
		sim = new ServerMovementSimulator()
		EventBus.subscribe(sim)
		
		viewPort.setBackgroundColor(new ColorRGBA(new Float(135/255f),new Float(206/255f),new Float(250/255f), 1f))

		inputManager.setCursorVisible(true)
		flyCam.setEnabled(false);
		flyCam.setMoveSpeed(15f)

		
		Node realtimeWorldNode = new Node("realtimeWorldNode")
		rootNode.attachChild(realtimeWorldNode)
		realtimeDyn4JAppState = new Dyn4JAppState()
		realtimeDyn4JAppState.broadcastPhysicsTicks=true
		stateManager.attach(realtimeDyn4JAppState)
		RealtimePlayerInWorldAppState realtimePlayerAppState = new RealtimePlayerInWorldAppState(realtimeDyn4JAppState:realtimeDyn4JAppState, worldNode:realtimeWorldNode, assetManager:assetManager)
		EventBus.subscribe(realtimePlayerAppState)
		stateManager.attach(realtimePlayerAppState)
		createWorld(realtimeDyn4JAppState, realtimeWorldNode)
		
		
		Node correctionWorldNode = new Node("correctionWorldNode")
		correctionWorldNode.setLocalTranslation(0f, 0f, 0f)
		rootNode.attachChild(correctionWorldNode)
		Dyn4JAppState correctionDyn4JAppState = new Dyn4JAppState()
		//stateManager.attach(correctionDyn4JAppState)
		CorrectionWorldAppState correctionAppState = new CorrectionWorldAppState(correctionDyn4JAppState:correctionDyn4JAppState, worldNode:correctionWorldNode, assetManager:assetManager)
		EventBus.subscribe(correctionAppState)
		stateManager.attach(correctionAppState)
		createWorld(correctionDyn4JAppState, correctionWorldNode)
		
		
		
		cam.setLocation(new Vector3f(0f, 0f, 35f));
		initKeys()
		//initPlayer(new Vector2f(0f, 0f), dyn4JAppState)

		new Timer().schedule({
			/*
			 sleep 2 & byzanz-record --duration=6 --x=0 --y=60 --width=320 --height=325 /home/finn/src/jme-dyn4j/etc/PlayerControl.gif
			 */
			//"sh /home/finn/src/jme-dyn4j/etc/mv-srv.sh".execute()
		} as TimerTask, 500)
	}
	

	void initKeys() {
		inputManager.addMapping("Join",  new KeyTrigger(KeyInput.KEY_P));
		inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(actionListener,"Left", "Right", "Jump", "Join");
		
	}
	
	
	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if (name.equals("Right")) {
				if (keyPressed) EventBus.publishAsync([actionType:"localMovement", action:"Right"])// playerControl.moveRight()
				else EventBus.publishAsync([actionType:"localMovement", action:"StopRight"])//playerControl.stopMoveRight()
			} else if (name.equals("Left")) {
				if (keyPressed) EventBus.publishAsync([actionType:"localMovement", action:"Left"])//playerControl.moveLeft()
				else EventBus.publishAsync([actionType:"localMovement", action:"StopLeft"])//playerControl.stopMoveLeft()
			} else if (name.equals("Jump")) {
				 EventBus.publishAsync([actionType:"localMovement", action:"Jump"])//playerControl.jump()
			} else if (name.equals("Join")) {
				EventBus.publishAsync([actionType:"localMovement", action:"Join"])
			}
		}
	};

	void createWorld(Dyn4JAppState dyn4JAppState, Node worldNode) {
		createFloor(dyn4JAppState, worldNode);
		//(1..40).each {
		//	createBox(new Vector2f(new Float((Math.random()*15)-7), new Float(-8)), dyn4JAppState, worldNode)
		//}
	}



	Double cageSize = 10

	private createFloor(Dyn4JAppState dyn4JAppState, Node worldNode) {
		Double width = cageSize
		Double thickness = 0.3
		Box b = new Box(new Float(width), new Float(thickness), Z_THICKNESS)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.getAdditionalRenderState().setWireframe(true)
		mat.setColor("Color", new ColorRGBA(new Float(160/255f),new Float(82/255f),new Float(45/255f), 1f))
		floorGeom.setMaterial(mat)
		floorGeom.setLocalTranslation(0f, -8f, 0f)
		worldNode.attachChild(floorGeom)

		floorGeom.addControl(new Dyn4JShapeControl(new Rectangle(width*2, thickness*2), MassType.INFINITE, 0, 1))
		dyn4JAppState.add(floorGeom)
	}

	private createBox(Vector2f location, Dyn4JAppState dyn4JAppState, Node worldNode) {
		Double boxSize = 0.5f;
		Box b = new Box(new Float(boxSize), new Float(boxSize), Z_THICKNESS)
		Geometry boxGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", new ColorRGBA(new Float(255/255f),new Float(228/255f),new Float(225/255f), 1f))

		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		worldNode.attachChild(boxGeom)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Rectangle(new Float(boxSize*2), new Float(boxSize*2)), MassType.NORMAL, 100, 0.8, 0)
		boxGeom.addControl(physics)
		dyn4JAppState.add(boxGeom)
	}
}


