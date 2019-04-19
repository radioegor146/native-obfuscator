/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

import java.util.Objects;

/**
 *
 * @author radioegor146
 */
public class CachedFieldInfo {
    public String clazz;
    public String name;
    public String desc;
    public boolean isStatic;
    
    public CachedFieldInfo(String clazz, String name, String desc, boolean isStatic) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.isStatic = isStatic;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CachedFieldInfo other = (CachedFieldInfo) obj;
        if (!Objects.equals(this.clazz, other.clazz)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.desc, other.desc)) {
            return false;
        }
        return Objects.equals(this.isStatic, other.isStatic);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.clazz);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.desc);
        hash = 97 * hash + Objects.hashCode(this.isStatic);
        return hash;
    }
}
