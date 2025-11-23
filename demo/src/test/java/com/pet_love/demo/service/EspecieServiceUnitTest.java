// java
package com.pet_love.demo.service;

import com.pet_love.demo.model.Especie;
import com.pet_love.demo.model.dto.EspecieDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EspecieServiceUnitTest {

    @Test
    public void convertToDTO_mapeiaCamposCorretamente() {
        //CT05
        Especie especie = new Especie();
        especie.setId(1L);
        especie.setNome("Canina");

        EspecieDTO dto = EspecieService.convertToDTO(especie);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Canina", dto.getNome());
    }

    @Test
    public void convertFromDTO_mapeiaCamposCorretamente() {
        //CT06
        EspecieDTO dto = new EspecieDTO();
        dto.setId(2L);
        dto.setNome("Felina");

        Especie especie = EspecieService.convertFromDTO(dto);

        assertNotNull(especie);
        assertEquals(2L, especie.getId());
        assertEquals("Felina", especie.getNome());
    }
}
