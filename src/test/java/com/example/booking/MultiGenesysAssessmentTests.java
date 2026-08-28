package com.example.booking;

import com.example.booking.dto.auth.LoginRequest;
import com.example.booking.dto.auth.RegisterRequest;
import com.example.booking.dto.reservation.ReservationRequest;
import com.example.booking.dto.resource.ResourceRequest;
import com.example.booking.entity.*;
import com.example.booking.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MultiGenesysAssessmentTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ResourceRepository resourceRepository;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                reservationRepository.deleteAll();
                resourceRepository.deleteAll();
                userRepository.deleteAll();

                SystemUser admin = new SystemUser();
                admin.setUsername("Admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                SystemUser user = new SystemUser();
                user.setUsername("User");
                user.setPassword(passwordEncoder.encode("User@123"));
                user.setRole(Role.USER);
                userRepository.save(user);
        }

        private String obtainToken(String username, String password) throws Exception {
                LoginRequest loginRequest = new LoginRequest(username, password);
                String requestBody = objectMapper.writeValueAsString(loginRequest);

                MvcResult result = mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                return com.jayway.jsonpath.JsonPath.read(response, "$.token");
        }

        @Test
        void testLoginSuccess() throws Exception {
                String token = obtainToken("User", "User@123");
                assertNotNull(token);
        }

        @Test
        void testLoginFailure() throws Exception {
                LoginRequest loginRequest = new LoginRequest("User", "wrongpassword");
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testCreateResourceAsAdmin() throws Exception {
                String adminToken = obtainToken("Admin", "Admin@123");

                ResourceRequest resourceRequest = new ResourceRequest(
                                "Conference Room A",
                                "Spacious meeting room",
                                new BigDecimal("150.00"),
                                true);

                mockMvc.perform(post("/resources")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resourceRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("Conference Room A"))
                                .andExpect(jsonPath("$.price").value(150.00));
        }

        @Test
        void testCreateResourceAsUserForbidden() throws Exception {
                String userToken = obtainToken("User", "User@123");

                ResourceRequest resourceRequest = new ResourceRequest(
                                "Projector",
                                "HD Projector",
                                new BigDecimal("50.00"),
                                true);

                mockMvc.perform(post("/resources")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resourceRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testGetResourcesAllowedForUserAndAdmin() throws Exception {
                // Create resource directly
                Resource resource = resourceRepository
                                .save(new Resource("Laptop", "Developer laptop", new BigDecimal("10.00"), true));

                String userToken = obtainToken("User", "User@123");
                String adminToken = obtainToken("Admin", "Admin@123");

                // User GET
                mockMvc.perform(get("/resources/" + resource.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Laptop"));

                // Admin GET
                mockMvc.perform(get("/resources")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        void testCreateReservationAndOwnership() throws Exception {
                // Save resource
                Resource resource = resourceRepository.save(
                                new Resource("Conference Room B", "Meeting room", new BigDecimal("100.00"), true));

                // Create User 2
                RegisterRequest registerRequest = new RegisterRequest("user2", "Password123!");
                mockMvc.perform(post("/auth/create-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                String user1Token = obtainToken("User", "User@123");
                String user2Token = obtainToken("User2", "Password123!");
                String adminToken = obtainToken("Admin", "Admin@123");

                // User 1 creates reservation
                ReservationRequest reservationRequest = new ReservationRequest(
                                resource.getId(),
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(1).plusHours(2));

                MvcResult result = mockMvc.perform(post("/reservations")
                                .header("Authorization", "Bearer " + user1Token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reservationRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.resourceId").value(resource.getId()))
                                .andExpect(jsonPath("$.username").value("User"))
                                .andReturn();

                String reservationJson = result.getResponse().getContentAsString();
                Long reservationId = ((Number) com.jayway.jsonpath.JsonPath.read(reservationJson, "$.id")).longValue();

                // User 1 can view their own reservation
                mockMvc.perform(get("/reservations/" + reservationId)
                                .header("Authorization", "Bearer " + user1Token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(reservationId));

                // User 2 cannot view User 1's reservation
                mockMvc.perform(get("/reservations/" + reservationId)
                                .header("Authorization", "Bearer " + user2Token))
                                .andExpect(status().isForbidden());

                // Admin can view User 1's reservation
                mockMvc.perform(get("/reservations/" + reservationId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(reservationId));
        }

        @Test
        void testOverlappingReservationFails() throws Exception {
                Resource resource = resourceRepository
                                .save(new Resource("Camera", "DSLR Camera", new BigDecimal("40.00"), true));
                String userToken = obtainToken("User", "User@123");

                LocalDateTime start = LocalDateTime.now().plusDays(2);
                LocalDateTime end = start.plusHours(4);

                ReservationRequest req1 = new ReservationRequest(resource.getId(), start, end);
                mockMvc.perform(post("/reservations")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req1)))
                                .andExpect(status().isCreated());

                // Overlapping request (starts during req1)
                ReservationRequest req2 = new ReservationRequest(resource.getId(), start.plusHours(1),
                                end.plusHours(1));
                mockMvc.perform(post("/reservations")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req2)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.msg")
                                                .value("Resource is already reserved for the requested time"));
        }

        @Test
        void testInvalidTimeRangeFails() throws Exception {
                Resource resource = resourceRepository
                                .save(new Resource("Camera", "DSLR Camera", new BigDecimal("40.00"), true));
                String userToken = obtainToken("User", "User@123");

                // Start time after end time
                LocalDateTime start = LocalDateTime.now().plusDays(2);
                LocalDateTime end = start.minusHours(1);

                ReservationRequest req = new ReservationRequest(resource.getId(), start, end);
                mockMvc.perform(post("/reservations")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.msg").value("Start time must be before end time"));
        }

        @Test
        void testSearchAndPaginationAndFiltering() throws Exception {
                Resource r1 = resourceRepository
                                .save(new Resource("Resource 1", "Desc", new BigDecimal("50.00"), true));
                Resource r2 = resourceRepository
                                .save(new Resource("Resource 2", "Desc", new BigDecimal("200.00"), true));

                SystemUser user = userRepository.findByUsername("User").orElseThrow();

                // Create reservations directly with different prices and statuses
                reservationRepository.save(new Reservation(r1, user, new BigDecimal("50.00"),
                                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(2),
                                ReservationStatus.CONFIRMED));
                reservationRepository.save(new Reservation(r2, user, new BigDecimal("200.00"),
                                LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(2),
                                ReservationStatus.PENDING));

                String userToken = obtainToken("User", "User@123");

                // Search with status filter
                mockMvc.perform(get("/reservations?status=CONFIRMED")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));

                // Search with price filter (minPrice = 100)
                mockMvc.perform(get("/reservations?minPrice=100.00")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].price").value(200.00));

                // Search with sorting (price,desc)
                mockMvc.perform(get("/reservations?sort=price,desc")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].price").value(200.00))
                                .andExpect(jsonPath("$.content[1].price").value(50.00));
        }
}
