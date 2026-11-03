package cl.duoc.tecnoshop.config;

import cl.duoc.tecnoshop.security.JwtFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filter(HttpSecurity http,JwtFilter jwt) throws Exception {
  return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(a->a
                  .requestMatchers("/api/v1/auth/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/error").permitAll()
                  .requestMatchers(HttpMethod.GET,"/api/v1/productos/**","/api/v1/categorias/**").permitAll()
                  .requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRADOR")
                  .requestMatchers(HttpMethod.GET,"/api/v1/boletas/**").hasAnyRole("ADMINISTRADOR","VENDEDOR","CLIENTE")
                  .requestMatchers(HttpMethod.POST,"/api/v1/boletas/**").hasAnyRole("ADMINISTRADOR","CLIENTE")
                  .requestMatchers("/api/v1/productos/**","/api/v1/categorias/**").hasRole("ADMINISTRADOR")
                  .anyRequest().authenticated())
          .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean CorsConfigurationSource corsConfigurationSource(){
  CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:5173","http://127.0.0.1:5173"));
  c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));
  UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
 }
}
