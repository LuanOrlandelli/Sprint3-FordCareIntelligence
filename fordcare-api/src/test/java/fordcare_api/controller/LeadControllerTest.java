package fordcare_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fordcare_api.dto.request.LeadCreateRequest;
import fordcare_api.dto.response.LeadResponse;
import fordcare_api.entity.Lead;
import fordcare_api.exception.GlobalExceptionHandler;
import fordcare_api.exception.ResourceNotFoundException;
import fordcare_api.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LeadControllerTest {

    private MockMvc mockMvc;
    private LeadService leadService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        leadService = mock(LeadService.class);

        LeadController leadController =
                new LeadController(leadService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(leadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void deveListarLeadsComSucesso() throws Exception {

        LeadResponse lead = new LeadResponse(
                1L,
                1L,
                "Cliente Teste",
                10L,
                "NEW",
                "APP",
                "Cliente interessado",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(leadService.listLeads())
                .thenReturn(List.of(lead));

        mockMvc.perform(
                        get("/leads")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("NEW"));
    }

    @Test
    void deveCriarLeadComSucesso() throws Exception {

        Lead savedLead = new Lead();

        savedLead.setId(1L);
        savedLead.setCustomerId(1L);
        savedLead.setDealershipId(10L);
        savedLead.setStatus("NEW");
        savedLead.setOrigin("APP");
        savedLead.setNotes("Cliente interessado");
        savedLead.setCreatedAt(LocalDateTime.now());
        savedLead.setUpdatedAt(LocalDateTime.now());

        when(
                leadService.createLead(
                        any(LeadCreateRequest.class),
                        any()
                )
        ).thenReturn(savedLead);

        String json = """
                {
                    "customerId": 1,
                    "dealershipId": 10,
                    "origin": "APP",
                    "notes": "Cliente interessado"
                }
                """;

        mockMvc.perform(
                        post("/leads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void deveRetornarBadRequestAoCriarLeadInvalido()
            throws Exception {

        String json = """
                {
                    "origin": ""
                }
                """;

        mockMvc.perform(
                        post("/leads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundQuandoLeadNaoExistir()
            throws Exception {

        when(
                leadService.updateLeadStatus(
                        eq(999L),
                        any(),
                        any()
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "Lead não encontrado"
                )
        );

        String json = """
                {
                    "status": "CONTACTED"
                }
                """;

        mockMvc.perform(
                        put("/leads/999/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status").value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Lead não encontrado")
                );
    }
}