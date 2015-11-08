package jmedyn4j

import java.io.IOException;
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
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

class Dyn4JShapeControl implements Control {
	private Spatial spatial
	protected Body body
	BodyFixture fixture
	// Leaky	
	Dyn4JShapeControl(AbstractShape shape, MassType massType) {
		body = new Body()
		fixture = new BodyFixture(shape)
		//fixture.restitution=0.2
		//fixture.friction=0.8
		//fixture.setDensity(10)
		body.addFixture(fixture)
		body.setMass(massType)
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

	private Float m00=0f
	private Float m01=0f
	
	private final static Float negligibleRotation = 0.001f
	protected void updateFromAppState() {
		Vector2 vector2 = body.getTransform().getTranslation()
		this.spatial.setLocalTranslation(
			new Float(vector2.x), 
			new Float(vector2.y), 0f)
		
		
		Transform transform = body.getTransform()
		//log.info "new box ${transform.x} ${transform.y}"
		this.spatial.setLocalTranslation(
			new Vector3f(
				new Float(transform.getTranslation().x),
				new Float(transform.getTranslation().y),
					0f))
		
		Double[] rot = body.getTransform().getValues()
		if ( Math.abs(rot[0] - m00) > negligibleRotation || Math.abs(rot[1] - m01) > negligibleRotation) {
			Quaternion roll = new Quaternion()
			roll.fromRotationMatrix(
				new Float(rot[0]), new Float(rot[1]), 0f,
				0f, 0f, 0f,
				0f, 0f, 0f)
			this.spatial.setLocalRotation(roll)
			m00=rot[0]
			m01=rot[1]
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
