package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.CustomerDTO;
import biz.itehnika.homeaccrest.dto.CustomerUpdateDTO;
import biz.itehnika.homeaccrest.exceptions.AppError;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.services.CustomerService;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Tag(name = "Customer activity", description = "Customer operations are available with administrator rights")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    
  
    @Operation(
        summary = "Get list of all customers",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "OK",
                     content =  @Content(mediaType = "application/json",
                                         schema = @Schema(
                                            example = "[{\"id\":1,\"login\":\"admin\",\"email\":\"admin@example.com\"}," +
                                                       "{\"id\":2,\"login\":\"testUser\",\"email\":\"user@example.com\"}]"))),
        @ApiResponse(responseCode = "401",
                     description = "Unauthorized",
                     content = { @Content(mediaType = "application/json") })
        }
    )
    @GetMapping("/admin/customers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<CustomerDTO> customersList(){
        List<CustomerDTO> customerDTOList = new ArrayList<>();
        for (Customer customer : customerService.getAllCustomers()){
            customerDTOList.add(CustomerDTO.of(customer));
        }
        return customerDTOList;
    }
    
    
    @Operation(
        summary = "Delete customers by the id's list",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @PostMapping(value = "/admin/delete")     // TODO - ADMIN role required
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ResponseEntity<HttpStatus> deleteCustomers(@Parameter(schema = @Schema(example = "[2, 13, 1567]"))
                                                      @RequestBody List<Long> toDeleteList) {
        if (toDeleteList != null && !toDeleteList.isEmpty()) {
            customerService.deleteCustomers(toDeleteList);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
    @Operation(
        summary = "Delete customer by the id",
        description = "Customer with ADMIN role cannot be deleted"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @DeleteMapping(value = "/admin/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable("id") Long id) {
        if (id != null) {
            customerService.deleteCustomer(id);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
    @Operation(
        summary = "Update Customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "400", description = "Bad Request",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(implementation = AppError.class)) }),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @PutMapping(value = "/admin/update/{id}")     // TODO - update any users from admin page
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCustomer (@PathVariable(value = "id") Long id, @RequestBody CustomerUpdateDTO customerUpdateDTO) {
       
        if(!customerService.existsById(id)){
            return new ResponseEntity<>(new AppError("Customer with specified ID not exists"), HttpStatus.BAD_REQUEST);
        }
        if (customerService.findByLogin(customerUpdateDTO.getLogin()) != null &&
            !customerService.findByLogin(customerUpdateDTO.getLogin()).getId().equals(id)) {
            return new ResponseEntity<>(new AppError("Customer with specified name already exists"), HttpStatus.BAD_REQUEST);
        }
        if (customerService.findByEmail(customerUpdateDTO.getEmail()) != null &&
            !customerService.findByEmail(customerUpdateDTO.getEmail()).getId().equals(id)) {
            return new ResponseEntity<>(new AppError("Customer with specified E-mail already exists"), HttpStatus.BAD_REQUEST);
        }
        customerService.updateCustomer(id, customerUpdateDTO);
        return ResponseEntity.ok(null);
    }

    static User getCurrentUser() {
        return (User)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    static boolean isAdmin(User user) {
        Collection<GrantedAuthority> roles = user.getAuthorities();

        for (GrantedAuthority auth : roles) {
            if ("ROLE_ADMIN".equals(auth.getAuthority()))
                return true;
        }
        return false;
    }

}









// NEW Version

//@GetMapping("/unsecured")
//public String unsecuredData() {
//    return "Unsecured data";
//}
//
//@GetMapping("/secured")
//public String securedData() {
//    return "Secured data";
//}
//
//@GetMapping("/admin")
//public String adminData() {
//    return "Admin data";
//}
//
//@GetMapping("/info")
//public String userData(Principal principal) {
//    return principal.getName();
//}
// End NEW Version