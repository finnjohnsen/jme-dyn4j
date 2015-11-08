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

class BasicTest extends SimpleApplication {
	
	static final Float Z_THICKNESS=0.1f
	/* 
byzanz-record --duration=1 --x=2080 --y=560 --width=320 --height=350 test.gif
	 */
	public static void main(String... args) {
		BasicTest main = new BasicTest()
		//main.setDisplayFps(false)
		main.setDisplayStatView(false)
		main.setShowSettings(false)
		AppSettings settings = new AppSettings(true)
		settings.setResolution(300, 300)
		settings.setVSync(true)
		main.setSettings(settings)
		main.start()
	}
	
	Dyn4JAppState dyn4JAppState
	
	
	@Override
	public void simpleInitApp() {
		
		inputManager.setCursorVisible(true)
		//flyCam.setEnabled(false);
		flyCam.setMoveSpeed(15f)
		
		dyn4JAppState = new Dyn4JAppState()
		stateManager.attach(dyn4JAppState)
		
		createWorld(dyn4JAppState)
		cam.setLocation(new Vector3f(0f, 0f, 35f));
	}

	void createWorld(Dyn4JAppState dyn4JAppState) {
		createFloor(dyn4JAppState);
		(1..10).each {
			createBox(new Vector2f(new Float((Math.random()*15)-7), new Float(Math.random()*15)), dyn4JAppState)
			createCircle(new Vector2f(new Float( (Math.random()*15)-7 ), new Float(Math.random()*15)), dyn4JAppState)
			//createCylynder(new Vector2f(new Float( (Math.random()*15)-7 ), new Float(Math.random()*15)), dyn4JAppState)
		}
	}
	
	private createCylynder(Vector2f location, Dyn4JAppState dyn4JAppState) {
		
		Cylinder c = new Cylinder(20, 50, 1f, 2f, true, true)
		
		Geometry cylGeom = new Geometry("Cylinder", c)
		
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Cyan)
		cylGeom.setMaterial(mat)
		
		cylGeom.setLocalTranslation(location.x, location.y, 0f)
		
		Quaternion roll = new Quaternion()
		roll.fromAngleAxis(new Float(FastMath.PI/4), Vector3f.UNIT_Z );
		cylGeom.setLocalRotation(roll)
		
		rootNode.attachChild(cylGeom)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Capsule(1f, 2f), MassType.NORMAL)
		cylGeom.addControl(physics)
		dyn4JAppState.add(cylGeom)
	}
	
	private createCircle(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Double radius = 0.5
		Sphere b = new Sphere(5, 30, new Float(radius))
		Geometry boxGeom = new Geometry("Sphere", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Blue)

		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		rootNode.attachChild(boxGeom)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Circle(radius), MassType.NORMAL)
		physics.setRestitution(0.1f)
		physics.setDesity(8)
		boxGeom.addControl(physics)
		dyn4JAppState.add(boxGeom)
	}
	

	private createFloor(Dyn4JAppState dyn4JAppState) {
		Double width = 150
		Double thickness = 0.3
		Box b = new Box(new Float(width), new Float(thickness), Z_THICKNESS)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Green)
		floorGeom.setMaterial(mat)
		floorGeom.setLocalTranslation(0f, -8f, 0f)
		rootNode.attachChild(floorGeom)
		
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Rectangle(width*2, thickness*2), MassType.INFINITE)
		physics.setRestitution(0.7f)
		physics.setDesity(5)
		floorGeom.addControl(physics)

		dyn4JAppState.add(floorGeom)
	}

	private createBox(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Double boxSize = 0.5f;
		Box b = new Box(new Float(boxSize), new Float(boxSize), Z_THICKNESS)
		Geometry boxGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Brown)

		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		rootNode.attachChild(boxGeom)

		boxGeom.addControl(new Dyn4JShapeControl(new Rectangle(new Float(boxSize*2), new Float(boxSize*2)), MassType.NORMAL))
		dyn4JAppState.add(boxGeom)
	}
}