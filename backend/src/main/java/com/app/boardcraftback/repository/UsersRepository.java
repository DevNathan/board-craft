package com.app.boardcraftback.repository;

import com.app.boardcraftback.domain.entity.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {

    Optional<Users> findByEmailIgnoreCase(String email);

    boolean existsByNickname(String nickname);

}
