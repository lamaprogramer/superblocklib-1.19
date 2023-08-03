package net.iamaprogrammer.superblocklib.util;

import net.minecraft.util.math.Vec3i;

public class Point extends Vec3i {

    public Point(int x, int y, int z) {
        super(x, y, z);
    }
    public Vec3i asVec() {
        return this;
    }
}
