package com.mhbas.context;

import com.mhbas.exception.BusinessException;

public final class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long userId) {
        threadLocal.set(userId);
    }

    /** 可能为 null：业务入口使用，缺失则默认当前用户无权限 */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /** 必定非 null：业务入口使用，缺失即 401 */
    public static Long requireCurrentId() {
        Long userId = getCurrentId();
        if (userId == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return userId;
    }

    /** 移除当前用户：业务出口使用 */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
