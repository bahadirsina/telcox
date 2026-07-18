package com.telcox.identity.repository;

import com.telcox.identity.domain.IdentityUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdentityUserRepository extends JpaRepository<IdentityUser, UUID> {

    Optional<IdentityUser> findByKeycloakSubject(UUID keycloakSubject);
}
