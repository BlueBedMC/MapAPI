package com.bluebed.mapapi.minestom.lwjgl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.MapColors;
import net.minestom.server.timer.Task;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.TemporalUnit;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;

public abstract class GLFWCapableBuffer {
    protected final byte[] colors;
    private final ByteBuffer pixels;
    private final long glfwWindow;
    private final int width;
    private final int height;
    private final ByteBuffer colorsBuffer;
    private boolean onlyMapColors;

    protected GLFWCapableBuffer(int width, int height) {
        this(width, height, GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new context
     */
    protected GLFWCapableBuffer(int width, int height, int apiContext, int clientAPI) {
        this.width = width;
        this.height = height;
        this.colors = new byte[width*height];
        colorsBuffer = BufferUtils.createByteBuffer(width*height);
        this.pixels = BufferUtils.createByteBuffer(width*height*4);
        if(!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW");
        }

        GLFWErrorCallback.createPrint().set();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_CREATION_API, apiContext);
        glfwWindowHint(GLFW_CLIENT_API, clientAPI);

        this.glfwWindow = glfwCreateWindow(width, height, "glfw", 0L, 0L);
        if(glfwWindow == 0L) {
            try(var stack = MemoryStack.stackPush()) {
                PointerBuffer desc = stack.mallocPointer(1);
                int errcode = glfwGetError(desc);
                throw new RuntimeException("("+errcode+") Failed to create GLFW Window.");
            }
        }

        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    public GLFWCapableBuffer unbindContextFromThread() {
        glfwMakeContextCurrent(0L);
        return this;
    }

    public void changeRenderingThreadToCurrent() {
        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    public Task setupRenderLoop(long period, TemporalUnit unit, WindowRunnable rendering) {
        return setupRenderLoop(Duration.of(period, unit), rendering);
    }

    public Task setupRenderLoop(Duration period, WindowRunnable rendering) {
        return MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    changeRenderingThreadToCurrent();

                    render(rendering);
                })
                .repeat(period)
                .schedule();
    }

    public void render(WindowRunnable rendering) {
        rendering.run(glfwWindow);
        glfwSwapBuffers(glfwWindow);
        prepareMapColors();
    }

    /**
     * Called in render after glFlush to read the pixel buffer contents and convert it to map colors.
     * Only call if you do not use {@link #render(WindowRunnable)} nor {@link #setupRenderLoop}
     */
    public void prepareMapColors() {
        if(onlyMapColors) {
            colorsBuffer.rewind();
            glReadPixels(0, 0, width, height, GL_RED, GL_UNSIGNED_BYTE, colorsBuffer);
            colorsBuffer.get(colors);
        } else {
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = Framebuffer.index(x, y, width)*4;
                    int red = pixels.get(i) & 0xFF;
                    int green = pixels.get(i+1) & 0xFF;
                    int blue = pixels.get(i+2) & 0xFF;
                    int alpha = pixels.get(i+3) & 0xFF;
                    int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                    colors[Framebuffer.index(x, y, width)] = MapColors.closestColor(argb).getIndex();
                }
            }
        }
    }

    public void cleanup() {
        glfwTerminate();
    }

    public long getGLFWWindow() {
        return glfwWindow;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * Tells this buffer that the **RED** channel contains the index of the map color to use.
     *
     * This allows for optimizations and fast rendering (because there is no need for a conversion)
     */
    public void useMapColors() {
        onlyMapColors = true;
    }

    /**
     * Opposite to {@link #useMapColors()}
     */
    public void useRGB() {
        onlyMapColors = false;
    }

    public BufferedImage asBufferedImage() {
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // RGBA

        // Read pixels from the framebuffer
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Create BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;

                // OpenGL's origin is bottom-left, BufferedImage is top-left
                image.setRGB(x, height - (y + 1), (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return image;
    }
}
