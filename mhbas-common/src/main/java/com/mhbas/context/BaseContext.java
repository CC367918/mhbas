package com.mhbas.context;

import com.mhbas.exception.BusinessException;

public final class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long userId) {
        threadLocal.set(userId);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static Long requireCurrentId() {
        Long userId = getCurrentId();
        if (userId == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return userId;
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
