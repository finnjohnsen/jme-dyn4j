package jmedyn4j

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Capsule
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Transform

import com.jme3.app.SimpleApplication
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
import com.jme3.scene.*;

class BasicTest extends SimpleApplication {
	
	static final Float Z_THICKNESS=0.5f
	
	public static void main(String... args) {
		BasicTest main = new BasicTest()
		main.setDisplayFps(false)
		main.setDisplayStatView(false)
		main.setShowSettings(false)
		AppSettings settings = new AppSettings(true)
		settings.setResolution(400, 400)
		settings.setVSync(true)
		settings.setTitle("JME-DYN4J-TEST")
		main.setSettings(settings)
		main.start()
	}
	
	Dyn4JAppState dyn4JAppState
	
	
	@Override
	public void simpleInitApp() {
		viewPort.setBackgroundColor(new ColorRGBA(new Float(135/255f),new Float(206/255f),new Float(250/255f), 1f))
		inputManager.setCursorVisible(true)
		flyCam.setEnabled(false);
		flyCam.setMoveSpeed(15f)
		
		dyn4JAppState = new Dyn4JAppState()
		stateManager.attach(dyn4JAppState)
		
		createWorld(dyn4JAppState)
		cam.setLocation(new Vector3f(0f, 0f, 35f));
		
		
		new Timer().schedule({
			/*
			 byzanz-record --duration=6 --x=0 --y=30 --width=320 --height=340 /home/finn/src/jme-dyn4j/etc/jme-dyn4j.gif
				  */
			"sh /home/finn/src/jme-dyn4j/etc/mv-srv.sh".execute()
		} as TimerTask, 100)
	}

	void createWorld(Dyn4JAppState dyn4JAppState) {
		createFloor(dyn4JAppState);
		createRightWall(dyn4JAppState)
		createLeftWall(dyn4JAppState)
		(1..20).each {
			createBox(new Vector2f(new Float((Math.random()*15)-7), new Float(Math.random()*25)), dyn4JAppState)
			createCircle(new Vector2f(new Float( (Math.random()*15)-7 ), new Float(Math.random()*25)), dyn4JAppState)
			createCylynder(new Vector2f(new Float( (Math.random()*15)-7 ), new Float(Math.random()*25)), dyn4JAppState)
		}
	}
	
	private createCylynder(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Gray)
		
		Geometry cylGeom = new Geometry("Cylinder", new Cylinder(20, 50, 0.3f, 2f))
		cylGeom.setMaterial(mat)
		Quaternion roll = new Quaternion()
		roll.fromAngleAxis(new Float(FastMath.PI/2), Vector3f.UNIT_X );
		cylGeom.setLocalRotation(roll)
		
		Node capsuleNode = new Node()
		capsuleNode.attachChild(cylGeom)
		capsuleNode.setLocalTranslation(location.x, location.y, 0f)
		
		rootNode.attachChild(capsuleNode)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Capsule(0.3, 2), MassType.NORMAL)
		physics.setRestitution(0.7f)
		physics.setDesity(4)
		//physics.setDesity(1000)
		capsuleNode.addControl(physics)
		dyn4JAppState.add(capsuleNode)
	}
	
	private createBox(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Double boxSize = 0.7f;
		Box b = new Box(new Float(boxSize), new Float(boxSize), Z_THICKNESS)
		Geometry boxGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Brown)
		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		rootNode.attachChild(boxGeom)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Rectangle(new Float(boxSize*2), new Float(boxSize*2)), MassType.NORMAL)
		boxGeom.addControl(physics)
		dyn4JAppState.add(boxGeom)
	}
	
	private createCircle(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Double radius = 0.5
		Sphere b = new Sphere(5, 30, new Float(radius))
		Geometry boxGeom = new Geometry("Sphere", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Orange)
		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		rootNode.attachChild(boxGeom)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Circle(radius), MassType.NORMAL)
		boxGeom.addControl(physics)
		dyn4JAppState.add(boxGeom)
	}
	

	Double cageSize = 10
	
	private createFloor(Dyn4JAppState dyn4JAppState) {
		Double width = cageSize
		Double thickness = 0.3
		Box b = new Box(new Float(width), new Float(thickness), Z_THICKNESS)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Green)
		floorGeom.setMaterial(mat)
		floorGeom.setLocalTranslation(0f, -8f, 0f)
		rootNode.attachChild(floorGeom)
		
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Rectangle(width*2, thickness*2), MassType.INFINITE)
		floorGeom.addControl(physics)

		dyn4JAppState.add(floorGeom)
	}
	
	
	private createRightWall(Dyn4JAppState dyn4JAppState) {
		Double width = cageSize
		Double thickness = 0.3
		Box b = new Box(new Float(thickness), new Float(width), Z_THICKNESS)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Green)
		floorGeom.setMaterial(mat)
		floorGeom.setLocalTranslation(new Float(width), 2f, 0f)
		rootNode.attachChild(floorGeom)
		
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Rectangle(thickness*2, width*2), MassType.INFINITE)
		floorGeom.addControl(physics)

		dyn4JAppState.add(floorGeom)
	}
	
	private createLeftWall(Dyn4JAppState dyn4JAppState) {
		Double width = cageSize
		Double thickness = 0.3
		Box b = new Box(new Float(thickness), new Float(width), Z_THICKNESS)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Green)
		floorGeom.setMaterial(mat)
		floorGeom.setLocalTranslation(new Float(-width), 2f, 0f)
		rootNode.attachChild(floorGeom)
		
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Rectangle(thickness*2, width*2), MassType.INFINITE)
		floorGeom.addControl(physics)

		dyn4JAppState.add(floorGeom)
	}
	
	


}