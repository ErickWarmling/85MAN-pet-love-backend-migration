// java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Especie;
import com.pet_love.demo.model.Raca;
import com.pet_love.demo.model.dto.RacaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RacaServiceUnitTest {

    @Test
    public void convertToDTO_mapeiaCamposCorretamente() {
        //CT07
        Especie especie = new Especie();
        especie.setId(1L);
        especie.setNome("Canina");

        Raca raca = new Raca();
        raca.setId(10L);
        raca.setNome("Labrador");
        raca.setEspecie(especie);

        RacaDTO dto = RacaService.convertToDTO(raca);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Labrador", dto.getNome());
        assertNotNull(dto.getEspecie());
        assertEquals(1L, dto.getEspecie().getId());
        assertEquals("Canina", dto.getEspecie().getNome());
    }

    @Test
    public void convertFromDTO_mapeiaCamposCorretamente() {
        //CT08
        Especie especie = new Especie();
        especie.setId(2L);
        especie.setNome("Felina");

        RacaDTO dto = new RacaDTO();
        dto.setId(20L);
        dto.setNome("Siamês");
        dto.setEspecie(especie);

        Raca raca = RacaService.convertFromDTO(dto);

        assertNotNull(raca);
        assertEquals(20L, raca.getId());
        assertEquals("Siamês", raca.getNome());
        assertNotNull(raca.getEspecie());
        assertEquals(2L, raca.getEspecie().getId());
        assertEquals("Felina", raca.getEspecie().getNome());
    }
}
