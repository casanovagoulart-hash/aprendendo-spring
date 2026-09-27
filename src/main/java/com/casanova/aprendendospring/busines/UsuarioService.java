package com.casanova.aprendendospring.busines;

import com.casanova.aprendendospring.controller.dtos.UsuarioResponseDTO;
import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import com.casanova.aprendendospring.infrastructure.exceptions.ConflictException;
import com.casanova.aprendendospring.infrastructure.exceptions.ResourceNotFoundException;
import com.casanova.aprendendospring.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario salvaUsuario(Usuario usuario) {
        if (emailExiste(usuario.getEmail())) {
            throw new ConflictException("Email já cadastrado!");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    private boolean emailExiste(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscaUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email não encontrado! " + email));
        return new UsuarioResponseDTO(usuario); // acesso a enderecos/telefones acontece AQUI, dentro da transação
    }

    public void deletaUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }
}