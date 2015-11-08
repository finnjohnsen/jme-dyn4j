package jmedyn4j

import com.jme3.app.Application;
import com.jme3.app.state.AppState
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node
import com.jme3.scene.Spatial;

import javax.sql.rowset.spi.SyncResolver;

import org.dyn4j.dynamics.World

class Dyn4JAppState implements AppState {

	Boolean initialized = false
	Boolean enabled = false
	Boolean attached = false
	
	World world
	
	private Set<Node> nodes = new HashSet<Node>();
	
	
	void addNode(Node node) {
		if (node.getControl(Dyn4JShapeControl.class) == null) throw new IllegalArgumentException("Cannot handle a node which isnt a ${Dyn4JShapeControl.getClass().getSimpleName()}")
		synchronized(nodes) {
			 nodes.add(node)
		}
	}
	
	@Override
	public void update(float tpf) {
		if (initialized && enabled && attached && world) world.update(tpf)
		world.update(tpf)
		synchronized(nodes) {
			nodes.asList().each { Node n ->
				Dyn4JShapeControl ctl = n.getControl(Dyn4JShapeControl.class)
				if (ctl != null) { nodes.remove(n); return; } //evict nodes which have their Dyn4JShapeControl removed
				ctl.updateFromAppState()
			}
		}
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		world = new World()
		initialized = true
	}
	
	@Override
	public void render(RenderManager rm) {
	}

	@Override
	public void postRender() {
	}

	@Override
	public void cleanup() {
		//world = null
	}
	
	@Override
	public void stateAttached(AppStateManager stateManager) {
		attached = true
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
		attached = false
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setEnabled(boolean active) {
		enabled = active
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
