package jmedyn4j

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.joint.Joint

interface IDyn4JControl {
	void updateFromAppState()
	List<Body> getBodies()
	List<Joint> getJoints()
}
