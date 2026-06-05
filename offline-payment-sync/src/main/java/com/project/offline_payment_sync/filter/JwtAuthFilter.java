

package com.project.offline_payment_sync.filter;

import com.project.offline_payment_sync.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService,
                         UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        //  Handle CORS preflight OPTIONS requests
        // These must pass through WITHOUT authentication
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("\n [CORS PREFLIGHT] OPTIONS request - Allowing through");
            System.out.println("   Origin: " + request.getHeader("Origin"));
            System.out.println("   Requested Method: " + request.getHeader("Access-Control-Request-Method"));
            
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();
        
        System.out.println("\n========== JWT FILTER ==========");
        System.out.println(" Path: " + path);
        System.out.println(" Method: " + request.getMethod());
        System.out.println(" Remote IP: " + request.getRemoteAddr());

        // Skip JWT validation for auth endpoints
        if (path.startsWith("/auth")) {
            System.out.println(" Public endpoint (/auth/**) - Skipping JWT validation");
            System.out.println("========== PROCEEDING ==========\n");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println(" Authorization Header: " + (authHeader != null ? "PRESENT " : "NULL "));
        
        if (authHeader != null) {
            System.out.println("   Header starts with 'Bearer': " + authHeader.startsWith("Bearer "));
            if (authHeader.startsWith("Bearer ")) {
                System.out.println("   Token length: " + (authHeader.length() - 7) + " characters");
            }
        }

        String token = null;
        String email = null;

        // Extract token from header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println(" Bearer token extracted");
            System.out.println("   First 50 chars: " + token.substring(0, Math.min(50, token.length())) + "...");
            
            // Check token expiration
            boolean isExpired = jwtService.isTokenExpired(token);
            System.out.println("Token Expired: " + (isExpired ? "YES " : "NO "));
            
            if (isExpired) {
                System.out.println(" REJECTED: Token has expired");
                returnErrorResponse(response, 
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token has expired. Please login again.");
                return;
            }

            // Extract email from token
            try {
                email = jwtService.extractUsername(token);
                System.out.println("Email extracted: " + email);
            } catch (Exception e) {
                System.out.println(" Failed to extract email: " + e.getMessage());
                returnErrorResponse(response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid JWT token.");
                return;
            }
        } else {
            System.out.println(" No Bearer token in Authorization header");
            System.out.println(" This request will fail with 403 Forbidden");
            returnErrorResponse(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Missing or invalid Authorization header. Expected format: 'Bearer <token>'");
            return;
        }

        // Validate and set authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("\n EMAIL FOUND - Validating authentication...");

            try {
                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println(" User found in database: " + userDetails.getUsername());

                // Validate token
                boolean isValid = jwtService.isTokenValid(token);
                System.out.println(" Token valid: " + isValid);

                if (isValid) {
                    // Extract role from JWT (with built-in null safety)
                    String roleFromToken = jwtService.extractRole(token);
                    System.out.println("👤 Role from token: " + roleFromToken);
                    
                    // Double-check role is not null
                    if (roleFromToken == null || roleFromToken.trim().isEmpty()) {
                        System.out.println(" Role is null/empty - defaulting to ROLE_USER");
                        roleFromToken = "ROLE_USER";
                    }
                    
                    // Create authority from role
                    List<SimpleGrantedAuthority> authorities = 
                            Collections.singletonList(new SimpleGrantedAuthority(roleFromToken));
                    System.out.println(" Authority: " + roleFromToken);

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println(" Authentication set in SecurityContext");
                    System.out.println("   Principal: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    System.out.println("   Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

                } else {
                    System.out.println(" Token validation FAILED");
                    returnErrorResponse(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "JWT token is invalid.");
                    return;
                }

            } catch (Exception e) {
                System.out.println(" Exception during authentication: " + e.getMessage());
                e.printStackTrace();
                returnErrorResponse(response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "User not found or authentication failed: " + e.getMessage());
                return;
            }
        } else {
            if (email == null) {
                System.out.println(" Email is null - Invalid token or no token provided");
            } else {
                System.out.println("ℹ Authentication already set in SecurityContext");
            }
        }

        System.out.println("========== PROCEEDING ==========\n");
        filterChain.doFilter(request, response);
    }

    // Helper method to send error responses
    private void returnErrorResponse(HttpServletResponse response, 
                                     int status, 
                                     String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
            "{\"error\": \"%s\", \"status\": %d, \"timestamp\": %d}",
            message, status, System.currentTimeMillis()
        ));
    }
}