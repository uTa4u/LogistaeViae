package su.uTa4u.logistaeviae.block;

import net.minecraftforge.common.property.IUnlistedProperty;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

public final class ConnectionsProperty implements IUnlistedProperty<Byte> {
    private final String name;

    public ConnectionsProperty(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Byte value) {
        return 0 <= value && value < 64;
    }

    @Override
    public Class<Byte> getType() {
        return Byte.class;
    }

    @Override
    public String valueToString(Byte value) {
        return TileEntityPipe.unpackConnections(value).toString();
    }
}