package com.example.user.Repositories;

import com.example.user.Model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Account> findByUsernameIgnoreCase(String username);

    Optional<Account> findByEmailIgnoreCase(String email);
}
