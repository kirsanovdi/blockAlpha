package engine;

import controller.Commands;
import graphics.Camera;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Set;

import static controller.Commands.*;

public class Model {
    private Camera camera;

    private Vector3f position;
    private final float g = 0.00098f;
    private float speed;
    private float downSpeed;
    private final Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private final float modelHeight = 1.5f;

    Model(Vector3f position, float speed, Camera camera) {
        this.position = new Vector3f(position);
        this.speed = speed;
        this.downSpeed = 0f;
        this.camera = camera;
    }

    /**
     * new Vector3f
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * new Vector3f
     */
    public Vector3f getCameraPosition() {
        return new Vector3f(position).add(0f, modelHeight, 0f);
    }

    /**
     * new Vector3f
     */
    public Vector3f getOrientation() {
        return new Vector3f(camera.orientation);
    }

    public void handleInput(Set<Commands> commandsSet, Set<Vector3i> nearestBlocks) {
        Vector3f orientation = new Vector3f(camera.orientation);
        orientation.y = 0f;
        orientation.normalize();

        if (commandsSet.contains(FORWARD)) moveForward(orientation, nearestBlocks);
        if (commandsSet.contains(BACKWARD)) moveBackward(orientation, nearestBlocks);
        if (commandsSet.contains(LEFT)) moveLeft(orientation, nearestBlocks);
        if (commandsSet.contains(RIGHT)) moveRight(orientation, nearestBlocks);
        if (commandsSet.contains(JUMP)) downSpeed += 0.1f;
        fallDown(nearestBlocks);

        final float df = 0.1f;
        if (commandsSet.contains(SPEED_1)) speed = 1.0f * df ;
        if (commandsSet.contains(SPEED_01)) speed = 0.1f * df ;
        if (commandsSet.contains(SPEED_0025)) speed = 0.025f * df ;
        camera.setPos(new Vector3f(position).add(0f, modelHeight, 0f));
    }

    protected boolean checkMove(Vector3f delta, Set<Vector3i> nearestBlocks) {
        final Vector3i adjustableProbPosition = EngineRuntime.getVector3i(getPosition().add(delta));
        final Vector3i cameraProbPos = EngineRuntime.getVector3i(getCameraPosition().add(delta));
        while (adjustableProbPosition.y <= cameraProbPos.y) {
            if(nearestBlocks.contains(adjustableProbPosition)) return false;
            adjustableProbPosition.add(0, 1, 0);
        }
        return true;
    }

    private void moveForward(Vector3f movingOrientation, Set<Vector3i> nearestBlocks) {
        final Vector3f delta = new Vector3f(movingOrientation).mul(speed);
        final Vector3f dx = new Vector3f(delta).mul(0f, 0f, 16f), dz = new Vector3f(delta).mul(16f, 0f, 0f);
        if (checkMove(dx, nearestBlocks))
            position.add(dx.mul(0.05f));
        if (checkMove(dz, nearestBlocks))
            position.add(dz.mul(0.05f));
    }

    private void moveBackward(Vector3f movingOrientation, Set<Vector3i> nearestBlocks) {
        final Vector3f delta = new Vector3f(movingOrientation).mul(-speed);
        final Vector3f dx = new Vector3f(delta).mul(0f, 0f, 16f), dz = new Vector3f(delta).mul(16f, 0f, 0f);
        if (checkMove(dx, nearestBlocks))
            position.add(dx.mul(0.05f));
        if (checkMove(dz, nearestBlocks))
            position.add(dz.mul(0.05f));
    }

    private void moveRight(Vector3f movingOrientation, Set<Vector3i> nearestBlocks) {
        final Vector3f delta = new Vector3f(movingOrientation).cross(up).normalize().mul(speed);
        final Vector3f dx = new Vector3f(delta).mul(0f, 0f, 16f), dz = new Vector3f(delta).mul(16f, 0f, 0f);
        if (checkMove(dx, nearestBlocks))
            position.add(dx.mul(0.05f));
        if (checkMove(dz, nearestBlocks))
            position.add(dz.mul(0.05f));
    }

    private void moveLeft(Vector3f movingOrientation, Set<Vector3i> nearestBlocks) {
        final Vector3f delta = new Vector3f(movingOrientation).cross(up).normalize().mul(-speed);
        final Vector3f dx = new Vector3f(delta).mul(0f, 0f, 16f), dz = new Vector3f(delta).mul(16f, 0f, 0f);
        if (checkMove(dx, nearestBlocks))
            position.add(dx.mul(0.05f));
        if (checkMove(dz, nearestBlocks))
            position.add(dz.mul(0.05f));
    }

    private void fallDown(Set<Vector3i> nearestBlocks) {
        final Vector3f dy = new Vector3f(0f, downSpeed, 0f);
        if (checkMove(dy, nearestBlocks)) {
            position.add(dy);
            downSpeed -= g;
        } else downSpeed = 0f;
    }
}