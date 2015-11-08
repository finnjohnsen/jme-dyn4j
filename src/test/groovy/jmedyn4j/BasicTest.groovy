package jmedyn4j

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Transform

import com.jme3.app.SimpleApplication
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
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
		cam.setLocation(new Vector3f(0f, 0f, 25f));
	}

	void createWorld(Dyn4JAppState dyn4JAppState) {
		createFloor(dyn4JAppState);
		createBox(new Vector2f(0.6f, 2f), dyn4JAppState)
		createBox(new Vector2f(0f, 4f), dyn4JAppState)
		createBox(new Vector2f(0.3f, 6f), dyn4JAppState)
		createBox(new Vector2f(0.2f, 8f), dyn4JAppState)
		createCircle(new Vector2f(1.1f, 8f), dyn4JAppState)
	}
	
	private createCircle(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Double radius = 1
		Sphere b = new Sphere(5, 30, new Float(radius))
		Geometry boxGeom = new Geometry("Sphere", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Blue)

		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		rootNode.attachChild(boxGeom)
		Dyn4JShapeControl physics = new Dyn4JShapeControl(new Circle(radius), MassType.NORMAL)
		physics.setRestitution(0.5f)
		boxGeom.addControl(physics)
		dyn4JAppState.add(boxGeom)
	}
	

	private createFloor(Dyn4JAppState dyn4JAppState) {
		Double width = 150
		Double thickness = 0.1
		Box b = new Box(new Float(width), new Float(thickness), Z_THICKNESS)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Green)
		floorGeom.setMaterial(mat)
		floorGeom.setLocalTranslation(0f, -8f, 0f)
		rootNode.attachChild(floorGeom)
		floorGeom.addControl(new Dyn4JShapeControl(new Rectangle(width*2, thickness*2), MassType.INFINITE))

		dyn4JAppState.add(floorGeom)
	}

	private createBox(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Box b = new Box(0.5f, 0.5f, Z_THICKNESS)
		Geometry boxGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Blue)

		boxGeom.setLocalTranslation(location.x, location.y, 0f)
		boxGeom.setMaterial(mat)
		rootNode.attachChild(boxGeom)

		boxGeom.addControl(new Dyn4JShapeControl(new Rectangle(1.0, 1.0), MassType.NORMAL))
		dyn4JAppState.add(boxGeom)
	}
}