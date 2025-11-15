package com.pet_love.demo.service;

import com.pet_love.demo.model.*;
import com.pet_love.demo.model.dto.PetDTO;
import com.pet_love.demo.model.dto.PessoaPetDTO;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PetServiceUnitTest {

    @Test
    public void convertToDTO_mapeiaCamposCorretamente() {
        Pet pet = new Pet();
        pet.setId(10L);
        pet.setNome("Toby");
        pet.setDataNascimento("2017-03-10");
        pet.setObservacoes("Nenhuma");
        pet.setFoto("url");

        Especie especie = new Especie();
        especie.setId(2L);
        especie.setNome("Canino");
        pet.setEspecie(especie);

        Raca raca = new Raca();
        raca.setId(3L);
        raca.setNome("Beagle");
        pet.setRaca(raca);

        Pessoa p = new Pessoa(); p.setId(7L); p.setNome("Lia");
        PessoaPet pp = new PessoaPet();
        pp.setPessoa(p);
        pp.setPet(pet);
        pp.setPrincipal(true);

        pet.setDonos(List.of(pp));

        PetDTO dto = PetService.convertToDTO(pet);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertNotNull(dto.getEspecie());
        assertEquals(2L, dto.getEspecie().getId());
        assertNotNull(dto.getRaca());
        assertEquals(3L, dto.getRaca().getId());
        assertNotNull(dto.getDonos());
        assertEquals(1, dto.getDonos().size());
        assertEquals(7L, dto.getDonos().get(0).getPessoaId());
        assertTrue(dto.getDonos().get(0).isPrincipal());
    }

    @Test
    public void convertFromDTO_mapeiaCamposCorretamente() throws Exception {
        Especie especieDto = new Especie(); especieDto.setId(5L);
        Raca racaDto = new Raca(); racaDto.setId(6L);

        PetDTO dto = new PetDTO();
        dto.setId(null);
        dto.setNome("Nina");
        dto.setDataNascimento("2020-05-05");
        dto.setEspecie(especieDto);
        dto.setRaca(racaDto);

        PessoaPetDTO donoExistente = new PessoaPetDTO(); donoExistente.setPessoaId(20L); donoExistente.setPrincipal(true);
        PessoaPetDTO donoInexistente = new PessoaPetDTO(); donoInexistente.setPessoaId(21L); donoInexistente.setPrincipal(false);
        dto.setDonos(List.of(donoExistente, donoInexistente));

        Especie especie = new Especie(); especie.setId(5L);
        Raca raca = new Raca(); raca.setId(6L);
        Pessoa pessoaExistente = new Pessoa(); pessoaExistente.setId(20L);


        Pet pet = PetService.convertFromDTO(dto);

        assertNotNull(pet);
        assertNotNull(pet.getEspecie());
        assertEquals(5L, pet.getEspecie().getId());
        assertNotNull(pet.getRaca());
        assertEquals(6L, pet.getRaca().getId());
    }
}
