package com.codebattlearena.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object role = session.getAttribute("userRole");
                Object name = session.getAttribute("userName");
                Object id = session.getAttribute("userId");
                if (role != null) {
                    String r = role.toString();
                    var auth = new UsernamePasswordAuthenticationToken(
                            name != null ? name : (id != null ? id.toString() : "user"),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + r))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(req, res);
    }
}
