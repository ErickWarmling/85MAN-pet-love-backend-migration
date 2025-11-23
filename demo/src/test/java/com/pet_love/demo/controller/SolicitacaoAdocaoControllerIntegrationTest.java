package com.pet_love.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet_love.demo.model.*;
import com.pet_love.demo.model.dto.SolicitacaoAdocaoDTO;
import com.pet_love.demo.repository.*;
import com.pet_love.demo.service.SolicitacaoAdocaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SolicitacaoAdocaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitacaoAdocaoRepository solicitacaoAdocaoRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PessoaPetRepository pessoaPetRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pet petSemDono;
    private Pessoa pessoaSolicitante;
    private Pessoa pessoaSolicitante2;
    private SolicitacaoAdocao solicitacaoAdocao;
    private SolicitacaoAdocao solicitacaoAdocao2;
    private Especie especie;

    @BeforeEach
    void setUp() {
        pessoaPetRepository.deleteAll();
        solicitacaoAdocaoRepository.deleteAll();
        pessoaRepository.deleteAll();
        petRepository.deleteAll();
        especie = especieRepository.findAll().get(0);

        petSemDono = new Pet();
        petSemDono.setNome("Pet 1");
        petSemDono.setDataNascimento("2025-10-03");
        petSemDono.setEspecie(especie);
        petSemDono = petRepository.save(petSemDono);

        pessoaSolicitante = new Pessoa();
        pessoaSolicitante.setNome("Dono 1");
        pessoaSolicitante.setCpf("953.374.520-70");
        pessoaSolicitante.setEmail("dono1@Gmail.com");
        pessoaSolicitante = pessoaRepository.save(pessoaSolicitante);

        pessoaSolicitante2 = new Pessoa();
        pessoaSolicitante2.setNome("Dono 2");
        pessoaSolicitante2.setCpf("177.642.650-98");
        pessoaSolicitante2.setEmail("dono2@gmail.com");
        pessoaSolicitante2 = pessoaRepository.save(pessoaSolicitante2);

        solicitacaoAdocao = new SolicitacaoAdocao();
        solicitacaoAdocao.setPet(petSemDono);
        solicitacaoAdocao.setPessoa(pessoaSolicitante);
        solicitacaoAdocao.setDataHora("21/11/2025");
        solicitacaoAdocao.setStatus(1);
        solicitacaoAdocao = solicitacaoAdocaoRepository.save(solicitacaoAdocao);

        solicitacaoAdocao2 = new SolicitacaoAdocao();
        solicitacaoAdocao2.setPet(petSemDono);
        solicitacaoAdocao2.setPessoa(pessoaSolicitante2);
        solicitacaoAdocao2.setDataHora("22/11/2025");
        solicitacaoAdocao2.setStatus(1);
        solicitacaoAdocao2 = solicitacaoAdocaoRepository.save(solicitacaoAdocao2);
    }

    // CT07 - Atualizar o dono do pet ao aprovar solicitação de adoção
    @Test
    void testAtualizarDonoPetAoAprovarSolicitacaoAdocao() throws Exception {
        SolicitacaoAdocaoDTO solicitacaoAdocaoDTO = SolicitacaoAdocaoService.convertToDTO(solicitacaoAdocao);
        solicitacaoAdocaoDTO.setStatus(2); // Status Aprovado

        mockMvc.perform(put("/api/solicitacao-adocao/" + solicitacaoAdocao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(solicitacaoAdocaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(2));

        // Verifica se foi criado o vínculo de dono principal
        Optional<PessoaPet> donoPrincipal = pessoaPetRepository.findByPetIdAndPrincipalTrue(petSemDono.getId());

        assertThat(donoPrincipal)
                .isPresent()
                .hasValueSatisfying(vinculo -> {
                    assertThat(vinculo.getPessoa().getId()).isEqualTo(pessoaSolicitante.getId());
                    assertThat(vinculo.isPrincipal()).isTrue();
                });
    }

    // CT08 - Manter pet sem dono ao reprovar uma solicitação de adoção
    @Test
    void testManterPetSemDonoAoRprovarUmaSolicitacaoAdocao() throws Exception{
        SolicitacaoAdocaoDTO solicitacaoAdocaoDTO = SolicitacaoAdocaoService.convertToDTO(solicitacaoAdocao);
        solicitacaoAdocaoDTO.setStatus(3); // Status Reprovado

        mockMvc.perform(put("/api/solicitacao-adocao/" + solicitacaoAdocao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitacaoAdocaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(3));

        SolicitacaoAdocao solicitacaoAdocaoReprovada = solicitacaoAdocaoRepository
                .findById(solicitacaoAdocao.getId())
                .orElseThrow();

        assertThat(solicitacaoAdocaoReprovada.getStatus()).isEqualTo(3);

        // O pet deve continuar sem dono principal
        Optional<PessoaPet> donoPrincipal = pessoaPetRepository
                .findByPetIdAndPrincipalTrue(petSemDono.getId());
        assertThat(donoPrincipal).isEmpty();

        // A pessoa com a solicitação de adoção reprovada não deve ter nenhum vínculo com o pet
        List<PessoaPet> vinculos = pessoaPetRepository.findByPessoaId(pessoaSolicitante.getId());
        assertThat(vinculos)
                .extracting(vinculo -> vinculo.getPet().getId())
                .doesNotContain(petSemDono.getId());
    }

    // CT09 - Aprovar solicitação de adoção enquanto outra está pendente para o mesmo pet
    @Test
    void testAprovarSolicitacaoAdocaoEnquantoOutraEstaPendente() throws Exception{
        SolicitacaoAdocaoDTO solicitacaoAdocaoDTO = SolicitacaoAdocaoService.convertToDTO(solicitacaoAdocao);
        solicitacaoAdocaoDTO.setStatus(2); // Status Aprovado

        mockMvc.perform(put("/api/solicitacao-adocao/" + solicitacaoAdocao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(solicitacaoAdocaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(2));

        // Verifica se o primeiro solicitante virou dono
        Optional<PessoaPet> donoPrincipal = pessoaPetRepository
                .findByPetIdAndPrincipalTrue(petSemDono.getId());

        assertThat(donoPrincipal)
                .isPresent()
                .hasValueSatisfying(vinculo -> assertThat(vinculo.getPessoa().getId())
                        .isEqualTo(pessoaSolicitante.getId()));

        // Verifica se a outra solicitação foi automaticamente reprovada
        SolicitacaoAdocao solicitacaoReprovada = solicitacaoAdocaoRepository
                .findById(solicitacaoAdocao2.getId())
                .orElseThrow();

        assertThat(solicitacaoReprovada.getStatus()).isEqualTo(3);

        // Verifica se o segundo solicitante não tem vínculo com o pet
        List<PessoaPet> vinculos = pessoaPetRepository
                .findByPessoaId(pessoaSolicitante2.getId());

        assertThat(vinculos)
                .extracting(vinculo -> vinculo.getPet().getId())
                .doesNotContain(petSemDono.getId());
    }

    // CT10 - Exclusão de dono com solicitação de adoção pendente
    @Test
    void testExclusaoDeDonoComSolicitacaoAdocaoPendente() throws Exception{
        mockMvc.perform(delete("/api/pessoas/" + pessoaSolicitante.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Não é possível excluir o dono pois ele está vinculado a uma solicitação de adoção"));
    }

    // CT11 - Exclusão de pet com solicitação de adoção pendente
    @Test
    void testExclusaoDePetComSolicitacaoAdocaoPendente() throws Exception{
        mockMvc.perform(delete("/api/pets/" + petSemDono.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Não é possível excluir o pet pois ele está vinculado a uma solicitação de adoção"));
    }
}