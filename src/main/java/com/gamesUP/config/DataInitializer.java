package com.gamesUP.config;

import com.gamesUP.model.*;
import com.gamesUP.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
    private final PurchaseRepository purchaseRepository;

    @Override
    public void run(String... args) {
        // Créer un admin par défaut
        User admin = null;
        if (!userRepository.existsByUsername("admin")) {
            admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gamesup.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            admin = userRepository.save(admin);
            System.out.println("✅ Admin créé : username=admin, password=admin123");
        } else {
            admin = userRepository.findByUsername("admin").orElseThrow();
        }

        // Créer un client par défaut
        User client = null;
        if (!userRepository.existsByUsername("client")) {
            client = new User();
            client.setUsername("client");
            client.setEmail("client@gamesup.com");
            client.setPassword(passwordEncoder.encode("client123"));
            client.setRole(Role.ROLE_CLIENT);
            client = userRepository.save(client);
            System.out.println("✅ Client créé : username=client, password=client123");
        } else {
            client = userRepository.findByUsername("client").orElseThrow();
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
        Game sevenWonders = null;
        Game azul = null;
        
        if (gameRepository.count() == 0) {
            Category strategy = categoryRepository.findByName("Stratégie").orElseThrow();
            Publisher asmodee = publisherRepository.findByName("Asmodee").orElseThrow();
            Author antoine = authorRepository.findByName("Antoine Bauza").orElseThrow();

            sevenWonders = new Game();
            sevenWonders.setName("7 Wonders");
            sevenWonders.setDescription("Construisez votre civilisation en 3 âges");
            sevenWonders.setPrice(39.99);
            sevenWonders.setMinPlayers(2);
            sevenWonders.setMaxPlayers(7);
            sevenWonders.setPlayingTime(30);
            sevenWonders.setCategory(strategy);
            sevenWonders.setPublisher(asmodee);
            sevenWonders.getAuthors().add(antoine);
            sevenWonders = gameRepository.save(sevenWonders);

            // Créer un deuxième jeu pour avoir plus de données
            Category family = categoryRepository.findByName("Famille").orElseThrow();
            Publisher gigamic = publisherRepository.findByName("Gigamic").orElseThrow();
            Author reiner = authorRepository.findByName("Reiner Knizia").orElseThrow();

            azul = new Game();
            azul.setName("Azul");
            azul.setDescription("Jeu de placement de tuiles inspiré des azulejos portugais");
            azul.setPrice(29.99);
            azul.setMinPlayers(2);
            azul.setMaxPlayers(4);
            azul.setPlayingTime(45);
            azul.setCategory(family);
            azul.setPublisher(gigamic);
            azul.getAuthors().add(reiner);
            azul = gameRepository.save(azul);

            System.out.println("✅ 2 jeux créés");
        } else {
            // Récupérer les jeux existants
            sevenWonders = gameRepository.findByName("7 Wonders").orElse(null);
            azul = gameRepository.findByName("Azul").orElse(null);
            
            if (sevenWonders == null || azul == null) {
                System.out.println("⚠️  Jeux manquants dans la base, skip création des commandes");
                return; // Sortir si les jeux n'existent pas
            }
        }

        // Créer des commandes d'exemple
        if (purchaseRepository.count() == 0) {
            // Commande 1 : Pour le client
            Purchase purchase1 = new Purchase();
            purchase1.setUser(client);
            purchase1.setPurchaseDate(LocalDateTime.now().minusDays(5));
            purchase1.setStatus(PurchaseStatus.DELIVERED);
            purchase1.setTotalAmount(0.0);

            PurchaseLine line1 = new PurchaseLine();
            line1.setPurchase(purchase1);
            line1.setGame(sevenWonders);
            line1.setQuantity(2);
            line1.setUnitPrice(sevenWonders.getPrice());
            purchase1.getPurchaseLines().add(line1);

            // Calculer le total
            double total1 = line1.getQuantity() * line1.getUnitPrice();
            purchase1.setTotalAmount(total1);
            
            purchaseRepository.save(purchase1);

            // Commande 2 : Pour l'admin (test)
            
            Purchase purchase2 = new Purchase();
            purchase2.setUser(admin);
            purchase2.setPurchaseDate(LocalDateTime.now().minusDays(2));
            purchase2.setStatus(PurchaseStatus.PAID);
            purchase2.setTotalAmount(0.0);

            PurchaseLine line2a = new PurchaseLine();
            line2a.setPurchase(purchase2);
            line2a.setGame(sevenWonders);
            line2a.setQuantity(1);
            line2a.setUnitPrice(sevenWonders.getPrice());
            purchase2.getPurchaseLines().add(line2a);

            PurchaseLine line2b = new PurchaseLine();
            line2b.setPurchase(purchase2);
            line2b.setGame(azul);
            line2b.setQuantity(1);
            line2b.setUnitPrice(azul.getPrice());
            purchase2.getPurchaseLines().add(line2b);

            // Calculer le total
            double total2 = (line2a.getQuantity() * line2a.getUnitPrice()) + 
                           (line2b.getQuantity() * line2b.getUnitPrice());
            purchase2.setTotalAmount(total2);
            
            purchaseRepository.save(purchase2);

            System.out.println("✅ 2 commandes créées");
            System.out.println("   - Commande 1 : Client (2x 7 Wonders) = " + total1 + "€ - DELIVERED");
            System.out.println("   - Commande 2 : Admin (1x 7 Wonders + 1x Azul) = " + total2 + "€ - PAID");
        }
    }
}