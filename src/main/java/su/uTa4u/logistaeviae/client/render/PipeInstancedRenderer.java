package su.uTa4u.logistaeviae.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.model.Quad;
import su.uTa4u.logistaeviae.mixin.ActiveRenderInfoAccessor;
import su.uTa4u.logistaeviae.mixin.ContainerLocalRenderInformationAccessor;
import su.uTa4u.logistaeviae.mixin.RenderGlobalAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

// Thanks tttsaurus for https://github.com/tttsaurus/Mc122RenderBook
// TODO: don't autosub this class to event, sub from ClientProxy only if GL4+ is available
@EventBusSubscriber
public final class PipeInstancedRenderer {
    // Taken from org.lwjgl.opengl.APIUtil
    private static final int BUFFERS_SIZE = 32;
    private static final IntBuffer INT_BUFFER = BufferUtils.createIntBuffer(BUFFERS_SIZE);
    private static final FloatBuffer FLOAT_BUFFER = BufferUtils.createFloatBuffer(BUFFERS_SIZE);

    private static int textureID;
    private static int shadeModel;
    private static float r;
    private static float g;
    private static float b;
    private static float a;
    private static boolean blend;
    private static boolean lighting;
    private static boolean texture2D;
    private static boolean alphaTest;
    private static boolean depthTest;
    private static boolean cullFace;

    private static int prevProgram;
    private static int prevVao;
    private static int prevVbo;
    private static int prevEbo;

