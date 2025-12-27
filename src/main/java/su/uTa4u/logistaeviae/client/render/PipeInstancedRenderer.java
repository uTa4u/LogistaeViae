package su.uTa4u.logistaeviae.client.render;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ByteArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.block.ModBlocks;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.model.PipeQuad;
import su.uTa4u.logistaeviae.mixin.ActiveRenderInfoAccessor;
import su.uTa4u.logistaeviae.mixin.ContainerLocalRenderInformationAccessor;
import su.uTa4u.logistaeviae.mixin.RenderGlobalAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL42.glDrawElementsInstancedBaseInstance;

// Thanks tttsaurus for https://github.com/tttsaurus/Mc122RenderBook
public final class PipeInstancedRenderer {
    public static PipeInstancedRenderer instance;

    private final OpenGLSaver glsaver;

    private final ByteBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private final Object2ByteArrayMap<TextureAtlasSprite> textureIDs;
    private final int program;
    private final int vao;
    private final int baseInstancevbo;
    private final int instvbo;
    private final int ebo;
    private final int projMatrixUniformLoc;
    private final int viewMatrixUniformLoc;
    private final int texBufferUniformLoc;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return;
        TextureMap textureMap = mc.getTextureMapBlocks();

        double partialTicks = event.getPartialTicks();
        ICamera camera = new Frustum();
        double cameraX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double cameraY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double cameraZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        camera.setPosition(cameraX, cameraY, cameraZ);

        RenderGlobalAccessor renderGlobalAccessor = ((RenderGlobalAccessor) event.getContext());

