package com.example.weaver.services;

import com.example.weaver.enums.AuthProvider;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.User;
import com.example.weaver.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User create(String email,String hashedPassword){
        if(userRepository.existsByEmail(email)){
            throw new BadRequestException("User with this email already exists");
        }
        User user=User.builder()
                .email(email)
                .password(hashedPassword)
                .status(UserStatus.PENDING)
                .provider(AuthProvider.LOCAL)
                .build();
        return userRepository.save(user);
    }

    public User createUserViaOAuth(String email,AuthProvider authProvider,String providerId){
        User newUser=User.builder()
                .email(email)
                .status(UserStatus.ACTIVE)
                .provider(authProvider)
                .providerId(providerId)
                .build();
        return userRepository.save(newUser);
    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
    public List<User> searchUsers(String query, UUID userId){
        return userRepository.searchUsersExcludingSelf(userId, query);
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(()->new NotFoundException("User not found"));
    }
    public List<User> findAllByIds(List<UUID> userIds) {
        return userRepository.findAllById(userIds);
    }
}
