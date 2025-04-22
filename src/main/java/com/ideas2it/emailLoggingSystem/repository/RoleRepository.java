package com.ideas2it.emailLoggingSystem.repository;

import com.ideas2it.emailLoggingSystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    /**
     * Finds a Role by its name.
     *
     * @param name the name of the Role to find
     * @return an Optional containing the Role if found, otherwise empty
     */
    Optional<Role> findByName(String name);

    /**
     * Checks if a Role with the specified name exists.
     *
     * @param name the name of the Role to check
     * @return true if the Role exists, otherwise false
     */
    boolean existsByName(String name);
}
