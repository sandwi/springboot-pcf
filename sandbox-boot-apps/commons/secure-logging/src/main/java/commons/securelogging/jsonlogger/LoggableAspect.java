package commons.securelogging.jsonlogger;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
public class LoggableAspect {

    private final JsonLoggingWrapper jsonLogger;

    public LoggableAspect(JsonLoggingWrapper jsonLoggingWrapper) {
        this.jsonLogger = jsonLoggingWrapper;
    }

    @Around(
            "@annotation(commons.securelogging.jsonlogger.Loggable)")
    public Object logPost(ProceedingJoinPoint joinPoint) throws Throwable {
        Object requestBody = findRequestBody(joinPoint);
        Map<String, String> headers = findRequestHeaders(joinPoint);
        jsonLogger.log(requestBody, buildPath(), "request", headers);

        Object returnValue = joinPoint.proceed();

        if (returnValue != null) {
            jsonLogger.log(returnValue, buildPath(), "response", headers);
        }

        return returnValue;
    }

    private Object findRequestBody(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(RequestBody.class)) {
                    return joinPoint.getArgs()[i];
                }
            }
        }
        return null;
    }

    private Map<String, String> findRequestHeaders(ProceedingJoinPoint joinPoint) {
        Map<String, String> headers = new HashMap<>();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(RequestHeader.class)) {
                    RequestHeader header = (RequestHeader) annotation;
                    String key = header.value();
                    if (key.isEmpty()) {
                        key = header.name();
                    }
                    if (key.isEmpty()) {
                        try {
                            key = ((MethodSignature)joinPoint.getStaticPart().getSignature()).getParameterNames()[i];
                        } catch (Exception ignored) {
                            key = method.getParameters()[i].getName();
                        }
                    }
                    headers.put(key, String.valueOf(joinPoint.getArgs()[i]));
                    break;
                }
            }
        }
        return headers;
    }

    private String buildPath() {
        javax.servlet.http.HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String query = request.getQueryString() == null ? "" : request.getQueryString();
        if (StringUtils.isEmpty(query)) {
            return request.getRequestURI();
        } else {
            return request.getRequestURI() + "?" + query;
        }
    }
}

