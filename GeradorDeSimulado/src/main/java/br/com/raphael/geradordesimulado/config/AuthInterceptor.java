package br.com.raphael.geradordesimulado.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();
        // Só protege rotas /admin/**
        if (uri.startsWith("/admin")) {
            HttpSession session = req.getSession(false);
            boolean logged = (session != null && session.getAttribute("USER_ID") != null);
            if (!logged) {
                res.sendRedirect("/login");
                return false;
            }
        }
        return true;
    }
}
