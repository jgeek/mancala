package me.moreka.mancala.service;

import me.moreka.mancala.entity.User;
import me.moreka.mancala.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreateUser(String username) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            user = userRepository.save(new User(username));
        }
        return user;
    }
}