    private static final FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(Quad.VERTEX_COUNT * Quad.VERTEX_LENGHT);
    private static final int program;
    private static final int vao;
    private static final int vbo;
    private static final int ebo;
    private static final int projMatrixUniformLoc;
    private static final int viewMatrixUniformLoc;

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) throw new RuntimeException("RenderViewEntity is null??? :sob:");
        double partialTicks = event.getPartialTicks();
        ICamera camera = new Frustum();
        double cameraX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double cameraY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double cameraZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        camera.setPosition(cameraX, cameraY, cameraZ);

        RenderGlobalAccessor renderGlobalAccessor = ((RenderGlobalAccessor) event.getContext());

        boolean hasSomethingToRender = false;
        checkHasSomethingToRender:
        for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobalAccessor.getRenderInfos()) {
            for (TileEntity tileEntity : ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().getCompiledChunk().getTileEntities()) {
                if (tileEntity instanceof TileEntityPipe && camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
                    hasSomethingToRender = true;
                    break checkHasSomethingToRender;
                }
            }
        }
        if (!hasSomethingToRender) return;

        storeCommonGlStates();
        storeVertexObjects();
        storeProgram();

        glUseProgram(program);

        renderGlobalAccessor.getRenderEngine().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        glUniformMatrix4(projMatrixUniformLoc, false, ActiveRenderInfoAccessor.getProjMatrix());
        glUniformMatrix4(viewMatrixUniformLoc, false, ActiveRenderInfoAccessor.getViewMatrix());

        GlStateManager.disableCull();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobalAccessor.getRenderInfos()) {
            for (TileEntity tileEntity : ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().getCompiledChunk().getTileEntities()) {
                if (!(tileEntity instanceof TileEntityPipe)) continue;
                if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) continue;

                for (Quad quad : PipeModelManager.getQuadsForPipe((TileEntityPipe) tileEntity).values()) {
                    vertexBuffer.put(quad.pack(tileEntity.getPos(), (float) cameraX, (float) cameraY, (float) cameraZ)).flip();
                    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
                    glDrawElements(GL_TRIANGLES, Quad.INDICES.length, GL_UNSIGNED_INT, 0);
                    vertexBuffer.clear();
                }
            }
        }

        restoreProgram();
        restoreVertexObjects();
        restoreCommonGlStates();
    }

    private static void storeCommonGlStates() {
        glGetInteger(GL_TEXTURE_BINDING_2D, INT_BUFFER);
        textureID = INT_BUFFER.get(0);

        glGetFloat(GL_CURRENT_COLOR, FLOAT_BUFFER);
        r = FLOAT_BUFFER.get(0);
        g = FLOAT_BUFFER.get(1);
        b = FLOAT_BUFFER.get(2);
        a = FLOAT_BUFFER.get(3);

        glGetInteger(GL_SHADE_MODEL, INT_BUFFER);
        shadeModel = INT_BUFFER.get(0);

        blend = glIsEnabled(GL_BLEND);
        lighting = glIsEnabled(GL_LIGHTING);
        texture2D = glIsEnabled(GL_TEXTURE_2D);
        alphaTest = glIsEnabled(GL_ALPHA_TEST);
        depthTest = glIsEnabled(GL_DEPTH_TEST);
        cullFace = glIsEnabled(GL_CULL_FACE);
    }

    private static void restoreCommonGlStates() {
        GlStateManager.bindTexture(textureID);
        GlStateManager.color(r, g, b, a);
        GlStateManager.shadeModel(shadeModel);

        if (blend) GlStateManager.enableBlend();
        else GlStateManager.disableBlend();

        if (lighting) GlStateManager.enableLighting();
        else GlStateManager.disableLighting();

        if (texture2D) GlStateManager.enableTexture2D();
        else GlStateManager.disableTexture2D();

        if (alphaTest) GlStateManager.enableAlpha();
        else GlStateManager.disableAlpha();

        if (depthTest) GlStateManager.enableDepth();
        else GlStateManager.disableDepth();

        if (cullFace) GlStateManager.enableCull();
        else GlStateManager.disableCull();
    }

    private static void storeVertexObjects() {
        prevVao = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        prevVbo = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        prevEbo = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
    }

    private static void restoreVertexObjects() {
        glBindVertexArray(prevVao);
        glBindBuffer(GL_ARRAY_BUFFER, prevVbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }

    private static void storeProgram() {
        prevProgram = glGetInteger(GL_CURRENT_PROGRAM);
    }

    private static void restoreProgram() {
        glUseProgram(prevProgram);
    }

    private static String getShaderSource(String name) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new IOException();
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load shader file: " + name, e);
        }
    }

    static {
        program = glCreateProgram();
        if (program == 0) throw new RuntimeException("Could not create shader program");

        int vertShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShaderID, getShaderSource("assets/" + Tags.MOD_ID + "/shaders/pipe.vert"));
        glCompileShader(vertShaderID);

        if (glGetShaderi(vertShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Could not compile vertex shader: " + glGetShaderInfoLog(vertShaderID, 1024));
        }
        glAttachShader(program, vertShaderID);

        int fragShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShaderID, getShaderSource("assets/" + Tags.MOD_ID + "/shaders/pipe.frag"));
        glCompileShader(fragShaderID);

        if (glGetShaderi(fragShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Could not compile fragment shader: " + glGetShaderInfoLog(fragShaderID, 1024));
        }
        glAttachShader(program, fragShaderID);

        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Could not link shader program: " + glGetProgramInfoLog(program, 1024));
        }

        glDetachShader(program, vertShaderID);
        glDeleteShader(vertShaderID);
        glDetachShader(program, fragShaderID);
        glDeleteShader(fragShaderID);

        final String projMatrixUniformName = "projMatrix";
        projMatrixUniformLoc = glGetUniformLocation(program, projMatrixUniformName);
        if (projMatrixUniformLoc < 0) throw new RuntimeException("Could not create uniform: " + projMatrixUniformName + " in shader program " + program);

        final String viewMatrixUniformName = "viewMatrix";
        viewMatrixUniformLoc = glGetUniformLocation(program, viewMatrixUniformName);
        if (viewMatrixUniformLoc < 0) throw new RuntimeException("Could not create uniform: " + viewMatrixUniformName + " in shader program " + program);

        storeVertexObjects();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(
                GL_ELEMENT_ARRAY_BUFFER,
                (IntBuffer) BufferUtils.createIntBuffer(Quad.INDICES.length).put(Quad.INDICES).flip(),
                GL_STATIC_DRAW
        );

        restoreVertexObjects();
    }

    private static final class RenderInfo {
        private final int x;
        private final int y;
        private final int z;

        private RenderInfo(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
    }

}
