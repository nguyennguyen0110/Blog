package fa.training.blog.exception.configuration;

import fa.training.blog.security.JwtAccessDenyHandler;
import fa.training.blog.security.JwtAuthenticationEntryPoint;
import fa.training.blog.security.JwtRequestFilter;
import fa.training.blog.security.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private JwtAccessDenyHandler jwtAccessDenyHandler;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception{
        return auth.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // We don't need CSRF for JWT token-base authentication
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/signin", "/signup", "/").permitAll()
                .antMatchers(HttpMethod.GET, "/post", "/comment").permitAll()
                .antMatchers("/user*", "/user/**").hasRole("ADMIN")
                .antMatchers("/post*", "/post/**").hasRole("ADMIN")
                .antMatchers("/comment*", "/comment/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                // Add exception handler for authenticate and access process. Because exceptions throw in these processes
                // cannot be caught by our controller (which will be sent to our "MyExceptionHandler" class
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDenyHandler)
                // Make sure to use stateless session: session won't be used to store user's state
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
