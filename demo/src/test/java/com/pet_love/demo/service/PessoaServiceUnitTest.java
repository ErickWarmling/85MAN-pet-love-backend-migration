// java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Pessoa;
import com.pet_love.demo.model.dto.PessoaDTO;
import com.pet_love.demo.model.PessoaPet;
import com.pet_love.demo.model.Pet;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class PessoaServiceUnitTest {

    @Test
    public void convertToDTO_mapeiaCamposCorretamente() {
        // Configurando os pets
        Pet pet1 = new Pet();
        pet1.setId(101L);

        Pet pet2 = new Pet();
        pet2.setId(102L);

        PessoaPet pp1 = new PessoaPet();
        pp1.setPet(pet1);

        PessoaPet pp2 = new PessoaPet();
        pp2.setPet(pet2);

        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("Lucas");
        pessoa.setCpf("12345678900");
        pessoa.setCidade("Recife");
        pessoa.setTelefone("81999990000");
        pessoa.setEmail("lucas@example.com");
        pessoa.setPets(Arrays.asList(pp1, pp2));

        PessoaDTO dto = PessoaService.convertToDTO(pessoa);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Lucas", dto.getNome());
        assertEquals("12345678900", dto.getCpf());
        assertEquals("Recife", dto.getCidade());
        assertEquals("81999990000", dto.getTelefone());
        assertEquals("lucas@example.com", dto.getEmail());
        assertNotNull(dto.getPetsIds());
        assertEquals(2, dto.getPetsIds().size());
        assertTrue(dto.getPetsIds().contains(101L));
        assertTrue(dto.getPetsIds().contains(102L));
    }

    @Test
    public void convertFromDTO_mapeiaCamposCorretamente() {
        PessoaDTO dto = new PessoaDTO();
        dto.setId(2L);
        dto.setNome("Maria");
        dto.setCpf("98765432100");
        dto.setCidade("São Paulo");
        dto.setTelefone("11988887777");
        dto.setEmail("maria@example.com");

        Pessoa pessoa = PessoaService.convertFromDTO(dto);

        assertNotNull(pessoa);
        assertEquals(2L, pessoa.getId());
        assertEquals("Maria", pessoa.getNome());
        assertEquals("98765432100", pessoa.getCpf());
        assertEquals("São Paulo", pessoa.getCidade());
        assertEquals("11988887777", pessoa.getTelefone());
        assertEquals("maria@example.com", pessoa.getEmail());
        // pets não são mapeados aqui
        assertEquals(0, pessoa.getPets().size());
    }
}
