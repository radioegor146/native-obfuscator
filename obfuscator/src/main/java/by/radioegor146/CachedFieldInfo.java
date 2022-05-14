package by.radioegor146;

import java.util.Objects;

public class CachedFieldInfo {

    private final String clazz;
    private final String name;
    private final String desc;
    private final boolean isStatic;

    public CachedFieldInfo(String clazz, String name, String desc, boolean isStatic) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.isStatic = isStatic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedFieldInfo that = (CachedFieldInfo) o;
        return isStatic == that.isStatic &&
                clazz.equals(that.clazz) &&
                name.equals(that.name) &&
                desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, name, desc, isStatic);
    }
}
