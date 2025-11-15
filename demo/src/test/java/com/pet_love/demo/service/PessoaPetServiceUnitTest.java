// java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Pessoa;
import com.pet_love.demo.model.PessoaPet;
import com.pet_love.demo.model.Pet;
import com.pet_love.demo.model.dto.PessoaPetDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PessoaPetServiceUnitTest {

    private void injectRepositoryProxy(String fieldName, Class<?> repoInterface, Long expectedId, Optional<?> returnOptional) throws Exception {
        Object proxy = Proxy.newProxyInstance(
                repoInterface.getClassLoader(),
                new Class[]{repoInterface},
                (proxyObj, method, args) -> {
                    if ("findById".equals(method.getName()) && args != null && args.length == 1) {
                        Long idArg = (Long) args[0];
                        return idArg.equals(expectedId) ? returnOptional : Optional.empty();
                    }
                    // default return for other methods
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType().equals(boolean.class)) return false;
                        if (method.getReturnType().equals(int.class)) return 0;
                    }
                    return null;
                }
        );

        Field field = PessoaPetService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        // field is static
        field.set(null, proxy);
    }

    @Test
    public void convertToDTO_mapeiaCamposCorretamente() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(11L);

        Pet pet = new Pet();
        pet.setId(22L);

        PessoaPet pessoaPet = new PessoaPet();
        pessoaPet.setPessoa(pessoa);
        pessoaPet.setPet(pet);
        pessoaPet.setPrincipal(true);

        var dto = PessoaPetService.convertToDTO(pessoaPet);

        assertNotNull(dto);
        assertEquals(11L, dto.getPessoaId());
        assertEquals(22L, dto.getPetId());
        assertTrue(dto.isPrincipal());
    }

    @Test
    public void convertFromDTO_retornaPessoaPet_quandoPessoaEPetExistem() throws Exception {
        Long pessoaId = 101L;
        Long petId = 202L;

        Pessoa pessoa = new Pessoa();
        pessoa.setId(pessoaId);
        Pet pet = new Pet();
        pet.setId(petId);

        injectRepositoryProxy("pessoaRepository", Class.forName("com.pet_love.demo.repository.PessoaRepository"), pessoaId, Optional.of(pessoa));
        injectRepositoryProxy("petRepository", Class.forName("com.pet_love.demo.repository.PetRepository"), petId, Optional.of(pet));

        PessoaPetDTO dto = new PessoaPetDTO();
        dto.setPessoaId(pessoaId);
        dto.setPetId(petId);
        dto.setPrincipal(false);

        PessoaPet resultado = PessoaPetService.convertFromDTO(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getPessoa());
        assertNotNull(resultado.getPet());
        assertEquals(pessoaId, resultado.getPessoa().getId());
        assertEquals(petId, resultado.getPet().getId());
        assertFalse(resultado.isPrincipal());
    }

    @Test
    public void convertFromDTO_naoDefinePessoa_quandoPessoaNaoExiste() throws Exception {
        Long pessoaId = 301L;
        Long petId = 302L;

        Pet pet = new Pet();
        pet.setId(petId);

        // pessoaRepository retorna empty, petRepository retorna pet válido
        injectRepositoryProxy("pessoaRepository", Class.forName("com.pet_love.demo.repository.PessoaRepository"), pessoaId, Optional.empty());
        injectRepositoryProxy("petRepository", Class.forName("com.pet_love.demo.repository.PetRepository"), petId, Optional.of(pet));

        PessoaPetDTO dto = new PessoaPetDTO();
        dto.setPessoaId(pessoaId);
        dto.setPetId(petId);
        dto.setPrincipal(true);

        PessoaPet resultado = PessoaPetService.convertFromDTO(dto);

        assertNotNull(resultado);
        assertNull(resultado.getPessoa());
        assertNotNull(resultado.getPet());
        assertEquals(petId, resultado.getPet().getId());
        assertTrue(resultado.isPrincipal());
    }

    @Test
    public void convertFromDTO_naoDefinePet_quandoPetNaoExiste() throws Exception {
        Long pessoaId = 401L;
        Long petId = 402L;

        Pessoa pessoa = new Pessoa();
        pessoa.setId(pessoaId);

        // pessoaRepository retorna pessoa válido, petRepository retorna empty
        injectRepositoryProxy("pessoaRepository", Class.forName("com.pet_love.demo.repository.PessoaRepository"), pessoaId, Optional.of(pessoa));
        injectRepositoryProxy("petRepository", Class.forName("com.pet_love.demo.repository.PetRepository"), petId, Optional.empty());

        PessoaPetDTO dto = new PessoaPetDTO();
        dto.setPessoaId(pessoaId);
        dto.setPetId(petId);
        dto.setPrincipal(false);

        PessoaPet resultado = PessoaPetService.convertFromDTO(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getPessoa());
        assertNull(resultado.getPet());
        assertEquals(pessoaId, resultado.getPessoa().getId());
        assertFalse(resultado.isPrincipal());
    }
}
