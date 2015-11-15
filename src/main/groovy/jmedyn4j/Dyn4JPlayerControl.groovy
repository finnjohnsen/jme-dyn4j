package jmedyn4j

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.Force
import org.dyn4j.dynamics.joint.Joint
import org.dyn4j.dynamics.joint.MotorJoint
import org.dyn4j.geometry.AbstractShape
import org.dyn4j.geometry.Capsule
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2

import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Spatial
import com.jme3.scene.control.Control

class Dyn4JPlayerControl implements Control, IDyn4JControl {
	
	private Spatial spatial
	Body body
	Body controller
	MotorJoint joint
	BodyFixture bodyFixture
	
	private Long weight = 80
	Dyn4JPlayerControl(Double width=0.3, Double height=1.8, Long weight=80) {
		this.weight=weight
		AbstractShape shape = new Capsule(width, height)
		
		body = new Body()
		bodyFixture = new BodyFixture(shape)
		bodyFixture.setDensity(weight);
		bodyFixture.setFriction(1) //no slipperiness
		bodyFixture.setRestitution(0) //no bouncyness
		body.addFixture(bodyFixture)
		body.setMass(MassType.NORMAL)
		
		body.setAutoSleepingEnabled(false)
		bodies.add(body)
		
		controller = new Body()
		BodyFixture controllerFixture = new BodyFixture(shape)
		controllerFixture.setSensor(true)
		controllerFixture.setDensity(weight);
		
		controller.setMass(MassType.INFINITE)
		
		controller.addFixture(controllerFixture);
		
		joint = new MotorJoint(controller, body);
		
		joint.setLinearTarget(new Vector2(0.5, 0.5));
		joint.setAngularTarget(Math.PI * 1.0);
		joint.setCorrectionFactor(1.0);
		
		// allow translational changes (change this depending on how fast you
		// want the player body to react to changes in the controller body)
		joint.setMaximumForce(1);
		
		// allow rotational changes (change this depending on how fast you want
		// the player body to react to changes in the controller body)
		joint.setMaximumTorque(Double.MAX_VALUE);
		joint.setCollisionAllowed(false)
	}
	

	
	// more = more bouncy
	void setRestitution(Double restitution) {
		bodyFixture.setRestitution(restitution)
	}
	// more = in kg/m
	void setDesity(Double kg) {
		bodyFixture.setDensity(kg)
	}
	
	// low = more slippery
	void setFriction(Double friction) {
		bodyFixture.setFriction(friction)
	}
		
	List<Body> getBodies() {
		[body, controller];
	}	
	List<Joint> getJoints() {
		[joint]
	}
	
	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial
		body.translate(new Double(spatial.getLocalTranslation().x), new Double(spatial.getLocalTranslation().y))
		controller.translate(new Double(spatial.getLocalTranslation().x), new Double(spatial.getLocalTranslation().y))
	}

	private Double lastAngle=-1
	private Transform lastTransform = new Transform()
	void updateFromAppState() {
		if (walkRight) {
			if (body.getLinearVelocity().x < 0) body.setLinearVelocity(0, body.getLinearVelocity().y)
			if (body.getLinearVelocity().x < 3) body.applyImpulse(new Vector2(weight/2, 0));
		} else if (walkLeft) {
			if (body.getLinearVelocity().x > 0) body.setLinearVelocity(0, body.getLinearVelocity().y)
			if (body.getLinearVelocity().x > -3) body.applyImpulse(new Vector2(-(weight/2), 0));
		} else {
			if (Math.abs(body.getLinearVelocity().x)>(weight/4)) {
				if (body.getLinearVelocity().x>0)body.setLinearVelocity(weight/4, body.getLinearVelocity().y)
				if (body.getLinearVelocity().x<0)body.setLinearVelocity(-(weight/4), body.getLinearVelocity().y)
			}
		}
		
		if (jump) {
			println "${body.getLinearVelocity().y}"
			jump=false
			if (body.getInContactBodies(false).size() == 0) { //poor mans "is on ground" check.
			} else if (body.getLinearVelocity().y>0.1) {	
			} else {
				body.applyImpulse(new Vector2(0, weight*3.0));
			}
			 
			
		}
		
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
	
	Boolean walkRight = false
	Boolean walkLeft = false
	Boolean jump = false
	
	void moveRight() {
		walkRight=true
	}
	
	void moveLeft() {
		walkLeft=true
	}
	
	void stopMoveRight() {
		walkRight=false
	}
	
	void stopMoveLeft() {
		walkLeft=false
	}
	
	void jump() {
		jump=true
	}
	
	@Override
	public void update(float tpf) {
	}
	
	@Override 
	public void write(JmeExporter ex) throws IOException {}

	@Override 
	public void read(JmeImporter im) throws IOException {}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		return null;
	}
	@Override
	public void render(RenderManager rm, ViewPort vp) {
	}
}
