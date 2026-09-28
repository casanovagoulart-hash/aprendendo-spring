package com.casanova.aprendendospring.controller;

import com.casanova.aprendendospring.busines.UsuarioService;
import com.casanova.aprendendospring.controller.dtos.UsuarioCreateDTO;
import com.casanova.aprendendospring.controller.dtos.UsuarioDTO;
import com.casanova.aprendendospring.controller.dtos.UsuarioResponseDTO;
import com.casanova.aprendendospring.infrastructure.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService,
                             AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Antes: recebia e devolvia a entidade Usuario diretamente.
     * Agora: recebe UsuarioCreateDTO (só os campos que o cliente pode
     * enviar) e devolve UsuarioResponseDTO (só os campos que o cliente
     * pode ver) - a senha/hash nunca trafega na resposta.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> salvaUsuario(@RequestBody UsuarioCreateDTO usuarioCreateDTO) {
        return ResponseEntity.ok(usuarioService.salvaUsuario(usuarioCreateDTO));
    }

    @PostMapping("/login")
    public String login(@RequestBody UsuarioDTO usuarioDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usuarioDTO.getEmail(), usuarioDTO.getSenha())
        );
        return "Bearer " + jwtUtil.generateToken(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> buscaUsuarioPorEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(usuarioService.buscaUsuarioPorEmail(email));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deletaUsuarioPorEmail(@PathVariable String email) {
        usuarioService.deletaUsuarioPorEmail(email);
        return ResponseEntity.ok().build();
    }
}