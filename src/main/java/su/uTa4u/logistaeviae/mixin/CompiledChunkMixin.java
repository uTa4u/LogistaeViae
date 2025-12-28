package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import su.uTa4u.logistaeviae.interfaces.CompiledChunkPipeProvider;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.ArrayList;
import java.util.List;

@Mixin(CompiledChunk.class)
public abstract class CompiledChunkMixin implements CompiledChunkPipeProvider {

    @Unique
    private final List<TileEntityPipe> logistaeaviae_tileEntityPipes = new ArrayList<>();

    @Override
    public List<TileEntityPipe> logistaeviae_getPipes() {
        return this.logistaeaviae_tileEntityPipes;
    }

    @Override
    public void logistaeviae_addPipes(TileEntityPipe pipe) {
        this.logistaeaviae_tileEntityPipes.add(pipe);
    }
}
