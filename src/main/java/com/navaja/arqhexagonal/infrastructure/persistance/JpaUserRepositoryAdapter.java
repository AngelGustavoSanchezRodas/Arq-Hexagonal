package com.navaja.arqhexagonal.infrastructure.persistance;

import com.navaja.arqhexagonal.application.port.out.UserRepositoryPort;
import com.navaja.arqhexagonal.domain.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springUserRepository;

    public JpaUserRepositoryAdapter(SpringDataUserRepository springUserRepository) {
        this.springUserRepository = springUserRepository;
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = new UserEntity(user.id(), user.firstName(), user.lastName());
        final UserEntity savedUser = springUserRepository.save(userEntity);
        return new User(savedUser.getId(), savedUser.getFirstName(),  savedUser.getLastName());
    }
}
