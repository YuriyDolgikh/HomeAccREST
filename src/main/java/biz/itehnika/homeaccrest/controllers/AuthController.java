package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.*;
import biz.itehnika.homeaccrest.exceptions.AppError;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.services.CustomerService;
import biz.itehnika.homeaccrest.services.InvalidTokenService;
import biz.itehnika.homeaccrest.services.UserDetailsServiceImpl;
import biz.itehnika.homeaccrest.utils.JwtTokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;


@Tag(name = "Security & Authentication")
@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final InvalidTokenService invalidTokenService;
    
    
    @Value("${jwt.lifetime}")
    private Duration duration;
    
    @Operation(
        summary = "Create JWT-token after login",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(
                        example = "{" +
                                      "\"user\":{\"id\":42,\"email\":\"john@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\"},"+
                                    "\"token\":\"eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzIwMjQ1NDg3LCJleHAiOjE3MjAyNTA4ODd9.6tQPCbF35tfDiIxXlx5lcsBai1irxdbqzyzg2mVzlKQ\"" +
                                  "}"
                )) }),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(
                        example = "{\"message\":\"Wrong e-mail or password\",\"timestamp\":\"2024-07-05T18:41:44.902+00:00\"}"
                )) })
                          }
    )
    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthToken(@Parameter(schema = @Schema(example = "{\"email\":\"userEmail\",\"password\":\"userPassword\"}"))
                                             @RequestBody JwtRequestDTO authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError("Wrong e-mail or password"), HttpStatus.UNAUTHORIZED);
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        Customer customer = customerService.findByEmail(authRequest.getEmail());
        String token = jwtTokenUtils.generateToken(userDetails);
        CustomerDTO user = new CustomerDTO(customer.getId(), customer.getEmail(), customer.getFirstName(), customer.getLastName());
        return ResponseEntity.ok(new JwtResponseDTO(user, token));
    }
    
    @Operation(
        summary = "Register new Customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "400", description = "Bad Request",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(implementation = AppError.class)) })
                          }
    )
    @PostMapping("/auth/register")
    public ResponseEntity<?> createNewCustomer(@Parameter(schema = @Schema(example = "{\"email\":\"bill@mail.com\",\"password\":\"pass123\",\"confirmPassword\":\"pass123\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                                               @RequestBody CustomerRegistrationDTO customerRegistrationDTO) {
        if (!customerRegistrationDTO.getPassword().equals(customerRegistrationDTO.getConfirmPassword())) {
            return new ResponseEntity<>(new AppError("Passwords do not match"), HttpStatus.BAD_REQUEST);
        }
        if (customerService.findByEmail(customerRegistrationDTO.getEmail()) != null) {
            return new ResponseEntity<>(new AppError("Customer with specified E-mail already exists"), HttpStatus.BAD_REQUEST);
        }
        customerService.createNewCustomer(customerRegistrationDTO);
        return ResponseEntity.ok(null);
    }
    
    
    @Operation(
        summary = "Logout Customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = AppError.class)) })
    }
    )
    @GetMapping("/auth/logout")
    public ResponseEntity<?> logoutCustomer(HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                if (invalidTokenService.existsByToken(token)){
                    throw new LoginException();
                }
                jwtTokenUtils.getUsername(token);
            } catch (ExpiredJwtException e) {
                log.debug("Время жизни токена вышло");
            } catch (LoginException e) {
                log.debug("Пользователь не авторизован");
            } catch (SecurityException e) {
                log.debug("Подпись неправильная");
            }
        }
        invalidTokenService.addToBlackList(new InvalidTokenDTO(token, LocalDateTime.now().plusMinutes(duration.toMinutesPart())));
  
        return ResponseEntity.ok().build();
    }
    
    @Operation(
        summary = "Get current Customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "OK",
            content =  @Content(mediaType = "application/json",
                schema = @Schema(
                    example = "{\"id\":78,\"email\":\"john@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @GetMapping("/auth/current")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public CustomerDTO getCurrentCustomer(Principal principal){
        Customer customer = customerService.findByEmail(principal.getName());
        return CustomerDTO.of(customer);
    }
   
}
