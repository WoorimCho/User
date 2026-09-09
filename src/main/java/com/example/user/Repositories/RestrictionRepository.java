package com.example.user.Repositories;

import com.example.user.Model.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestrictionRepository extends JpaRepository<Restriction, Long> {

    List<Restriction> findByKindIgnoreCaseOrderByLabelAsc(String kind);

    List<Restriction> findAllByOrderByKindAscLabelAsc();

    Optional<Restriction> findByCodeIgnoreCase(String code);
}
