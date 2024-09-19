package com.javaguides.BankingApp.security;

import com.javaguides.BankingApp.config.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;  // Inject JwtService

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String bearer = request.getHeader("Authorization");

        if (!jwtService.hasAuthorizationBearer(bearer)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Remove "Bearer " prefix to get the token
        String token = jwtService.getAccessToken(bearer);

        if (!jwtService.parseToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return;
        }

        printTokenAndClaims(token);


        setAuthenticationContext(token, request);
        filterChain.doFilter(request, response);
    }

    private void printTokenAndClaims(String token) {
        System.out.println("JWT Token: " + token);

        try {
            Claims claims = jwtService.extractAllClaims(token);
            System.out.println("Claims:");
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("Error extracting claims: " + e.getMessage());
        }
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        // Assuming 'role' is a List of Strings
        String roles = jwtService.getClaim(token, "role"); // Changed to match expected type
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (roles != null) {
           // for (String role : roles) {
                grantedAuthorities.add(new SimpleGrantedAuthority(roles));
                System.out.println("Role: " + roles);
            //}
        } else {
            // Log or handle the case when roles are not present in the token
            System.out.println("Roles are null or missing in the token.");
        }

        return grantedAuthorities;
    }



    private void setAuthenticationContext(String token, HttpServletRequest request) {
        // Use email instead of user_name
        UserDetails details = User.withUsername(String.valueOf(jwtService.getClaim(token, "email")))
                .password("password")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .authorities(getAuthorities(token)).build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
