package by.radioegor146.special;

import by.radioegor146.MethodContext;

public interface SpecialMethodProcessor {
    String preProcess(MethodContext context);
    void postProcess(MethodContext context);
}
