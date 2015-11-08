package jmedyn4j

import javax.vecmath.Vector2f;

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Transform

import com.jme3.app.SimpleApplication
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.system.AppSettings

class BasicTest extends SimpleApplication {
	
	public static void main(String... args) {
		BasicTest main = new BasicTest()
		main.setDisplayFps(false)
		main.setDisplayStatView(false)
		main.setShowSettings(false)
		AppSettings settings = new AppSettings(true)
		settings.setResolution(1024, 768)
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
	}

	private createFloor(Dyn4JAppState dyn4JAppState) {
		Box b = new Box(10, 0.1, 1)
		Geometry floorGeom = new Geometry("Box", b)
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat.setColor("Color", ColorRGBA.Green)
		floorGeom.setMaterial(mat)
		floorGeom.move(0f, -8f, 0f)
		rootNode.attachChild(floorGeom)
		floorGeom.addControl(new Dyn4JShapeControl(new Rectangle(10.0, 0.1), MassType.INFINITE))

		dyn4JAppState.add(floorGeom)
	}

	private createBox(Vector2f location, Dyn4JAppState dyn4JAppState) {
		Box b = new Box(0.5f, 0.5f, 0.5f)
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