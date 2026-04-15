package com.navaja.arqhexagonal.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository  extends JpaRepository <Long, UserEntity> {
}
