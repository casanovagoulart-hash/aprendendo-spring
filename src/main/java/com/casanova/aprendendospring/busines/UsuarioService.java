package com.casanova.aprendendospring.busines;

import com.casanova.aprendendospring.controller.dtos.EnderecoCreateDTO;
import com.casanova.aprendendospring.controller.dtos.TelefoneCreateDTO;
import com.casanova.aprendendospring.controller.dtos.UsuarioCreateDTO;
import com.casanova.aprendendospring.controller.dtos.UsuarioResponseDTO;
import com.casanova.aprendendospring.infrastructure.entity.Endereco;
import com.casanova.aprendendospring.infrastructure.entity.Telefone;
import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import com.casanova.aprendendospring.infrastructure.exceptions.ConflictException;
import com.casanova.aprendendospring.infrastructure.exceptions.ResourceNotFoundException;
import com.casanova.aprendendospring.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Recebe um DTO de entrada (nunca a entidade diretamente) e devolve um
     * DTO de saída (nunca a entidade diretamente). Isso evita dois problemas:
     * 1) Mass assignment: o cliente não consegue setar campos como "id"
     *    que não deveriam vir de fora.
     * 2) Vazamento de dados sensíveis: a senha (mesmo já hasheada) nunca
     *    volta na resposta, porque UsuarioResponseDTO simplesmente não tem
     *    esse campo.
     */
    @Transactional
    public UsuarioResponseDTO salvaUsuario(UsuarioCreateDTO usuarioCreateDTO) {
        if (emailExiste(usuarioCreateDTO.getEmail())) {
            throw new ConflictException("Email já cadastrado!");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioCreateDTO.getNome());
        usuario.setEmail(usuarioCreateDTO.getEmail());
        // O hash da senha acontece aqui dentro, na camada de negócio - nunca
        // no Controller e nunca em um DTO, para manter essa regra centralizada.
        usuario.setSenha(passwordEncoder.encode(usuarioCreateDTO.getSenha()));

        usuario.setEnderecos(mapEnderecos(usuarioCreateDTO.getEnderecos()));
        usuario.setTelefones(mapTelefones(usuarioCreateDTO.getTelefones()));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // Construímos o DTO de resposta AINDA dentro da transação (@Transactional
        // nesse método), assim como você já fazia em buscaUsuarioPorEmail - isso
        // evita LazyInitializationException ao acessar enderecos/telefones.
        return new UsuarioResponseDTO(usuarioSalvo);
    }

    private List<Endereco> mapEnderecos(List<EnderecoCreateDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(dto -> {
                    Endereco endereco = new Endereco();
                    endereco.setRua(dto.getRua());
                    endereco.setNumero(dto.getNumero());
                    endereco.setBairro(dto.getBairro());
                    endereco.setCidade(dto.getCidade());
                    endereco.setCep(dto.getCep());
                    // Relacionamento é unidirecional (@OneToMany + @JoinColumn em
                    // Usuario, sem mappedBy) - Endereco não tem campo de volta para
                    // Usuario, então não há nada a setar aqui. O Hibernate resolve
                    // a coluna usuario_id sozinho ao salvar a lista dentro de Usuario.
                    return endereco;
                })
                .toList();
    }

    private List<Telefone> mapTelefones(List<TelefoneCreateDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(dto -> {
                    Telefone telefone = new Telefone();
                    telefone.setNumero(dto.getNumero());
                    // Mesma observação do endereço: relacionamento unidirecional,
                    // nada a setar em Telefone.
                    return telefone;
                })
                .toList();
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