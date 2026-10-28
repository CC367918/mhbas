package com.mhbas.interceptor;

import com.mhbas.context.BaseContext;
import com.mhbas.properties.JwtProperties;
import com.mhbas.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    public JwtInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //1、从请求头中获取令牌
        String authorization = request.getHeader("Authorization");
        //2、判断令牌是否存在，如果不存在，返回401
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(response);
        }

        //3、解析令牌
        try {
            String token = authorization.substring(7).trim();
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecret(), token);
            BaseContext.setCurrentId(Long.valueOf(claims.getSubject()));
            return true;
        } catch (Exception exception) {
            BaseContext.removeCurrentId();
            return unauthorized(response);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        BaseContext.removeCurrentId();
    }

    private boolean unauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":401,\"message\":\"登录状态已失效，请重新登录\",\"data\":null}"
        );
        return false;
    }
}
