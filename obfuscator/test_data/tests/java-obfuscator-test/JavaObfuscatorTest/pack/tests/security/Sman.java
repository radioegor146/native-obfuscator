package pack.tests.security;

import java.security.Permission;

public class Sman extends SecurityManager {
    public void checkPermission(Permission perm) {
        if (perm.getName().contains("exitVM"))
            throw new SecurityException("HOOKED");
    }
}
