package com.pet_love.demo.repository;

import com.pet_love.demo.model.Pessoa;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Pessoa p WHERE NOT EXISTS (SELECT 1 FROM Usuario u WHERE u.pessoa.id = p.id)")
    int deleteAllWithoutUsuario();

}
