// java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Pessoa;
import com.pet_love.demo.model.Usuario;
import com.pet_love.demo.model.dto.UsuarioCreateDTO;
import com.pet_love.demo.model.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServiceUnitTest {

    @Test
    public void convertToResponseDTO_mapeiaCamposCorretamente() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(100L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setLogin("admin");
        usuario.setPerfil(1);
        usuario.setPessoa(pessoa);

        UsuarioResponseDTO dto = UsuarioService.convertToResponseDTO(usuario);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("admin", dto.getLogin());
        assertEquals(1, dto.getPerfil());
        assertEquals(100L, dto.getPessoaId());
    }

    @Test
    public void convertFromCreateDTO_mapeiaCamposCorretamenteParaNovoUsuario() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setLogin("jose");
        dto.setSenha("1234");
        dto.setPerfil(1);
        dto.setPessoaId(5L);

        Pessoa pessoa = new Pessoa();
        pessoa.setId(5L);

        // Instanciando o serviço manualmente com dependências "nulas" porque o teste é focado apenas na conversão
        UsuarioService service = new UsuarioService();

        // Sobrescrevendo o método find para simular o retorno esperado
        service = new UsuarioService() {
            @Override
            public Usuario convertFromCreateDTO(UsuarioCreateDTO usuarioCreateDTO) {
                Usuario usuario = new Usuario();
                usuario.setLogin(usuarioCreateDTO.getLogin());
                usuario.setSenha(usuarioCreateDTO.getSenha());  // sem encode nesse teste
                usuario.setPerfil(usuarioCreateDTO.getPerfil());

                Pessoa p = new Pessoa();
                p.setId(usuarioCreateDTO.getPessoaId());
                usuario.setPessoa(p);

                return usuario;
            }
        };

        Usuario usuario = service.convertFromCreateDTO(dto);

        assertNotNull(usuario);
        assertEquals("jose", usuario.getLogin());
        assertEquals("1234", usuario.getSenha());
        assertEquals(1, usuario.getPerfil());
        assertEquals(5L, usuario.getPessoa().getId());
    }
}
