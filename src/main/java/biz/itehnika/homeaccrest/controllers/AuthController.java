package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.CustomerRegistrationDTO;
import biz.itehnika.homeaccrest.dto.JwtRequestDTO;
import biz.itehnika.homeaccrest.dto.JwtResponseDTO;
import biz.itehnika.homeaccrest.exceptions.AppError;
import biz.itehnika.homeaccrest.services.CustomerService;
import biz.itehnika.homeaccrest.services.UserDetailsServiceImpl;
import biz.itehnika.homeaccrest.utils.JwtTokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Security & Authentication")
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    
    
    @Operation(
        summary = "Create JWT-token after login",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(
                        example = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzIwMjQ1NDg3LCJleHAiOjE3MjAyNTA4ODd9.6tQPCbF35tfDiIxXlx5lcsBai1irxdbqzyzg2mVzlKQ\"}")) }),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(
                        example = "{\"message\":\"Wrong login or password\",\"timestamp\":\"2024-07-05T18:41:44.902+00:00\"}"
                )) })
                          }
    )
    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@Parameter(schema = @Schema(example = "{\"login\":\"userLogin\",\"password\":\"passw\"}", required = true))
                                             @RequestBody JwtRequestDTO authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError("Wrong login or password"), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getLogin());
        String token = jwtTokenUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponseDTO(token));
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
    @PostMapping("/registration")
    public ResponseEntity<?> createNewCustomer(@Parameter(schema = @Schema(example = "{\"login\":\"Bill\",\"password\":\"pass123\",\"confirmPassword\":\"pass123\",\"email\":\"bill@mail.com\"}", required = true))
                                               @RequestBody CustomerRegistrationDTO customerRegistrationDTO) {
        if (!customerRegistrationDTO.getPassword().equals(customerRegistrationDTO.getConfirmPassword())) {
            return new ResponseEntity<>(new AppError("Passwords do not match"), HttpStatus.BAD_REQUEST);
        }
        if (customerService.findByLogin(customerRegistrationDTO.getLogin()) != null) {
            return new ResponseEntity<>(new AppError("Customer with specified name already exists"), HttpStatus.BAD_REQUEST);
        }
        if (customerService.findByEmail(customerRegistrationDTO.getEmail()) != null) {
            return new ResponseEntity<>(new AppError("Customer with specified E-mail already exists"), HttpStatus.BAD_REQUEST);
        }
        customerService.createNewCustomer(customerRegistrationDTO);
        return ResponseEntity.ok(null);
    }
}
