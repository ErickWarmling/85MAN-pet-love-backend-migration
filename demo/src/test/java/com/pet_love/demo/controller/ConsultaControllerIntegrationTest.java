package com.pet_love.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet_love.demo.model.Consulta;
import com.pet_love.demo.model.Especie;
import com.pet_love.demo.model.Funcionario;
import com.pet_love.demo.model.Pet;
import com.pet_love.demo.model.dto.ConsultaDTO;
import com.pet_love.demo.repository.ConsultaRepository;
import com.pet_love.demo.repository.EspecieRepository;
import com.pet_love.demo.repository.FuncionarioRepository;
import com.pet_love.demo.repository.PetRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConsultaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pet pet1;
    private Pet pet2;
    private Funcionario vet1;
    private Funcionario vet2;
    private Consulta consulta;
    private Especie especie;

    @BeforeEach
    void setUp() {
        consultaRepository.deleteAll();
        petRepository.deleteAll();
        funcionarioRepository.deleteAll();
        especie = especieRepository.findAll().get(0);

        pet1 = new Pet(null, null, "2025-01-01", null, null, especie, null, null);
        pet1.setNome("Pet 1");
        pet1 = petRepository.save(pet1);

        pet2 = new Pet(null, null, "2025-01-01", null, null, especie, null, null);
        pet2.setNome("Pet 2");
        pet2 = petRepository.save(pet2);

        vet1 = new Funcionario();
        vet1.setNome("Vet 1");
        vet1.setCpf("9999-9999");
        vet1.setEmail("vet1@gmail.com");
        vet1 = funcionarioRepository.save(vet1);

        vet2 = new Funcionario();
        vet2.setNome("Vet 2");
        vet2.setCpf("8888-9999");
        vet2.setEmail("vet2@gmail.com");
        vet2 = funcionarioRepository.save(vet2);

        consulta = new Consulta();
        consulta.setDataHora(LocalDateTime.now());
        consulta.setObservacoes("Consulta teste");
        consulta.setValor(200.0);
        consulta.setPet(pet1);
        consulta.setFuncionario(vet1);
        consulta = consultaRepository.save(consulta);
    }

    // CT01 - Remover pet da consulta
    @Test
    void testRemoverPetDaConsulta_deveRetornarErro() throws Exception {
        var consulta = consultaRepository.findAll().get(0);

        var consultaDTO = new ConsultaDTO(
                consulta.getId(),
                consulta.getDataHora(),
                "Removendo pet",
                consulta.getValor(),
                consulta.getFuncionario().getId(),
                null // petId removido
        );

        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(put("/api/consultas/" + consulta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaDTO)))
        );

        assertTrue(ex.getMessage().contains("É necessário informar um pet para a consulta"));
    }

    // CT02 - Excluir pet vinculado a uma consulta
    @Test
    void testExcluirPetVinculadoAConsulta_deveRetornarErro() throws Exception {
        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(delete("/api/pets/" + pet1.getId()))
        );

        assertTrue(ex.getMessage().contains("Não é possível excluir o pet pois ele já está vinculado a uma consulta"));
    }

    // CT03 - Alterar pet da consulta e excluir pet anterior
    @Test
    void testAlterarPetDaConsultaEExcluirAnterior_devePermitirAlteracao() throws Exception {
        // Alterar para pet2
        ConsultaDTO consultaDTO = new ConsultaDTO(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getObservacoes(),
                consulta.getValor(),
                vet1.getId(), // mesmo veterinário
                pet2.getId()  // novo pet
        );

        // Atualizar consulta
        mockMvc.perform(put("/api/consultas/" + consulta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petId").value(pet2.getId()));

        // Tentar excluir o pet antigo (que não está mais associado)
        mockMvc.perform(delete("/api/pets/" + pet1.getId()))
                .andExpect(status().isNoContent());
    }

    // CT05 - Excluir veterinário vinculado a uma consulta
    @Test
    void testExcluirVeterinarioVinculadoAConsulta_deveRetornarErro() throws Exception {
        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(delete("/api/funcionarios/" + vet1.getId()))
        );

        assertTrue(ex.getMessage().contains("Não é possível excluir o funcionário pois ele já está vinculado a uma consulta"));
    }

    // CT04 - Remover veterinário da consulta (veterinário = null)
    @Test
    void testRemoverVeterinarioDaConsulta_deveRetornarErro() throws Exception {
        ConsultaDTO consultaDTO = new ConsultaDTO(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getObservacoes(),
                consulta.getValor(),
                null, // removendo veterinário
                pet1.getId()
        );

        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(put("/api/consultas/" + consulta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaDTO)))
        );

        assertTrue(ex.getMessage().contains("É necessário informar um veterinário para a consulta"));
    }

    // CT06 - Alterar veterinário da consulta e excluir veterinário anterior
    @Test
    void testAlterarVeterinarioDaConsultaEExcluirAnterior_devePermitirAlteracao() throws Exception {
        // Alterar para vet2
        ConsultaDTO consultaDTO = new ConsultaDTO(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getObservacoes(),
                consulta.getValor(),
                vet2.getId(), // novo veterinário
                pet1.getId()
        );

        // Atualizar consulta
        mockMvc.perform(put("/api/consultas/" + consulta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionarioId").value(vet2.getId()));

        // Tentar excluir o veterinário antigo (não associado agora)
        mockMvc.perform(delete("/api/funcionarios/" + vet1.getId()))
                .andExpect(status().isNoContent());
    }
}
