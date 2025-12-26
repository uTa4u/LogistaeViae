package su.uTa4u.logistaeviae.client.model;

// four points in order
// 3 2
// 0 1
public final class PipeQuad {
    public static final byte POS_0_00 = 0b00;
    public static final byte POS_0_25 = 0b01;
    public static final byte POS_0_75 = 0b10;
    public static final byte POS_1_00 = 0b11;

    public static final int VERTEX_COUNT = 4;
    public static final int POS_COUNT = 3;
    public static final int VERTEX_LENGHT = 5; // x, y, z, u, v
    public static final int[] INDICES = new int[]{0, 1, 3, 3, 1, 2};

    // Pipe quads only ever take one of 4 positions: 0, 0.25, 0.75, 1
    // Packed order: x3, x2, x1, x0
    public byte xs;
    public byte ys;
    public byte zs;
    //    final int[] colors;
    public final float[] us;
    public final float[] vs;
//    final int[] skyLights;
//    final int[] blockLights;

    PipeQuad(
            byte x0, byte y0, byte z0,
            byte x1, byte y1, byte z1,
            byte x2, byte y2, byte z2,
            byte x3, byte y3, byte z3
    ) {
        this.xs = setPos(x0, x1, x2, x3);
        this.ys = setPos(y0, y1, y2, y3);
        this.zs = setPos(z0, z1, z2, z3);
        this.us = new float[4];
        this.vs = new float[4];
    }

    public void texture(
            float u0, float v0,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3
    ) {
        this.us[0] = u0;
        this.us[1] = u1;
        this.us[2] = u2;
        this.us[3] = u3;
        this.vs[0] = v0;
        this.vs[1] = v1;
        this.vs[2] = v2;
        this.vs[3] = v3;
    }

//    public float[] pack(BlockPos pos, float cx, float cy, float cz) {
//        int x = pos.getX();
//        int y = pos.getY();
//        int z = pos.getZ();
//        return new float[]{
//                this.xs[0] + x - cx, this.ys[0] + y - cy, this.zs[0] + z - cz, this.us[0], this.vs[0],
//                this.xs[1] + x - cx, this.ys[1] + y - cy, this.zs[1] + z - cz, this.us[1], this.vs[1],
//                this.xs[2] + x - cx, this.ys[2] + y - cy, this.zs[2] + z - cz, this.us[2], this.vs[2],
//                this.xs[3] + x - cx, this.ys[3] + y - cy, this.zs[3] + z - cz, this.us[3], this.vs[3]
//        };
//    }

    public void putX(int index, byte value) {
        int index2 = index * 2;
        checkIndex(index2);
        checkValue(value);
        this.xs &= (byte) ~(0b11 << index2);
        this.xs |= (byte) ((value & 0b11) << index2);
    }

    public void putY(int index, byte value) {
        int index2 = index * 2;
        checkIndex(index2);
        checkValue(value);
        this.ys &= (byte) ~(0b11 << index2);
        this.ys |= (byte) ((value & 0b11) << index2);
    }

    public void putZ(int index, byte value) {
        int index2 = index * 2;
        checkIndex(index2);
        checkValue(value);
        this.zs &= (byte) ~(0b11 << index2);
        this.zs |= (byte) ((value & 0b11) << index2);
    }

    public float getX(int index) {
        int index2 = index * 2;
        checkIndex(index2);
        return ((this.xs >>> index2) & 1) * 0.25f + ((this.xs >>> (index2 + 1)) & 1) * 0.75f;
    }

    public float getY(int index) {
        int index2 = index * 2;
        checkIndex(index2);
        return ((this.ys >>> index2) & 1) * 0.25f + ((this.ys >>> (index2 + 1)) & 1) * 0.75f;
    }

    public float getZ(int index) {
        int index2 = index * 2;
        checkIndex(index2);
        return ((this.zs >>> index2) & 1) * 0.25f + ((this.zs >>> (index2 + 1)) & 1) * 0.75f;
    }

    private static void checkValue(byte value) {
        if (value != POS_0_00 && value != POS_0_25 && value != POS_0_75 && value != POS_1_00) {
            throw new IllegalArgumentException("Value must be one of the following: POS_0_00, POS_0_25, POS_0_75, POS_1_00");
        }
    }

    private static void checkIndex(int index) {
        if (index < 0 || index > 6) {
            throw new IllegalArgumentException("Index must be between 0 and 6");
        }
    }

    private static byte setPos(byte b1, byte b2, byte b3, byte b4) {
        byte result = 0;
        result |= (byte) (b1 << 0);
        result |= (byte) (b2 << 2);
        result |= (byte) (b3 << 4);
        result |= (byte) (b4 << 6);
        return result;
    }
}
