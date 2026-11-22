package by.radioegor146;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassMethodFilterTest {

    @Test
    void whitelistCanSelectAnEntireClassOrOneMethod() {
        ClassNode owner = new ClassNode();
        owner.name = "example/Target";
        MethodNode first = new MethodNode();
        first.name = "first";
        first.desc = "()V";
        MethodNode second = new MethodNode();
        second.name = "second";
        second.desc = "()V";
        owner.methods.add(first);
        owner.methods.add(second);

        ClassMethodList firstOnly = ClassMethodList.parse(Collections.singletonList("example/Target#first!()V"));
        ClassMethodFilter methodFilter = new ClassMethodFilter(null, firstOnly, false);
        assertTrue(methodFilter.shouldProcess(owner));
        assertTrue(methodFilter.shouldProcess(owner, first));
        assertFalse(methodFilter.shouldProcess(owner, second));

        ClassMethodList wholeClass = ClassMethodList.parse(Collections.singletonList("example/Target"));
        ClassMethodFilter classFilter = new ClassMethodFilter(firstOnly, wholeClass, false);
        assertTrue(classFilter.shouldProcess(owner));
        assertFalse(classFilter.shouldProcess(owner, first));
        assertTrue(classFilter.shouldProcess(owner, second));
    }
}
