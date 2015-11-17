package jmedyn4j

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.Force
import org.dyn4j.dynamics.World
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
	Boolean debugging = false
	
	private Spatial spatial
	private Long weight = 80
	
	
	private Body mainBody
	private Body controllerbody
	private MotorJoint joint
	Dyn4JPlayerControl(Double width=0.3, Double height=1.8, Long weight=80) {
		this.weight=weight
		AbstractShape shape = new Capsule(width, height)
		
		mainBody = new Body()
		BodyFixture bodyFixture = new BodyFixture(shape)
		bodyFixture.setDensity(weight);
		bodyFixture.setFriction(1) //no slipperiness
		bodyFixture.setRestitution(0) //no bouncyness
		mainBody.addFixture(bodyFixture)
		mainBody.setMass(MassType.NORMAL)
		mainBody.setAutoSleepingEnabled(false)
		
		controllerbody = new Body()
		BodyFixture controllerFixture = new BodyFixture(shape)
		controllerFixture.setSensor(true)
		controllerFixture.setDensity(weight);
		controllerFixture.setDensity(weight);
		controllerFixture.setFriction(1) //no slipperiness
		controllerFixture.setRestitution(0) //no bouncyness
		controllerbody.addFixture(controllerFixture);
		controllerbody.setMass(MassType.INFINITE)
		
		joint = new MotorJoint(controllerbody, mainBody);
		joint.setLinearTarget(new Vector2(0.5, 0.5));
		joint.setAngularTarget(Math.PI * 1.0);
		joint.setCorrectionFactor(1.0);
		joint.setMaximumForce(1);
		joint.setMaximumTorque(Double.MAX_VALUE);
		joint.setCollisionAllowed(false)
	}
	
	@Override 
	void addToWorld(World world) {
		world.addBody(mainBody)
		world.addBody(controllerbody)
		world.addJoint(joint)
	}
	
	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial
		mainBody.translate(new Double(spatial.getLocalTranslation().x), new Double(spatial.getLocalTranslation().y))
		controllerbody.translate(new Double(spatial.getLocalTranslation().x), new Double(spatial.getLocalTranslation().y))
	}

	private Double lastAngle=-1
	private Transform lastTransform = new Transform()
	void updateFromAppState() {
		if (debugging) println "updateFromAppState"
		updateWalkDirection()
		updateJump()
		updateLocation()
		updateRotation()
	}

	private updateRotation() {
		Double angle = mainBody.getTransform().getRotation()
		if (angle != lastAngle) {
			Quaternion roll = new Quaternion()
			roll.fromAngleAxis( new Float(angle) , Vector3f.UNIT_Z);
			this.spatial.setLocalRotation(roll)
			if (debugging) println "rotating to ${angle}"
			lastAngle = angle
		}
	}

	Map getTrlv() {
		Vector2 tr = mainBody.getTransform().getTranslation()
		Vector2 lv = mainBody.getLinearVelocity()
		return [tr:[x:tr.x.round(4), y:tr.y.round(4)], lv:[x:lv.x.round(4), y:lv.y.round(4)]]
	}
	
	void setTrlv(Map trlv) {
		Transform tr = new Transform()
		tr.setTranslation(trlv.tr.x, trlv.tr.y)
		tr.setRotation(mainBody.getTransform().getRotation())
		mainBody.setTransform(tr)
		mainBody.setLinearVelocity(trlv.lv.x, trlv.lv.y)
	}
	
	
	private updateLocation() {
		Vector2 vector2 = mainBody.getTransform().getTranslation()
		if (debugging) println "moving to $vector2"
		this.spatial.setLocalTranslation(
				new Float(vector2.x),
				new Float(vector2.y), 0f)
/*
		Transform transform = mainBody.getTransform()
		if (transform.getTranslation().x == lastTransform.getTranslation().x &&
		transform.getTranslation().y == lastTransform.getTranslation().y) {
			this.spatial.setLocalTranslation(
					new Vector3f(
					new Float(transform.getTranslation().x),
					new Float(transform.getTranslation().y),
					0f))
			lastTransform=transform
		}*/
	}

	private updateJump() {
		if (jump) {
			jump=false
			if (canJump()) mainBody.applyImpulse(new Vector2(0, weight*3.5));
		}
	}
	
	Boolean canJump() {
		(mainBody.getInContactBodies(false).size() != 0 
		&& mainBody.getLinearVelocity().y<0.1)
	}

	private updateWalkDirection() {
		if (walkRight) {
			if (mainBody.getLinearVelocity().x < 0) mainBody.setLinearVelocity(0, mainBody.getLinearVelocity().y)
			if (mainBody.getLinearVelocity().x < 3) mainBody.applyImpulse(new Vector2(weight/2, 0));
		} else if (walkLeft) {
			if (mainBody.getLinearVelocity().x > 0) mainBody.setLinearVelocity(0, mainBody.getLinearVelocity().y)
			if (mainBody.getLinearVelocity().x > -3) mainBody.applyImpulse(new Vector2(-(weight/2), 0));
		} else { //stop
			//if (Math.abs(body.getLinearVelocity().x)>(weight/4)) {
			//	if (body.getLinearVelocity().x>0)body.setLinearVelocity(weight/3, body.getLinearVelocity().y)
			//	if (body.getLinearVelocity().x<0)body.setLinearVelocity(-(weight/4), body.getLinearVelocity().y)
			//}
		}
	}
	
	private Boolean walkRight = false
	private Boolean walkLeft = false
	private Boolean jump = false
	
	void doMove(String move) {
		switch (move) {
			case ("Join"):  break; /*ignore*/
			case ("Right"): moveRight(); break;
			case ("Left"): moveLeft(); break;
			case ("StopLeft"): stopMoveLeft(); break;
			case ("StopRight"): stopMoveRight(); break;
			case ("Jump"): jump(); break;
			default: println "No idea what to do about $move"; 
		}
	}
	
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
