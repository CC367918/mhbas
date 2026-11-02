package com.mhbas.interceptor;

import com.mhbas.constant.RedisConstants;
import com.mhbas.context.BaseContext;
import com.mhbas.properties.JwtProperties;
import com.mhbas.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;

    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 预处理方法，在 Controller 方法调用之前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截 Controller 方法，静态资源等非 Controller handler 直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 预检请求不做鉴权
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = JwtUtil.resolveToken(request.getHeader(jwtProperties.getTokenName()));
        if(token==null){
            return unauthorized(response);
        }
        // 解析令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long userId = Long.valueOf(claims.get(RedisConstants.FIELD_USER_ID).toString());

            // JWT 合法不代表会话有效：需再校验 Redis，令牌被登出或空闲超时都会导致会话不存在
            String key = RedisConstants.LOGIN_USER_KEY + token;
            Object cachedUserId = stringRedisTemplate.opsForHash().get(key,RedisConstants.FIELD_USER_ID);
            if (cachedUserId == null) {
                return unauthorized(response);
            }
            // 交叉校验：会话属主必须与令牌属主一致，防止共用 Redis 实例或 key 前缀冲突导致脏数据放行
            if (!String.valueOf(userId).equals(cachedUserId.toString())) {
                return unauthorized(response);
            }
            // 滑动刷新会话有效期
            stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL_SECONDS, TimeUnit.SECONDS);
            BaseContext.setCurrentId(userId);

            return true;
        } catch (DataAccessException exception) {
            log.error("会话校验失败，Redis 不可用: key={}", RedisConstants.LOGIN_USER_KEY + token, exception);
            return serviceUnavailable(response);   // 503，避免误导用户重复登录
        } catch (Exception exception) {
            BaseContext.removeCurrentId();
            return unauthorized(response);         // 验签/类型转换失败 → 401
        }
    }

    /**
     * 完成请求处理，但在视图渲染之前执行
     * @param request
     * @param response
     * @param handler
     * @param exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        BaseContext.removeCurrentId();
    }

    //处理未授权
    private boolean unauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());   // 设置字符编码
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":401,\"message\":\"登录状态已失效，请重新登录\",\"data\":null}"
        );
        return false;
    }

    //处理服务不可用
    private boolean serviceUnavailable(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());   // 设置字符编码
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":503,\"message\":\"服务不可用，请稍后再试\",\"data\":null}"
        );
        return false;
    }
}
