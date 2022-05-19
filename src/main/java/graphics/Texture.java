package graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.opengl.GL46.glDeleteTextures;
import static org.lwjgl.opengl.GL46.glActiveTexture;
import static org.lwjgl.opengl.GL46.glGetUniformLocation;
import static org.lwjgl.opengl.GL46.glUniform1i;
import static org.lwjgl.opengl.GL46.glGenerateMipmap;

public class Texture {
    private final int texture;

    Texture(String path, int width, int height) {

        texture = glGenTextures();
        glActiveTexture(texture);

        bind();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        try {
            File imgPath = new File(path);
            BufferedImage image = ImageIO.read(imgPath);

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
            ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

            for (int h = 0; h < image.getHeight(); h++) {
                for (int w = 0; w < image.getWidth(); w++) {
                    int pixel = pixels[(image.getHeight() - 1 - h) * image.getWidth() + w];

                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            System.out.println("Texture (capacity = " + buffer.capacity() + ") loaded");
        } catch (Exception e) {
            System.out.println("Unable to load " + path + " texture");
            //System.out.println(e);
        }

        glGenerateMipmap(GL_TEXTURE_2D);

        unbind();
    }

    public void texUnit(Shader shader, String uniform, int unit) {
        int tex0Uni = glGetUniformLocation(shader.getId(), uniform);
        shader.activate();
        glUniform1i(tex0Uni, unit);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void delete() {
        glDeleteTextures(texture);
    }
}
