package su.uTa4u.logistaeviae.interfaces;

import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.List;

public interface CompiledChunkPipeProvider {

    List<TileEntityPipe> logistaeviae_getPipes();

    void logistaeviae_addPipes(TileEntityPipe pipe);
}
