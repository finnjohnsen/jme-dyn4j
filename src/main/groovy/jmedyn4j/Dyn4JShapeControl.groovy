package jmedyn4j

import java.io.IOException;
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.AbstractShape
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Transform

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control

class Dyn4JShapeControl implements Control {
	private Spatial spatial
	private Body body

	// Leaky	
	Dyn4JShapeControl(AbstractShape shape, MassType massType) {
		body = new Body()
		body.addFixture(new BodyFixture(shape))
		body.setMass(massType)
	}
	
	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial
		body.setTransform(new Transform(
			new Float(spatial.getLocalTranslation().x),
			new Float(spatial.getLocalTranslation().y),
			0f))
		//TODO: set initial rotation of the dyn4j-Body
		
	}

	protected void updateFromAppState() {
		Transform trLocation = body.getTransform()
		this.spatial.setLocalTranslation(
			new Float(trLocation.getTranslationX()), 
			new Float(trLocation.getTranslationY()), 0f)
		/*
		Transform trRotation = body.getTransform()
				this.spatial.setLocalTranslation(
			new Float(trRotation.getTranslationX()), 
			new Float(trRotation.getTranslationY()), 0f)
			*/
		
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
