package com.gamesUP.service;

import com.gamesUP.dto.recommendation.RecommendationDtos.*;
import com.gamesUP.model.Game;
import com.gamesUP.model.Rating;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RatingRepository ratingRepository;
    private final GameRepository gameRepository;
    private final RestTemplate restTemplate;
    private final String pythonApiUrl;

    /**
     * Génère des recommandations personnalisées pour un utilisateur.
     *
     * Flux :
     * 1. Récupérer les notes de l'utilisateur en base Spring
     * 2. Appeler POST {pythonApiUrl}/recommendations/ avec user_id + purchases
     * 3. Désérialiser la réponse Python : { user_id, recommendations: [...] }
     * 4. Enrichir avec prix et note moyenne depuis la base Spring
     * 5. Si Python indisponible ou aucune note → liste vide, pas de crash
     */
    public List<RecommendationResponse> getRecommendations(Long userId) {

        // Étape 1 : Récupérer les ratings de l'utilisateur
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        // Construire le corps de la requête Python
        List<UserPurchaseRequest> purchases = userRatings.stream()
                .map(r -> new UserPurchaseRequest(
                        r.getGame().getId(),
                        r.getScore()   // note sur 10, même échelle que Python
                ))
                .collect(Collectors.toList());

        UserDataRequest requestBody = new UserDataRequest(userId, purchases);

        // Étape 2 : Appeler Python
        List<PythonRecommendation> pythonRecs = callPythonApi(requestBody);

        if (pythonRecs.isEmpty()) {
            return Collections.emptyList();
        }

        // Étape 3 : Enrichir avec les données Spring (prix, note moyenne réelle)
        List<Long> gameIds = pythonRecs.stream()
                .map(PythonRecommendation::getGameId)
                .collect(Collectors.toList());

        Map<Long, Game> gamesById = gameRepository.findAllById(gameIds.stream().distinct().collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(Game::getId, Function.identity()));

        Map<Long, Double> avgRatings = gameIds.stream()
                .distinct()  // ← évite les doublons de game_id dans la liste Python
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            Double avg = ratingRepository.calculateAverageRatingForGame(id);
                            return avg != null ? avg : 0.0;
                        }
                ));

        return pythonRecs.stream()
                .map(rec -> {
                    Game game = gamesById.get(rec.getGameId());
                    return new RecommendationResponse(
                            rec.getGameId(),
                            rec.getGameName(),
                            rec.getScore(),
                            avgRatings.get(rec.getGameId()),
                            game != null ? game.getPrice() : null
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Appelle POST /recommendations/ sur l'API Python.
     *
     * La réponse Python a la forme :
     *   { "user_id": 1, "recommendations": [ {game_id, game_name, score}, ... ] }
     *
     * On extrait uniquement la liste "recommendations".
     * En cas d'erreur réseau → liste vide + log warn (pas de 500 côté Spring).
     */
    private List<PythonRecommendation> callPythonApi(UserDataRequest requestBody) {
        String url = pythonApiUrl + "/recommendations/";
        try {
            PythonApiResponse response = restTemplate.postForObject(
                    url,
                    requestBody,
                    PythonApiResponse.class
            );

            if (response == null || response.getRecommendations() == null) {
                return Collections.emptyList();
            }
            return response.getRecommendations();

        } catch (RestClientException e) {
            log.warn("API Python indisponible ({}). Aucune recommandation retournée. Erreur : {}",
                    url, e.getMessage());
            return Collections.emptyList();
        }
    }
}