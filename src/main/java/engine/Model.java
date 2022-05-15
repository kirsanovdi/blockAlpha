package engine;

import controller.Commands;
import graphics.Camera;
import org.joml.Vector3f;

import java.util.Set;
import static controller.Commands.*;

public class Model {
    private Camera camera;

    private Vector3f position;
    private float speed;
    private final Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

    Model(Vector3f position, float speed, Camera camera){
        this.position = new Vector3f(position);
        this.speed = speed;
        this.camera = camera;
    }

    public Vector3f getPosition(){
        return new Vector3f(position);
    }
    public Vector3f getOrientation(){
        return new Vector3f(camera.orientation);
    }

    public void handleInput(Set<Commands> commandsSet){
        if(commandsSet.contains(FORWARD)) moveForward(camera.orientation);
        if(commandsSet.contains(BACKWARD)) moveBackward(camera.orientation);
        if(commandsSet.contains(LEFT)) moveLeft(camera.orientation);
        if(commandsSet.contains(RIGHT)) moveRight(camera.orientation);

        if(commandsSet.contains(SPEED_1)) speed = 1.0f;
        if(commandsSet.contains(SPEED_01)) speed = 0.1f;
        if(commandsSet.contains(SPEED_0025)) speed = 0.025f;
        camera.setPos(position);
    }

    private void moveForward(Vector3f movingOrientation){
        position.add(new Vector3f(movingOrientation).mul(speed));
    }
    private void moveBackward(Vector3f movingOrientation){
        position.add(new Vector3f(movingOrientation).mul(-speed));
    }
    private void moveRight(Vector3f movingOrientation){
        position.add(new Vector3f(movingOrientation).cross(up).normalize().mul(speed));
    }
    private void moveLeft(Vector3f movingOrientation){
        position.add(new Vector3f(movingOrientation).cross(up).normalize().mul(-speed));
    }
}
