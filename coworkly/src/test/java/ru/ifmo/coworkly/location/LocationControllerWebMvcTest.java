package ru.ifmo.coworkly.location;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.List;
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
import ru.ifmo.coworkly.user.UserRole;

@WebMvcTest(LocationController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class LocationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

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
    void getLocationsReturnsDataForResident() throws Exception {
        when(locationService.getAll()).thenReturn(List.of(
                new LocationResponse(1L, "Nevsky Hub", "Nevsky 1"),
                new LocationResponse(2L, "Petrograd Space", "Kamennoostrovsky 10")
        ));

        mockMvc.perform(get("/api/locations")
                        .with(authentication(principal(7L).toAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Nevsky Hub"))
                .andExpect(jsonPath("$[1].address").value("Kamennoostrovsky 10"));
    }

    private UserPrincipal principal(Long id) {
        return new UserPrincipal(id, "resident@example.com", UserRole.RESIDENT);
    }
}
