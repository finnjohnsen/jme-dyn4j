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

	// Leaky	
	Dyn4JShapeControl(AbstractShape shape, MassType massType) {
		body = new Body()
		body.addFixture(new BodyFixture(shape))
		body.setMass(massType)
	}
	
	
	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial
		body.translate(new Double(spatial.getLocalTranslation().x), new Double(spatial.getLocalTranslation().y))
		//TODO: set initial rotation of the dyn4j-Body
		
	}

	protected void updateFromAppState() {
		Vector2 vector2 = body.getTransform().getTranslation()
		println "updateFromAppState: $vector2"
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
		/*
		Double r = body.getTransform().getRotation()
		println "$r"
		Quaternion roll = new Quaternion() 
		roll.fromAngleAxis( new Float(r) ,  Vector3f.UNIT_X);
		this.spatial.setLocalRotation(roll)*/
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
