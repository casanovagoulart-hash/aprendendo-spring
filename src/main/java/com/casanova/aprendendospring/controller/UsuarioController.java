package com.casanova.aprendendospring.controller;

import com.casanova.aprendendospring.busines.UsuarioService;
import com.casanova.aprendendospring.controller.dtos.UsuarioDTO;
import com.casanova.aprendendospring.controller.dtos.UsuarioResponseDTO;
import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import com.casanova.aprendendospring.infrastructure.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// NOTA: esta classe é uma reconstrução mínima, já que o JwtUtil original
// não foi compartilhado. Ajuste para bater com a sua implementação real
// (o importante aqui é só a assinatura do método generateToken usada pelo controller).
@RestController
@RequestMapping("/usuarios") // ou o path base que fizer sentido
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

@PostMapping
public ResponseEntity<Usuario> salvaUsuario(@RequestBody Usuario usuario){
    return ResponseEntity.ok(usuarioService.salvaUsuario(usuario));
}

@PostMapping("/login")
    public String login(@RequestBody UsuarioDTO usuarioDTO){
        Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              usuarioDTO.getEmail(), usuarioDTO.getSenha())
    );
        return "Bearer " + jwtUtil.generateToken(authentication.getName());
}

    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> buscaUsuarioPorEmail(@RequestParam("email") String email){
        return ResponseEntity.ok(usuarioService.buscaUsuarioPorEmail(email));
    }

    public UsuarioController(UsuarioService usuarioService,
                             AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deletaUsuarioPorEmail(@PathVariable String email) {
        usuarioService.deletaUsuarioPorEmail(email);
        return ResponseEntity.ok().build();
    }
}