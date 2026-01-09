package com.chevstrap.rbx.Models;

import java.lang.reflect.Method;

public class MethodPair {
    public final Method getMethod;
    public final Method setMethod;

    public MethodPair(Method getMethod, Method setMethod) {
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }
}
