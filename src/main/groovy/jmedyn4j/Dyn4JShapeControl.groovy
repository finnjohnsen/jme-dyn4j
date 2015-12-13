package jmedyn4j

import java.io.IOException
import java.util.List;

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.AbstractShape
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2

import com.jme3.app.state.AppStateManager
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control
import com.sun.glass.ui.Application

class Dyn4JShapeControl implements Control, IDyn4JControl {
	private Spatial spatial
	protected Body body
	BodyFixture fixture
	private World world
	
	Dyn4JShapeControl(AbstractShape shape,
		 massType,
		 Double weight=null, //in kg/m
		 Double friction=null, // low = more slippery
		 Double restitution=null// more = more bouncy
		 ) {
		body = new Body()
		fixture = new BodyFixture(shape)
		if (friction) fixture.setFriction(friction)
		if (restitution) fixture.setRestitution(restitution)
		if (weight) fixture.setDensity(weight)
		body.addFixture(fixture)
		body.setMass(massType)
		body.setAutoSleepingEnabled(true)
	}
		 
	 @Override
	 void addToWorld(World world) {
		 world = world
		 world.addBody(body)
	 }
	 
	 @Override
	 void removeFromWorld(World world) {
		 world.removeBody(body)
		 world=null
	 }
	
	// more = more bouncy
	void setRestitution(Double restitution) {
		fixture.setRestitution(restitution)
	}
	// more = in kg/m
	void setDesity(Double kg) {
		fixture.setDensity(kg)
	}
	
	// low = more slippery
	void setFriction(Double friction) {
		fixture.setFriction(friction)
	}
	
	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial
		body.translate(new Double(spatial.getLocalTranslation().x), new Double(spatial.getLocalTranslation().y))

		//TODO: set initial rotation of the dyn4j-Body
		
	}

	private Double lastAngle=-1
	private Transform lastTransform = new Transform()
	
	private final static Float negligibleAngleRotation = 0.001f
	void updatePhysics(float tpf){}
	void updateDraw(float tpf) {
		Vector2 vector2 = body.getTransform().getTranslation()
		this.spatial.setLocalTranslation(
			new Float(vector2.x), 
			new Float(vector2.y), 0f)
		
		
		Transform transform = body.getTransform()
		if (transform.getTranslation().x == lastTransform.getTranslation().x && 
			transform.getTranslation().y == lastTransform.getTranslation().y) {
			this.spatial.setLocalTranslation(
				new Vector3f(
					new Float(transform.getTranslation().x),
					new Float(transform.getTranslation().y),
						0f))
			lastTransform=transform
		}
		Double angle = body.getTransform().getRotation()
		if (angle != lastAngle) {
			Quaternion roll = new Quaternion()
			roll.fromAngleAxis( new Float(angle) , Vector3f.UNIT_Z);
			this.spatial.setLocalRotation(roll)
			lastAngle = angle
		} 
	}
	
	@Override
	public void update(float tpf) {
		//Dyn4JAppState handles everything
	}
	
	@Override
	public void write(JmeExporter ex) throws IOException {
	}

	@Override
	public void read(JmeImporter im) throws IOException {
	}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		return null;
	}
	@Override
	public void render(RenderManager rm, ViewPort vp) {
	}


}
