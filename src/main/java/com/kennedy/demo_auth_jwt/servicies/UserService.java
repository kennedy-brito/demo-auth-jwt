package com.kennedy.demo_auth_jwt.servicies;

import com.kennedy.demo_auth_jwt.entities.User;
import com.kennedy.demo_auth_jwt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    @Transactional
    public User save(User user){

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long id){
        return userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Id not found")
        );
    }

    @Transactional(readOnly = true)
    public List<User> findAll(){
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {

        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("user not found")
        );
    }

    @Transactional(readOnly = true)
    public User.Role getRoleByUsername(String username) {
        return userRepository.findRoleByUsername(username);
    }
}
