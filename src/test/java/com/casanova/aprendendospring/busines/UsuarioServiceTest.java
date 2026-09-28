package com.casanova.aprendendospring.busines;

import com.casanova.aprendendospring.controller.dtos.UsuarioCreateDTO;
import com.casanova.aprendendospring.controller.dtos.UsuarioResponseDTO;
import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import com.casanova.aprendendospring.infrastructure.exceptions.ConflictException;
import com.casanova.aprendendospring.infrastructure.exceptions.ResourceNotFoundException;
import com.casanova.aprendendospring.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Teste unitário (não sobe o Spring, não toca no PostgreSQL de verdade).
 *
 * @ExtendWith(MockitoExtension.class) liga o Mockito ao JUnit 5.
 * @Mock cria dublês (fakes) das dependências - aqui não existe banco real,
 * apenas objetos que "fingem" ser UsuarioRepository e PasswordEncoder.
 * @InjectMocks cria uma instância real de UsuarioService e injeta os @Mock
 * acima nos campos correspondentes (equivalente ao que o Spring faria,
 * mas manualmente e sem subir o contexto todo).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void salvaUsuario_deveLancarConflictException_quandoEmailJaExiste() {
        // ARRANGE: simula que já existe um usuário com esse email no banco
        String email = "existente@teste.com";
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .nome("Fulano")
                .email(email)
                .senha("senha123")
                .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(new Usuario()));

        // ACT + ASSERT: chamar salvaUsuario deve lançar ConflictException,
        // e o fluxo deve parar ali - nunca deve tentar salvar nem codificar a senha.
        assertThatThrownBy(() -> usuarioService.salvaUsuario(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void salvaUsuario_deveCodificarSenhaESalvar_quandoEmailNaoExiste() {
        // ARRANGE
        String emailNovo = "novo@teste.com";
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .nome("Ciclano")
                .email(emailNovo)
                .senha("senhaEmTextoPuro")
                .enderecos(List.of())
                .telefones(List.of())
                .build();

        when(usuarioRepository.findByEmail(emailNovo)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senhaEmTextoPuro")).thenReturn("HASH_FALSO_DE_TESTE");

        // Simula o que o banco faria: devolve a mesma entidade recebida, já com um id.
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioRecebido = invocation.getArgument(0);
            usuarioRecebido.setId(1L);
            return usuarioRecebido;
        });

        // ACT
        UsuarioResponseDTO resposta = usuarioService.salvaUsuario(dto);

        // ASSERT: a resposta tem os dados esperados...
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getNome()).isEqualTo("Ciclano");
        assertThat(resposta.getEmail()).isEqualTo(emailNovo);

        // ...e, mais importante: UsuarioResponseDTO simplesmente não possui um
        // campo/getter de senha - ou seja, é estruturalmente impossível a senha
        // (ou o hash dela) vazar nessa resposta. Não há "getSenha()" para chamar aqui.

        // Captura o objeto Usuario que de fato foi passado para o repository.save()
        // para confirmar que a senha foi codificada ANTES de ir para o banco,
        // nunca sendo salva em texto puro.
        ArgumentCaptor<Usuario> usuarioCapturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCapturado.capture());
        assertThat(usuarioCapturado.getValue().getSenha()).isEqualTo("HASH_FALSO_DE_TESTE");

        verify(passwordEncoder).encode("senhaEmTextoPuro");
    }

    @Test
    void buscaUsuarioPorEmail_deveLancarResourceNotFoundException_quandoNaoEncontrado() {
        String email = "naoexiste@teste.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscaUsuarioPorEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(email);
    }

    @Test
    void buscaUsuarioPorEmail_deveRetornarDTO_quandoEncontrado() {
        String email = "encontrado@teste.com";
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNome("Beltrano");
        usuario.setEmail(email);
        usuario.setEnderecos(List.of());
        usuario.setTelefones(List.of());

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO resposta = usuarioService.buscaUsuarioPorEmail(email);

        assertThat(resposta.getId()).isEqualTo(5L);
        assertThat(resposta.getEmail()).isEqualTo(email);
    }

    @Test
    void deletaUsuarioPorEmail_deveChamarRepositorio() {
        String email = "deletar@teste.com";

        usuarioService.deletaUsuarioPorEmail(email);

        // Verifica que o Service realmente delega a exclusão para o repositório,
        // sem nenhuma lógica extra escondida aqui.
        verify(usuarioRepository).deleteByEmail(email);
    }
}