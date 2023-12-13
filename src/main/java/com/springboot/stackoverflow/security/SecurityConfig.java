package com.springboot.stackoverflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        jdbcUserDetailsManager.setUsersByUsernameQuery(
                "select email, password, is_active from users where email = ?"
        );

        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "select u.email, r.role from users u " +
                        "LEFT JOIN user_role ur on u.id = ur.user_id " +
                        "LEFT JOIN roles r on ur.role_id = r.id where u.email = ?"
        );
        return jdbcUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(config ->
                        config
                                .requestMatchers("/css/**", "/login**", "/signup**", "/"
                                        , "/processUser**").permitAll()
                                .requestMatchers("/createBadge").hasRole("admin")
                                .requestMatchers("/**").hasAnyRole("user", "admin")
                                .anyRequest().authenticated()
                )
                .formLogin(form ->
                        form
                                .loginPage("/login")
                                .loginProcessingUrl("/authenticateTheUser")
                                .successForwardUrl("/")
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .permitAll());
        return http.build();
    }
}
