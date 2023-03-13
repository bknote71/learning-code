package com.bknote71.springbootsecurity.config;

import ch.qos.logback.core.encoder.Encoder;
import com.bknote71.springbootsecurity.security.filter.JsonAuthenticationFilter;
import com.bknote71.springbootsecurity.security.provider.MyAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration {
    private static final String[] permitAllResources = {"/login", "/logout", "/h2-console/**"};
    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.authenticationProvider(myAuthenticationProvider(userDetailsService));
        return authenticationManagerBuilder.build();
    }

    @Bean
    public AuthenticationProvider myAuthenticationProvider(UserDetailsService userDetailsService) {
        return new MyAuthenticationProvider(userDetailsService, passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // httpSecurity config
    // return SecurityFilterChain bean
    // 어떤 필터들이 등록되는지 DelegatingFilterProxy --> FilterChainProxy 를 살펴보면 된다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.csrf().disable();
        http.cors().disable();
        http.authorizeRequests()
                .antMatchers(permitAllResources).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jsonAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public Filter jsonAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        JsonAuthenticationFilter jsonAuthenticationFilter = new JsonAuthenticationFilter(authenticationManager);
        return jsonAuthenticationFilter;
    }

}