        // Try to optimize this somehow
        Byte2ObjectMap<List<TileEntityPipe>> pipeByType = new Byte2ObjectArrayMap<>();
        for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobalAccessor.getRenderInfos()) {
            for (TileEntity te : ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().getCompiledChunk().getTileEntities()) {
                if (te instanceof TileEntityPipe && camera.isBoundingBoxInFrustum(te.getRenderBoundingBox())) {
                    TileEntityPipe pipe = (TileEntityPipe) te;
                    pipeByType.computeIfAbsent(pipe.packConnections(), k -> new ArrayList<>()).add(pipe);
                }
            }
        }
        if (pipeByType.isEmpty()) return;

        this.glsaver.storeCommonGlStates();
        this.glsaver.storeVertexObjects();
        this.glsaver.storeProgram();

        glUseProgram(this.program);

        renderGlobalAccessor.getRenderEngine().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        glUniformMatrix4(this.projMatrixUniformLoc, false, ActiveRenderInfoAccessor.getProjMatrix());
        glUniformMatrix4(this.viewMatrixUniformLoc, false, ActiveRenderInfoAccessor.getViewMatrix());

        GlStateManager.disableCull();

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBindBuffer(GL_ARRAY_BUFFER, this.instvbo);

        for (Byte2ObjectMap.Entry<List<TileEntityPipe>> entry : pipeByType.byte2ObjectEntrySet()) {
            List<TileEntityPipe> pipes = entry.getValue();
            if (pipes.isEmpty()) continue;
            byte packedConnections = entry.getByteKey();

            this.vertexBuffer.clear();
            for (TileEntityPipe pipe : pipes) {
                BlockPos pos = pipe.getPos();
                this.vertexBuffer.putFloat((float) (pos.getX() - cameraX));
                this.vertexBuffer.putFloat((float) (pos.getY() - cameraY));
                this.vertexBuffer.putFloat((float) (pos.getZ() - cameraZ));
                this.vertexBuffer.put(this.textureIDs.getByte(textureMap.getAtlasSprite(PipeModelManager.getTextureLoc(pipe))));
            }
            this.vertexBuffer.flip();
            glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_DYNAMIC_DRAW);

            final int indicesPerPipe = PipeQuad.INDEX_COUNT * PipeModelManager.QUAD_COUNT;

            glDrawElementsInstancedBaseInstance(
                    GL_TRIANGLES,
                    indicesPerPipe,
                    GL_UNSIGNED_INT,
                    (long) packedConnections * indicesPerPipe * Integer.BYTES,
                    pipes.size(),
                    0
            );
        }

        this.glsaver.restoreProgram();
        this.glsaver.restoreVertexObjects();
        this.glsaver.restoreCommonGlStates();
    }

    private String getShaderSource(String name) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new IOException();
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load shader file: " + name, e);
        }
    }

    private PipeInstancedRenderer() {
        this.glsaver = new OpenGLSaver();
        // TODO: batch rendering to always fit inside the buffer
        // TODO: try indirect rendering, watch vid by that lady about it first
        // TODO: when using indirect rendering we can pass chunk coords in SSBO and use 4 bit per coord (0-15) here
        this.vertexBuffer = BufferUtils.createByteBuffer(20480 * (PipeQuad.POS_COUNT * Float.BYTES + 1)); // + 1 byte for texture id
        // Using 1/4 of maximum uniform size here (4kB), can extend if really have to
        this.textureBuffer = BufferUtils.createFloatBuffer(ModBlocks.PIPES.size() * 4); // 4 texture uv bounds
        this.textureIDs = new Object2ByteArrayMap<>();

        this.program = glCreateProgram();
        if (this.program == 0) {
            throw new RuntimeException("Could not create shader program");
        }

        int vertShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShaderID, getShaderSource("assets/" + Tags.MOD_ID + "/shaders/pipe.vert"));
        glCompileShader(vertShaderID);

        if (glGetShaderi(vertShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Could not compile vertex shader: " + glGetShaderInfoLog(vertShaderID, 1024));
        }
        glAttachShader(this.program, vertShaderID);

        int fragShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShaderID, getShaderSource("assets/" + Tags.MOD_ID + "/shaders/pipe.frag"));
        glCompileShader(fragShaderID);

        if (glGetShaderi(fragShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Could not compile fragment shader: " + glGetShaderInfoLog(fragShaderID, 1024));
        }
        glAttachShader(this.program, fragShaderID);

        glLinkProgram(this.program);
        if (glGetProgrami(this.program, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Could not link shader program: " + glGetProgramInfoLog(this.program, 1024));
        }

        glDetachShader(this.program, vertShaderID);
        glDeleteShader(vertShaderID);
        glDetachShader(this.program, fragShaderID);
        glDeleteShader(fragShaderID);

        final String projMatrixUniformName = "projMatrix";
        this.projMatrixUniformLoc = glGetUniformLocation(this.program, projMatrixUniformName);
        if (this.projMatrixUniformLoc < 0) {
            throw new RuntimeException("Could not create uniform: " + projMatrixUniformName + " in shader program " + this.program);
        }

        final String viewMatrixUniformName = "viewMatrix";
        this.viewMatrixUniformLoc = glGetUniformLocation(this.program, viewMatrixUniformName);
        if (this.viewMatrixUniformLoc < 0) {
            throw new RuntimeException("Could not create uniform: " + viewMatrixUniformName + " in shader program " + this.program);
        }

        final String texBufferUniformName = "textureBuffer";
        this.texBufferUniformLoc = glGetUniformLocation(this.program, texBufferUniformName);
        if (this.texBufferUniformLoc < 0) {
            throw new RuntimeException("Could not create uniform: " + texBufferUniformName + " in shader program " + this.program);
        }

        this.glsaver.storeVertexObjects();

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        int quadCount = PipeModelManager.BASE_INSTANCE_COUNT * PipeModelManager.QUAD_COUNT;
        ByteBuffer baseInstances = BufferUtils.createByteBuffer(quadCount * PipeQuad.VERTEX_COUNT * (1 + PipeQuad.POS_COUNT * Float.BYTES));
        for (byte i = 0; i < PipeModelManager.BASE_INSTANCE_COUNT; i++) {
            for (Map.Entry<EnumFacing, PipeQuad> entry : PipeModelManager.getQuadsForPipe(i).entrySet()) {
                byte faceIndex = (byte) entry.getKey().getIndex();
                PipeQuad quad = entry.getValue();
                for (int j = 0; j < 4; j++) {
                    baseInstances.putFloat(quad.xs[j]);
                    baseInstances.putFloat(quad.ys[j]);
                    baseInstances.putFloat(quad.zs[j]);
                    baseInstances.put(faceIndex);
                }
            }
        }
        baseInstances.flip();
        this.baseInstancevbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.baseInstancevbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 1 + 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(1, 1, GL_BYTE, 1 + 3 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBufferData(GL_ARRAY_BUFFER, baseInstances, GL_STATIC_DRAW);

        this.instvbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.instvbo);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 1 + 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(2);
        glVertexAttribDivisor(2, 1);
        glVertexAttribIPointer(3, 1, GL_BYTE, 1 + 3 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1);

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(quadCount * PipeQuad.INDEX_COUNT);
        for (int quadIndex = 0; quadIndex < quadCount; quadIndex++) {
            int baseVertex = quadIndex * PipeQuad.VERTEX_COUNT;
            indexBuffer.put(baseVertex);
            indexBuffer.put(baseVertex + 1);
            indexBuffer.put(baseVertex + 2);
            indexBuffer.put(baseVertex);
            indexBuffer.put(baseVertex + 2);
            indexBuffer.put(baseVertex + 3);
        }
        indexBuffer.flip();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        this.glsaver.restoreVertexObjects();
    }

    @SubscribeEvent
    public void onTextureStitchPost(TextureStitchEvent.Post event) {
        this.reloadTextureBuffer(event.getMap());
    }

    public void reloadTextureBuffer(TextureMap textureMap) {
        this.textureBuffer.clear();
        for (byte i = 0; i < ModBlocks.PIPES.size(); i++) {
            TextureAtlasSprite tex = textureMap.getAtlasSprite(ModBlocks.PIPES.get(i).getTexture().toString());
            this.textureBuffer.put(tex.getMinU());
            this.textureBuffer.put(tex.getMinV());
            this.textureBuffer.put(tex.getMaxU());
            this.textureBuffer.put(tex.getMaxV());
            this.textureIDs.put(tex, i);
        }
        this.textureBuffer.flip();

        this.glsaver.storeProgram();
        glUseProgram(this.program);
        glUniform4(this.texBufferUniformLoc, this.textureBuffer);
        this.glsaver.restoreProgram();
    }

    public static void initInstance() {
        if (instance == null) {
            instance = new PipeInstancedRenderer();
        }
    }

}
