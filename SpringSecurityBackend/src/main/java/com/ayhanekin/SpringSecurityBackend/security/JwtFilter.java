package com.ayhanekin.SpringSecurityBackend.security;

import com.ayhanekin.SpringSecurityBackend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// in spring some filters can work couple times for same request
// OncePerRequestFilter blocks it and makes for every request this class
// going work for once
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService; // for token transactions
    private final UserDetailsServiceImpl userDetailsService; // to reach out to db to find is there user we are seeking

    public JwtFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // every request comes to this method
    // request --> we are going to read the headers
    // response --> the answer we are going to send
    // filterChain --> when we are done wee should let the other filters work via this
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // we read the Authorization header if there is no header
        // or it's not starts with "Bearer " it means there is no token
        // filterChain.doFilter(request, response); means  go to the next filter
        // and finish the process in here
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // if we find token above we are parsing it to use below
        // Authorization header stars with "Bearer " and this is 7 element
        // what we need comes after that
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        // We are checking two things
        // username != null --> do we took username successfully from token
        //SecurityContextHolder.getContext().getAuthentication() == null --> for this request
        // is there any authentication done before
        // if the authentication done before no need to done again
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // we load the user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // we are checking is the token valid
            if (jwtService.isTokenValid(token,username)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
