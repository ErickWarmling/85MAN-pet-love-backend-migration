// java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Funcionario;
import com.pet_love.demo.model.dto.FuncionarioDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FuncionarioServiceUnitTest {

    @Test
    public void convertToDTO_mapeiaCamposCorretamente() {
        Funcionario f = new Funcionario();
        f.setId(1L);
        f.setNome("João");
        f.setCpf("123.456.789-00");
        f.setCidade("São Paulo");
        f.setTelefone("11-99999-0000");
        f.setEmail("joao@example.com");
        f.setCrmv("CRMV-001");
        f.setFuncao("Veterinário");

        FuncionarioDTO dto = FuncionarioService.convertToDTO(f);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("João", dto.getNome());
        assertEquals("123.456.789-00", dto.getCpf());
        assertEquals("São Paulo", dto.getCidade());
        assertEquals("11-99999-0000", dto.getTelefone());
        assertEquals("joao@example.com", dto.getEmail());
        assertEquals("CRMV-001", dto.getCrmv());
        assertEquals("Veterinário", dto.getFuncao());
    }

    @Test
    public void convertFromDTO_mapeiaCamposCorretamente() {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(2L);
        dto.setNome("Maria");
        dto.setCpf("987.654.321-00");
        dto.setCidade("Rio de Janeiro");
        dto.setTelefone("21-88888-1111");
        dto.setEmail("maria@example.com");
        dto.setCrmv("CRMV-002");
        dto.setFuncao("Recepcionista");

        Funcionario f = FuncionarioService.convertFromDTO(dto);

        assertNotNull(f);
        assertEquals(2L, f.getId());
        assertEquals("Maria", f.getNome());
        assertEquals("987.654.321-00", f.getCpf());
        assertEquals("Rio de Janeiro", f.getCidade());
        assertEquals("21-88888-1111", f.getTelefone());
        assertEquals("maria@example.com", f.getEmail());
        assertEquals("CRMV-002", f.getCrmv());
        assertEquals("Recepcionista", f.getFuncao());
    }
}