package br.com.linkzera.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.linkzera.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var token = request.getHeader("Authorization");

    if (token == null || token.isEmpty()) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token não informado!");
      return;
    }

    token = token.replace("Basic ", "");
    token = new String(Base64.getDecoder().decode(token));

    var username = token.split(":")[0];
    var password = token.split(":")[1];

    var user = this.userRepository.findByUsername(username);

    if (user == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não encontrado!");
      return;
    }

    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
    if (!passwordVerify.verified) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Senha inválida!");
      return;
    }

    request.setAttribute("user", user);

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
      String path = request.getServletPath();
      return path.startsWith("/users/");
  }

}

