package com.simon.common.config;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义AuthenticationEntryPoint实现类
 * @author simon
 * @create 2018-05-31 18:18
 **/
@Slf4j
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws  ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        log.error(authException.getMessage());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", response.getStatus());
        map.put("message", authException.getMessage());
        map.put("data", null);
        //map.put("path", request.getServletPath());
        //map.put("timestamp", String.valueOf(new Date().getTime()));

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), map);
        } catch (Exception e) {
            throw new ServletException();
        }
    }
}
