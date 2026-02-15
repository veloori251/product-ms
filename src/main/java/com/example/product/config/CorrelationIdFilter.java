package com.example.product.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY= "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            // Get Correlation id from header
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            //Generate if not present
            if(correlationId == null || correlationId.isBlank()){
                correlationId = UUID.randomUUID().toString();
            }
            // put into MDC
            MDC.put(MDC_KEY,correlationId);
            // add header to response
            response.setHeader(CORRELATION_ID_HEADER,correlationId);
            //continue Request
            filterChain.doFilter(request,response);

        }finally {
            // CLEAR MDC to prevent memory leaks
            MDC.clear();
        }

    }
}
