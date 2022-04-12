package by.radioegor146;

import org.objectweb.asm.tree.LabelNode;

import java.util.List;
import java.util.Objects;

public class CatchesBlock {
    public static class CatchBlock {
        private final String clazz;
        private final LabelNode handler;

        public CatchBlock(String clazz, LabelNode handler) {
            this.clazz = clazz;
            this.handler = handler;
        }

        public String getClazz() {
            return clazz;
        }

        public LabelNode getHandler() {
            return handler;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CatchBlock that = (CatchBlock) o;
            return Objects.equals(clazz, that.clazz) && Objects.equals(handler, that.handler);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, handler);
        }
    }

    private final List<CatchBlock> catches;

    public CatchesBlock(List<CatchBlock> catches) {
        this.catches = catches;
    }

    public List<CatchBlock> getCatches() {
        return catches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatchesBlock that = (CatchesBlock) o;
        return Objects.equals(catches, that.catches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catches);
    }
}
