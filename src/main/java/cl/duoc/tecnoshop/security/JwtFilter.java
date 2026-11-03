
package cl.duoc.tecnoshop.security;

import cl.duoc.tecnoshop.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

 private final JwtService jwt;
 private final UsuarioRepository usuarios;

 public JwtFilter(JwtService jwt, UsuarioRepository usuarios) {
  this.jwt = jwt;
  this.usuarios = usuarios;
 }

 @Override
 protected void doFilterInternal(
         HttpServletRequest request,
         HttpServletResponse response,
         FilterChain chain
 ) throws ServletException, IOException {

  String header = request.getHeader("Authorization");

  if (header != null &&
          header.startsWith("Bearer ") &&
          SecurityContextHolder.getContext().getAuthentication() == null) {

   try {
    String email = jwt.subject(header.substring(7));

    usuarios.findByEmail(email).ifPresent(usuario -> {
     UsernamePasswordAuthenticationToken autenticacion =
             new UsernamePasswordAuthenticationToken(
                     usuario.getEmail(),
                     null,
                     List.of(new SimpleGrantedAuthority(
                             "ROLE_" + usuario.getRol().name()
                     ))
             );

     SecurityContextHolder.getContext()
             .setAuthentication(autenticacion);
    });

   } catch (Exception e) {
    SecurityContextHolder.clearContext();
   }
  }

  chain.doFilter(request, response);
 }
}
