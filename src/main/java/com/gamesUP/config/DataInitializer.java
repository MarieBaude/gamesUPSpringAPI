package com.gamesUP.config;

import com.gamesUP.model.*;
import com.gamesUP.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final GameRepository gameRepository;

    @Override
    public void run(String... args) {
        // Créer un admin par défaut
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gamesup.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Admin créé : username=admin, password=admin123");
        }

        // Créer des catégories si elles n'existent pas
        if (categoryRepository.count() == 0) {
            Category strategy = new Category();
            strategy.setName("Stratégie");
            strategy.setDescription("Jeux de réflexion et de planification");
            categoryRepository.save(strategy);

            Category family = new Category();
            family.setName("Famille");
            family.setDescription("Jeux pour toute la famille");
            categoryRepository.save(family);

            Category party = new Category();
            party.setName("Ambiance");
            party.setDescription("Jeux festifs et rapides");
            categoryRepository.save(party);

            System.out.println("✅ 3 catégories créées");
        }

        // Créer des éditeurs
        if (publisherRepository.count() == 0) {
            Publisher asmodee = new Publisher();
            asmodee.setName("Asmodee");
            asmodee.setContactInfo("contact@asmodee.com");
            publisherRepository.save(asmodee);

            Publisher gigamic = new Publisher();
            gigamic.setName("Gigamic");
            gigamic.setContactInfo("contact@gigamic.com");
            publisherRepository.save(gigamic);

            System.out.println("✅ 2 éditeurs créés");
        }

        // Créer des auteurs
        if (authorRepository.count() == 0) {
            Author reiner = new Author();
            reiner.setName("Reiner Knizia");
            reiner.setBiography("Auteur prolifique de jeux de société");
            authorRepository.save(reiner);

            Author antoine = new Author();
            antoine.setName("Antoine Bauza");
            antoine.setBiography("Créateur de 7 Wonders");
            authorRepository.save(antoine);

            System.out.println("✅ 2 auteurs créés");
        }

        // Créer des jeux d'exemple
        if (gameRepository.count() == 0) {
            Category strategy = categoryRepository.findByName("Stratégie").orElseThrow();
            Publisher asmodee = publisherRepository.findByName("Asmodee").orElseThrow();
            Author antoine = authorRepository.findByName("Antoine Bauza").orElseThrow();

            Game sevenWonders = new Game();
            sevenWonders.setName("7 Wonders");
            sevenWonders.setDescription("Construisez votre civilisation en 3 âges");
            sevenWonders.setPrice(39.99);
            sevenWonders.setMinPlayers(2);
            sevenWonders.setMaxPlayers(7);
            sevenWonders.setPlayingTime(30);
            sevenWonders.setCategory(strategy);
            sevenWonders.setPublisher(asmodee);
            sevenWonders.getAuthors().add(antoine);
            gameRepository.save(sevenWonders);

            System.out.println("✅ 1 jeu créé");
        }
    }
}