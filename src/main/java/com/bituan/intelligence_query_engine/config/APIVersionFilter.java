package com.bituan.intelligence_query_engine.config;

import com.bituan.intelligence_query_engine.exception.BadRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class APIVersionFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    protected boolean shouldNotFilter (HttpServletRequest request) throws ServletException {
        String reqPath = request.getServletPath();

        return !reqPath.startsWith("/api/profiles");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("X-API-Version");

            String CURRENT_PROFILE_API_VERSION = "1";

            if (header == null || !header.equals(CURRENT_PROFILE_API_VERSION)) {
                throw new BadRequest("API version header required");
            }

            filterChain.doFilter(request, response);
        } catch (BadRequest e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}
