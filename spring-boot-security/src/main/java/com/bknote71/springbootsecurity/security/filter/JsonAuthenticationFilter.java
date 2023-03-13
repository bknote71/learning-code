package com.bknote71.springbootsecurity.security.filter;

import com.bknote71.springbootsecurity.security.MyUser;
import com.bknote71.springbootsecurity.security.token.MyAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonAuthenticationFilter(AuthenticationManager authenticationManager) {
        this("/login", authenticationManager);
    }

    protected JsonAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager) {
        // post only
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl, "POST"), authenticationManager);
        log.info("default Filter Process Url={}", defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        ServletInputStream inputStream = request.getInputStream();
        MyUser myUser = objectMapper.readValue(inputStream, MyUser.class);
        myUser.refine();
        MyAuthenticationToken authentication = MyAuthenticationToken.unauthenticated(myUser);
        return this.getAuthenticationManager().authenticate(authentication);
    }
}
