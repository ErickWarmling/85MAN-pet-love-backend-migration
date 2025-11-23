// language: java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Consulta;
import com.pet_love.demo.model.Funcionario;
import com.pet_love.demo.model.Pet;
import com.pet_love.demo.model.dto.ConsultaDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConsultaServiceUnitTest {

    private ConsultaService consultaService;

    @BeforeEach
    void setup() {
        consultaService = new ConsultaService();
    }

    @Test
    void convertToDTO_mapeiaCamposCorretamente() {
        //CT01
        Funcionario func = new Funcionario();
        func.setId(10L);
        Pet pet = new Pet();
        pet.setId(20L);

        Consulta consulta = new Consulta();
        consulta.setId(1L);
        consulta.setDataHora(LocalDateTime.of(2025, 1, 2, 10, 30));
        consulta.setObservacoes("obs");
        consulta.setValor(123.45);
        consulta.setFuncionario(func);
        consulta.setPet(pet);

        // quando
        ConsultaDTO dto = ConsultaService.convertToDTO(consulta);

        // então
        assertEquals(1L, dto.getId());
        assertEquals(consulta.getDataHora(), dto.getDataHora());
        assertEquals("obs", dto.getObservacoes());
        assertEquals(123.45, dto.getValor());
        assertEquals(10L, dto.getFuncionarioId());
        assertEquals(20L, dto.getPetId());
    }

    @Test
    void convertFromDTO_retornaConsulta_quandoFuncionarioEPetExistem() throws Exception {
        //CT02
        ConsultaDTO dto = new ConsultaDTO(5L,
                LocalDateTime.of(2025, 2, 3, 9, 15),
                "check",
                50.00,
                100L,
                200L);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(100L);
        Pet pet = new Pet();
        pet.setId(200L);

        // injeta proxies que respondem findById
        injectRepositoryProxy("funcionarioRepository", "findById", 100L, Optional.of(funcionario));
        injectRepositoryProxy("petRepository", "findById", 200L, Optional.of(pet));

        // quando
        Consulta consulta = consultaService.convertFromDTO(dto);

        // então
        assertEquals(5L, consulta.getId());
        assertEquals(dto.getDataHora(), consulta.getDataHora());
        assertEquals("check", consulta.getObservacoes());
        assertEquals(50, consulta.getValor());
        assertNotNull(consulta.getFuncionario());
        assertEquals(100L, consulta.getFuncionario().getId());
        assertNotNull(consulta.getPet());
        assertEquals(200L, consulta.getPet().getId());
    }

    @Test
    void convertFromDTO_lancaEntityNotFound_quandoFuncionarioNaoExiste() throws Exception {
        //CT03
        ConsultaDTO dto = new ConsultaDTO(null,
                LocalDateTime.now(),
                "x",
                10,
                999L,
                1L);

        // funcionario ausente
        injectRepositoryProxy("funcionarioRepository", "findById", 999L, Optional.empty());
        // pet presente (para isolar o teste)
        Pet pet = new Pet(); pet.setId(1L);
        injectRepositoryProxy("petRepository", "findById", 1L, Optional.of(pet));

        // quando / então
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> consultaService.convertFromDTO(dto));
        assertTrue(ex.getMessage().contains("Funcion\u00E1rio"));
    }

    @Test
    void convertFromDTO_lancaEntityNotFound_quandoPetNaoExiste() throws Exception {
        //CT04
        ConsultaDTO dto = new ConsultaDTO(null,
                LocalDateTime.now(),
                "y",
                20,
                1L,
                999L);

        // funcionario presente
        Funcionario func = new Funcionario(); func.setId(1L);
        injectRepositoryProxy("funcionarioRepository", "findById", 1L, Optional.of(func));
        // pet ausente
        injectRepositoryProxy("petRepository", "findById", 999L, Optional.empty());

        // quando / então
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> consultaService.convertFromDTO(dto));
        assertTrue(ex.getMessage().contains("Pet"));
    }

    /**
     * @param nomeCampo
     * @param metodo
     * @param esperadoId
     * @param retorno
     * @throws Exception
     */
    private void injectRepositoryProxy(String nomeCampo, String metodo, Long esperadoId, Optional<?> retorno) throws Exception {
        Field field = ConsultaService.class.getDeclaredField(nomeCampo);
        field.setAccessible(true);
        Class<?> repoInterface = field.getType();

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                if (m.getName().equals(metodo) && args != null && args.length == 1 && args[0] instanceof Long) {
                    Long idArg = (Long) args[0];
                    if (idArg.equals(esperadoId)) {
                        return retorno;
                    } else {
                        return Optional.empty();
                    }
                }
                // métodos não suportados
                throw new UnsupportedOperationException("Método não suportado no proxy de teste: " + m.getName());
            }
        };

        Object proxy = Proxy.newProxyInstance(repoInterface.getClassLoader(), new Class[]{repoInterface}, handler);
        field.set(consultaService, proxy);
    }
}
