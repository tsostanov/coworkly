package ru.ifmo.coworkly.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.coworkly.common.ApiExceptionHandler;
import ru.ifmo.coworkly.security.JwtAuthenticationFilter;
import ru.ifmo.coworkly.security.SecurityConfig;
import ru.ifmo.coworkly.security.UserPrincipal;

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class BookingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void allowFilterChainToContinue() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0, ServletRequest.class),
                    invocation.getArgument(1, ServletResponse.class));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void createBookingReturnsCreatedForOwnBooking() throws Exception {
        when(bookingService.createBooking(any(CreateBookingRequest.class))).thenReturn(101L);

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(principal(7L).toAuthentication()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 7,
                                  "spaceId": 5,
                                  "startsAt": "2026-07-05T10:00:00Z",
                                  "endsAt": "2026-07-05T11:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(101));
    }

    @Test
    void createBookingReturnsForbiddenWhenResidentCreatesForAnotherUser() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(authentication(principal(7L).toAuthentication()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 9,
                                  "spaceId": 5,
                                  "startsAt": "2026-07-05T10:00:00Z",
                                  "endsAt": "2026-07-05T11:00:00Z"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(bookingService, never()).createBooking(any(CreateBookingRequest.class));
    }

    @Test
    void createBookingReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(authentication(principal(7L).toAuthentication()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 7,
                                  "startsAt": "2026-07-05T10:00:00Z",
                                  "endsAt": "2026-07-05T11:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("spaceId")));
    }

    private UserPrincipal principal(Long id) {
        return new UserPrincipal(id, "resident@example.com", ru.ifmo.coworkly.user.UserRole.RESIDENT);
    }
}
