package com.hwinterton.inventory_api.startup;

import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hwinterton.inventory_api.model.Role;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;

/**
 * Creates default owner accounts when the application starts with an empty user table.
 * 
 * <p>Generates temporary first-login passwords, stores BCrypt password hashes,
 * and forces both accounts to change their passwords after first login.</p>
 */
@Component
public class StartupRunner implements ApplicationRunner{

    // startup dependency fields
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // constructor injection
    public StartupRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Runs automatically during application startup.
     *
     * <p>Skips setup if users already exist. Otherwise, creates owner and
     * owner_backup accounts with generated temporary passwords.</p>
     *
     * @param args startup arguments passed by Spring Boot
     * @throws Exception if startup account creation fails
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // skip first-run setup if users already exist
        if (userRepository.count() != 0){
            return;
        }
        // otherwise, generate temp first-login passwords 
        String tempPassword1 = UUID.randomUUID().toString();
        String tempPassword2 = UUID.randomUUID().toString();

        // create default owner account objects
        User owner = new User();
        User ownerBackup = new User();

        // sets owner fields 
        owner.setUsername("owner");
        owner.setPasswordHash(passwordEncoder.encode(tempPassword1));
        owner.setRole(Role.OWNER);
        owner.setMustChangePassword(true);
        owner.setActive(true);

        // sets owner_backup fields 
        ownerBackup.setUsername("owner_backup");
        ownerBackup.setPasswordHash(passwordEncoder.encode(tempPassword2));
        ownerBackup.setRole(Role.OWNER);
        ownerBackup.setMustChangePassword(true);
        ownerBackup.setActive(true);

        // store the fields into their respective  objects 
        userRepository.save(owner);
        userRepository.save(ownerBackup);

        System.out.println("owner temp password is " + tempPassword1);
        System.out.println("owner_backup temp password is " + tempPassword2);
    }
}
