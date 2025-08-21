package br.com.raphael.geradordesimulado.config;

import br.com.raphael.geradordesimulado.domain.User;
import br.com.raphael.geradordesimulado.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                User admin = User.builder()
                        .name("Admin")
                        .email("admin@admin.com")
                        .password(encoder.encode("123456"))
                        .role(User.Role.ADMIN)
                        .active(true)
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Usuário admin criado: admin@admin.com / 123456");
            }
        };
    }
}
