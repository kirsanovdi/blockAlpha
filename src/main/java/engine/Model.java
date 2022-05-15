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

    Model(Vector3f position, float speed, Camera camera){
        this.position = new Vector3f(position);
        this.speed = speed;
        this.downSpeed = 0f;
        this.camera = camera;
    }

    public Vector3f getPosition(){
        return new Vector3f(position);
    }

    public Vector3f getCameraPosition(){
        return new Vector3f(position).add(0f, 1.5f, 0f);
    }

    public Vector3f getOrientation(){
        return new Vector3f(camera.orientation);
    }

    public void handleInput(Set<Commands> commandsSet, Set<Vector3i> nearestBlocks){
        Vector3f orientation = new Vector3f(camera.orientation);
        orientation.y = 0f;

        if(commandsSet.contains(FORWARD)) moveForward(orientation, nearestBlocks);
        if(commandsSet.contains(BACKWARD)) moveBackward(orientation, nearestBlocks);
        if(commandsSet.contains(LEFT)) moveLeft(orientation, nearestBlocks);
        if(commandsSet.contains(RIGHT)) moveRight(orientation, nearestBlocks);
        if(commandsSet.contains(JUMP)) downSpeed += 0.1f;
        fallDown(nearestBlocks);

        if(commandsSet.contains(SPEED_1)) speed = 1.0f;
        if(commandsSet.contains(SPEED_01)) speed = 0.1f;
        if(commandsSet.contains(SPEED_0025)) speed = 0.025f;
        camera.setPos(new Vector3f(position).add(0f, 1.5f, 0f));
    }

    private void moveForward(Vector3f movingOrientation, Set<Vector3i> nearestBlocks){
        final Vector3f probPosition = new Vector3f(position).add(new Vector3f(movingOrientation).mul(speed));
        if(!nearestBlocks.contains(new Vector3i((int)probPosition.x, (int)probPosition.y, (int)probPosition.z)))
            position.add(new Vector3f(movingOrientation).mul(speed));
    }
    private void moveBackward(Vector3f movingOrientation, Set<Vector3i> nearestBlocks){
        final Vector3f probPosition = new Vector3f(position).add(new Vector3f(movingOrientation).mul(-speed));
        if(!nearestBlocks.contains(new Vector3i((int)probPosition.x, (int)probPosition.y, (int)probPosition.z)))
            position.add(new Vector3f(movingOrientation).mul(-speed));
        //position.add(new Vector3f(movingOrientation).mul(-speed));
    }
    private void moveRight(Vector3f movingOrientation, Set<Vector3i> nearestBlocks){
        final Vector3f probPosition = new Vector3f(position).add(new Vector3f(movingOrientation).cross(up).normalize().mul(speed));
        if(!nearestBlocks.contains(new Vector3i((int)probPosition.x, (int)probPosition.y, (int)probPosition.z)))
            position.add(new Vector3f(movingOrientation).cross(up).normalize().mul(speed));
        //position.add(new Vector3f(movingOrientation).cross(up).normalize().mul(speed));
    }
    private void moveLeft(Vector3f movingOrientation, Set<Vector3i> nearestBlocks){
        final Vector3f probPosition = new Vector3f(position).add(new Vector3f(movingOrientation).cross(up).normalize().mul(-speed));
        if(!nearestBlocks.contains(new Vector3i((int)probPosition.x, (int)probPosition.y, (int)probPosition.z)))
            position.add(new Vector3f(movingOrientation).cross(up).normalize().mul(-speed));
        //position.add(new Vector3f(movingOrientation).cross(up).normalize().mul(-speed));
    }

    private void fallDown(Set<Vector3i> nearestBlocks){
        final Vector3f probPosition = new Vector3f(position).add(0f, downSpeed, 0f);
        if(!nearestBlocks.contains(new Vector3i((int)probPosition.x, (int)probPosition.y, (int)probPosition.z))) {
            position.add(0f, downSpeed, 0f);
            downSpeed -= g;
        } else downSpeed = 0f;
    }
}
