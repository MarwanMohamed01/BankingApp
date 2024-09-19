package com.javaguides.BankingApp.system;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface systemDefaultsRepository extends JpaRepository<systemDefaults, Integer> {

    Optional<systemDefaults> findByKey(String key);

}
