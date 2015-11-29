package jmedyn4j
import static jmedyn4j.EventBusSingletonHolder.*
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppState
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node
import com.jme3.scene.Spatial;

import javax.sql.rowset.spi.SyncResolver;

import org.dyn4j.dynamics.Settings
import org.dyn4j.dynamics.World

class Dyn4JAppState extends AbstractAppState {
	private World world
	private Set<Spatial> spatials = new HashSet<Spatial>();
	void add(Spatial spatial) {
		if (world == null) world = new World()
		if (spatial.getControl(IDyn4JControl.class) == null) throw new IllegalArgumentException("Cannot handle a node which isnt a ${Dyn4JShapeControl.getClass().getSimpleName()}")
		synchronized(spatials) {
			 spatials.add(spatial)
			 IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class)
			 ctl.addToWorld(world)
		}
	}
	
	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager)
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
		super.stateDetached(stateManager);
	}
	
	
	public void updateWorld(float tpf) {
		//world.update(tpf)
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf)
		synchronized(spatials) {
			spatials.asList().each { Spatial spatial ->
				IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class)
				if (ctl == null) { spatials.remove(spatial); return; } //evict nodes which have their Dyn4JShapeControl removed
				ctl.updatePhysics(tpf)
			}
		}
		world.update(tpf, Integer.MAX_VALUE)
		synchronized(spatials) {
			spatials.asList().each { Spatial spatial ->
				IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class)
				if (ctl == null) { spatials.remove(spatial); return; } //evict nodes which have their Dyn4JShapeControl removed
				ctl.updateDraw(tpf)
			}
		}
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
	  super.initialize(stateManager, app);
	  
	}
}

