package fordcare_api.controller;

import fordcare_api.dto.request.LeadCreateRequest;
import fordcare_api.dto.request.LeadStatusUpdateRequest;
import fordcare_api.dto.response.LeadResponse;
import fordcare_api.entity.Lead;
import fordcare_api.service.LeadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> listLeads() {
        return ResponseEntity.ok(leadService.listLeads());
    }

    @PostMapping
    public ResponseEntity<Lead> createLead(
            @Valid @RequestBody LeadCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Lead lead = leadService.createLead(request, httpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lead);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Lead> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeadStatusUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                leadService.updateLeadStatus(id, request, httpRequest)
        );
    }
}