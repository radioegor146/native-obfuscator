package by.radioegor146;

import org.objectweb.asm.Label;

import java.util.WeakHashMap;

public class LabelPool {

    private final WeakHashMap<Label, Long> labels = new WeakHashMap<>();
    private long currentIndex = 0;

    public String getName(Label label) {
        return "L" + this.labels.computeIfAbsent(label, addedLabel -> ++currentIndex);
    }
}
