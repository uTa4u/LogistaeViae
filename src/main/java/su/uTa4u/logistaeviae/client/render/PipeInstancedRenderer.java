package su.uTa4u.logistaeviae.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.model.Quad;
import su.uTa4u.logistaeviae.mixin.ContainerLocalRenderInformationAccessor;
import su.uTa4u.logistaeviae.mixin.EntityRendererAccessor;
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
    private static final float[] IDENTITY_MATRIX = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };
    // Taken from org.lwjgl.opengl.APIUtil
    private static final int BUFFERS_SIZE = 32;
    private static final IntBuffer intBuffer = BufferUtils.createIntBuffer(BUFFERS_SIZE);
    private static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(BUFFERS_SIZE);

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

    private static final int program;
    private static final int vao;
    private static final int vbo;
    private static final int ebo;
    private static final FloatBuffer projMatrix = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer viewMatrix = BufferUtils.createFloatBuffer(16);
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

        EntityRendererAccessor entityRendererAccessor = ((EntityRendererAccessor) mc.entityRenderer);
        renderGlobalAccessor.getRenderEngine().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        // FIXME: generate launch scripts and launch in renderdoc
        // FIXME: generate launch scripts and launch in renderdoc
        // FIXME: generate launch scripts and launch in renderdoc
        //        this shit might not be needed

        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) GlStateManager.shadeModel(GL_SMOOTH);
        else GlStateManager.shadeModel(GL_FLAT);

        updateProjMatrix(entityRendererAccessor.callGetFOVModifier((float) partialTicks, true), (float) mc.displayWidth / mc.displayHeight, entityRendererAccessor.getFarPlaneDistance() * MathHelper.SQRT_2);
        glUniformMatrix4(projMatrixUniformLoc, false, projMatrix);

        updateViewMatrix(entity, (float) partialTicks);
        glUniformMatrix4(viewMatrixUniformLoc, false, viewMatrix);

        glBindVertexArray(vao);

        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(Quad.VERTEX_COUNT * Quad.VERTEX_LENGHT * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        IntBuffer indexBuffer = ByteBuffer.allocateDirect(Quad.INDICES.length * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(Quad.INDICES).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobalAccessor.getRenderInfos()) {
            for (TileEntity tileEntity : ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().getCompiledChunk().getTileEntities()) {
                if (!(tileEntity instanceof TileEntityPipe)) continue;
                if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) continue;

                for (Quad quad : PipeModelManager.getQuadsForPipe((TileEntityPipe) tileEntity).values()) {
                    vertexBuffer.put(quad.pack(tileEntity.getPos(), (float) cameraX, (float) cameraY, (float) cameraZ)).flip();
                    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
                    glDrawElements(GL_TRIANGLES, Quad.INDICES.length, GL_UNSIGNED_INT, 0);
                    vertexBuffer.clear();
                }
            }
        }

        RenderHelper.enableStandardItemLighting();

        restoreProgram();
        restoreVertexObjects();
        restoreCommonGlStates();
    }

    private static void makeIdentity(FloatBuffer matrix) {
        int pos = matrix.position();
        matrix.put(IDENTITY_MATRIX);
        matrix.position(pos);
    }

    // Taken from org.lwjgl.util.glu.Project#gluPerspective
    private static void updateProjMatrix(float fovy, float aspect, float zFar) {
        projMatrix.clear();

        final float zNear = 0.05f;
        float sine, cotangent, deltaZ;
        float radians = fovy / 2 * (float) Math.PI / 180;

        deltaZ = zFar - zNear;
        sine = (float) Math.sin(radians);

        if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
            return;
        }

        cotangent = (float) Math.cos(radians) / sine;

        makeIdentity(projMatrix);

        projMatrix.put(0 * 4 + 0, cotangent / aspect);
        projMatrix.put(1 * 4 + 1, cotangent);
        projMatrix.put(2 * 4 + 2, -(zFar + zNear) / deltaZ);
        projMatrix.put(2 * 4 + 3, -1);
        projMatrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ);
        projMatrix.put(3 * 4 + 3, 0);

        projMatrix.flip();
    }

    private static void updateViewMatrix(Entity camera, float partialTicks) {
        viewMatrix.clear();

        // This is already calculated in parent methods, but whatever I guess
        float x = (float) (camera.prevPosX + (camera.posX - camera.prevPosX) * partialTicks);
        float y = (float) (camera.prevPosY + (camera.posY - camera.prevPosY) * partialTicks);
        float z = (float) (camera.prevPosZ + (camera.posZ - camera.prevPosZ) * partialTicks);

        float yawRad = (float) Math.toRadians(camera.rotationYaw);
        float pitchRad = (float) Math.toRadians(camera.rotationPitch);

        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);

        makeIdentity(viewMatrix);

        viewMatrix.put(cosYaw);
        viewMatrix.put(sinYaw * sinPitch);
        viewMatrix.put(sinYaw * cosPitch);
        viewMatrix.put(0.0f);

        viewMatrix.put(0.0f);
        viewMatrix.put(cosPitch);
        viewMatrix.put(-sinPitch);
        viewMatrix.put(0.0f);

        viewMatrix.put(-sinYaw);
        viewMatrix.put(cosYaw * sinPitch);
        viewMatrix.put(cosYaw * cosPitch);
        viewMatrix.put(0.0f);

        viewMatrix.put(-(cosYaw * x - sinYaw * z));
        viewMatrix.put(-(sinYaw * sinPitch * x + cosPitch * y + cosYaw * sinPitch * z));
        viewMatrix.put(-(sinYaw * cosPitch * x - sinPitch * y + cosYaw * cosPitch * z));
        viewMatrix.put(1.0f);

        viewMatrix.flip();
    }

    private static void storeCommonGlStates() {
        glGetInteger(GL_TEXTURE_BINDING_2D, intBuffer);
        textureID = intBuffer.get(0);

        glGetFloat(GL_CURRENT_COLOR, floatBuffer);
        r = floatBuffer.get(0);
        g = floatBuffer.get(1);
        b = floatBuffer.get(2);
        a = floatBuffer.get(3);

        glGetInteger(GL_SHADE_MODEL, intBuffer);
        shadeModel = intBuffer.get(0);

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

        restoreVertexObjects();
    }

}
