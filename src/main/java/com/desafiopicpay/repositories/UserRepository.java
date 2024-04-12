package com.desafiopicpay.repositories;

import com.desafiopicpay.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //pode ou não retornar um usuário
    Optional<User> findUserByDocument(String document);
    Optional<User> findUserById(Long id);
}
