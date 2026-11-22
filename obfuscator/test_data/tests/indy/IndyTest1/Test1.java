import by.radioegor146.nativeobfuscator.Native;

import java.util.*;

@Native
public class Test1 {
    public static void main(String[] args) {
        List<String> l = new ArrayList<>();
        l.add("Red");
        l.add("Green");
        l.add("Blue");
        long lengthyColors = l.stream().filter(c -> c.length() > 3).count();
        System.out.println(lengthyColors);
    }
}
