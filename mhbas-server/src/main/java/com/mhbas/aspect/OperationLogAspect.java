package com.mhbas.aspect;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class OperationLogAspect {
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Around("@annotation(operationLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        String uri = currentUri();
        Long userId = BaseContext.getCurrentId();
        try {
            Object result = joinPoint.proceed();
            log.info("operation={} user={} uri={} method={} elapsed={}ms status=success",
                    operationLog.value(), userId, uri, joinPoint.getSignature().getName(),
                    System.currentTimeMillis() - start);
            return result;
        } catch (Throwable throwable) {
            log.warn("operation={} user={} uri={} method={} elapsed={}ms status=failed reason={}",
                    operationLog.value(), userId, uri, joinPoint.getSignature().getName(),
                    System.currentTimeMillis() - start, throwable.getMessage());
            throw throwable;
        }
    }

    private String currentUri() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getMethod() + " " + request.getRequestURI();
        }
        return "-";
    }
}
