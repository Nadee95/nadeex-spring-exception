package com.nadeex.spring.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadeex.spring.exception.config.ExceptionAutoConfiguration;
import com.nadeex.spring.exception.handler.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link GlobalExceptionHandler}.
 *
 * <p>Uses a minimal {@link StubController} to trigger each exception type
 * and asserts the correct HTTP status + {@code ErrorResponse} JSON shape.</p>
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.StubController.class)
@Import({GlobalExceptionHandler.class, ExceptionAutoConfiguration.class})
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Stub controller — only exists to throw each exception on demand
    // -------------------------------------------------------------------------

    @RestController
    @RequestMapping("/test")
    static class StubController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("User", 42L);
        }

        @GetMapping("/conflict")
        public void conflict() {
            throw new ConflictException("User", "john@example.com");
        }

        @GetMapping("/business")
        public void business() {
            throw new BusinessException("Case is already closed");
        }

        @GetMapping("/validation")
        public void validation() {
            throw new ValidationException("email", "Email is already registered");
        }

        @GetMapping("/unauthorized")
        public void unauthorized() {
            throw new UnauthorizedException("JWT token has expired");
        }

        @GetMapping("/forbidden")
        public void forbidden() {
            throw new ForbiddenException("Only lawyers can be assigned to cases");
        }

        @GetMapping("/server-error")
        public void serverError() {
            throw new RuntimeException("Unexpected infrastructure failure");
        }

        @GetMapping("/constraint-violation")
        public void constraintViolation() {
            // Simulates what @Validated on controller params or service layer would throw
            throw new ConstraintViolationException("findById.id: must not be null", Set.of());
        }

        @PostMapping("/bean-validation")
        public void beanValidation(@Valid @RequestBody SampleRequest request) {
            // If validation passes, just return — we only care about failure path
        }

        record SampleRequest(@NotNull @NotBlank String name, @NotNull String email) {}
    }

    // -------------------------------------------------------------------------
    // 404 Not Found
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ResourceNotFoundException → 404")
    class ResourceNotFoundTests {

        @Test
        void shouldReturn404WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("User not found with id: 42"))
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/test/not-found"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // 409 Conflict
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ConflictException → 409")
    class ConflictTests {

        @Test
        void shouldReturn409WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/conflict"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("User already exists with id: john@example.com"))
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_ALREADY_EXISTS"))
                    .andExpect(jsonPath("$.path").value("/test/conflict"));
        }
    }

    // -------------------------------------------------------------------------
    // 400 Bad Request
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("BusinessException → 400")
    class BusinessExceptionTests {

        @Test
        void shouldReturn400WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/business"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Case is already closed"))
                    .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"))
                    .andExpect(jsonPath("$.path").value("/test/business"));
        }
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — programmatic
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ValidationException → 422")
    class ValidationExceptionTests {

        @Test
        void shouldReturn422WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/validation"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.message").value("Email is already registered"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.path").value("/test/validation"));
        }
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — @Valid bean validation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("MethodArgumentNotValidException → 422")
    class BeanValidationTests {

        @Test
        void shouldReturn422WithFieldErrors() throws Exception {
            String emptyBody = objectMapper.writeValueAsString(
                    new StubController.SampleRequest("", null));

            mockMvc.perform(post("/test/bean-validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyBody))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.validationErrors").isArray())
                    .andExpect(jsonPath("$.validationErrors.length()").value(2));
        }
    }

    // -------------------------------------------------------------------------
    // 401 Unauthorized
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("UnauthorizedException → 401")
    class UnauthorizedTests {

        @Test
        void shouldReturn401WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/unauthorized"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("JWT token has expired"))
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.path").value("/test/unauthorized"));
        }
    }

    // -------------------------------------------------------------------------
    // 403 Forbidden
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ForbiddenException → 403")
    class ForbiddenTests {

        @Test
        void shouldReturn403WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/forbidden"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Only lawyers can be assigned to cases"))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.path").value("/test/forbidden"));
        }
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — ConstraintViolationException (@Validated)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ConstraintViolationException → 422")
    class ConstraintViolationTests {

        @Test
        void shouldReturn422WithCorrectBody() throws Exception {
            mockMvc.perform(get("/test/constraint-violation"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value("Validation failed for one or more constraints"))
                    .andExpect(jsonPath("$.path").value("/test/constraint-violation"));
        }
    }

    // -------------------------------------------------------------------------
    // 500 Internal Server Error — generic fallback
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Unhandled Exception → 500")
    class GenericExceptionTests {

        @Test
        void shouldReturn500WithSafeMessage() throws Exception {
            mockMvc.perform(get("/test/server-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                    .andExpect(jsonPath("$.path").value("/test/server-error"));
        }

        @Test
        void shouldNotExposeInternalExceptionMessageIn500() throws Exception {
            mockMvc.perform(get("/test/server-error"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
        }
    }
}

