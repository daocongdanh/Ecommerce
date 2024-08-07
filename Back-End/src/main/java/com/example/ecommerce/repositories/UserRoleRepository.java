package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Role;
import com.example.ecommerce.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByRole(Role role);
}
