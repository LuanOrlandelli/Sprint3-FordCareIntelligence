package fordcare_api.controller;

import fordcare_api.entity.Recommendation;
import fordcare_api.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{customerId}")
    public Recommendation getRecommendationByCustomerId(
            @PathVariable Long customerId
    ) {
        return recommendationService
                .getLatestRecommendationByCustomerId(customerId);
    }
}