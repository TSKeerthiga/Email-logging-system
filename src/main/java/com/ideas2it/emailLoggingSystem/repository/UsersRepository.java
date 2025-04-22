package com.ideas2it.emailLoggingSystem.repository;

import com.ideas2it.emailLoggingSystem.entity.Users;
import com.ideas2it.emailLoggingSystem.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UsersRepository extends JpaRepository<Users, Long> {
    /**
     * Finds a Users entity by its username.
     *
     * @param username the username of the user to find
     * @return an Optional containing the Users entity if found, otherwise empty
     */
    Optional<Users> findByUsername(String username);


    List<Users> findByRole_Name(String roleName);

}
