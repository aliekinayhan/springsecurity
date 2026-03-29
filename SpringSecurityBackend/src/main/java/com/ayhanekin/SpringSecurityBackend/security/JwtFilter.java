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


/*
in spring some filters can work couple times for same request
OncePerRequestFilter blocks it and makes for every request this class
going work for once
*/
@Component
public class JwtFilter extends OncePerRequestFilter {

    // for token transactions
    private final JwtService jwtService;
    // to reach out to db to find is there user we are seeking
    private final UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    /*
     every request comes to this method
     request --> we are going to read the headers
     response --> the answer we are going to send
     filterChain --> when we are done wee should let the other filters work via this
    */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*
         we read the Authorization header if there is no header, or it's not
         starts with "Bearer " it means there is no token
         filterChain.doFilter(request, response); means  go to the next filter
         and finish the process in here
        */
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         if we find token above we are parsing it to use below
         Authorization header stars with "Bearer " and this is 7 element
         what we need comes after that
        */
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        /*
         We are checking two things
         username != null --> do we took username successfully from token
         SecurityContextHolder.getContext().getAuthentication() == null -->
         for this request is there any authentication done before
         if the authentication done before no need to do again
        */
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // we load the user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // we are checking is the token valid
            if (jwtService.isTokenValid(token,username)) {

                /*
                we checked that the token is valid we brought the user
                from database but spring security doesn't know about those
                things because it's our code we didn't tell anything to spring
                about what's happening
                Spring security asks is this request authenticated?
                and to check it goes to the SecurityContext if it's empty 403
                and that's why we should declare that, the user has been authenticated
                with these three steps
                */
                /*
                We are building an authentication object in the format that Spring Security
                can understand via this object we are sending 3 things
                userDetails --> who is this user
                null --> password (it's unnecessary because token has been validated)
                userDetail.getAuthorities() --> user roles
                */
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                        );
                /*
                We are adding the additional information of request to this object
                its optional but best practice (ip address of request is residing here)
                */
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                /*
                And with these lines we are declaring that this request
                belongs to this user and this request has been authenticated
                at this point Spring Security will check the SecurityContext
                and will find the user and let him to use controllers
                */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
