package fordcare_api.service;

import fordcare_api.entity.Recommendation;
import fordcare_api.repository.RecommendationRepository;
import org.springframework.stereotype.Service;
import fordcare_api.exception.ResourceNotFoundException;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public Recommendation getLatestRecommendationByCustomerId(Long customerId) {
        return recommendationRepository
                .findTopByCustomerIdOrderByCreatedAtDesc(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Recomendação não encontrada")
                );
    }
}