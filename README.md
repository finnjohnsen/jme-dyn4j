2D in jMonkeyengine.

jMonkeyEngine 2D using Dyn4J.

Rationale.
jMonkeyengine has an excellent 3D physics engine bundled - Bullet. I've recently been trying to make a 2D game with physics using all the standard stuff in jME, with mixed luck. I came really close by forcing Z=0f using physics tick listeners. But when stuff moved really fast (projectiles or really hard collisions) stuff started flying behind each other and I figured switching to a native 2D physics engine was the right way to go.

Solution.
Attempt to mimic the familiar API you use when using standard bullet.

Has a AppState - Dyn4JAppState, trying to be like BullettAppState
Has a Control - Dyn4JShapeControl, tries to be like the bullett controls (RigidBody etc)


Current state.
Got solid item working (floor) and a couple of squares and circles bouncing on it. Run BasicTest.groovy in src/test/groovy

![screenshot](etc/jme-dyn4j.gif)

