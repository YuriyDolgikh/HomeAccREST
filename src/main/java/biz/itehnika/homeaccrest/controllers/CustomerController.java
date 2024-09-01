package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.CustomerDTO;
import biz.itehnika.homeaccrest.dto.CustomerFiltersDTO;
import biz.itehnika.homeaccrest.dto.CustomerPeriodDTO;
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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Customer activity", description = "Customer operations are available with administrator rights (except Filters and Settings)")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    
    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    @Operation(
        summary = "Get list of all customers",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "OK",
                     content =  @Content(mediaType = "application/json",
                                         schema = @Schema(
                                            example = "[{\"id\":1,\"firstName\":\"admin\",\"email\":\"admin@example.com\"}," +
                                                       "{\"id\":2,\"firstName\":\"testUser\",\"email\":\"user@example.com\"}]"))),
        @ApiResponse(responseCode = "401",
                     description = "Unauthorized",
                     content = { @Content(mediaType = "application/json") })
        }
    )
    @GetMapping("/customers")
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
    @DeleteMapping(value = "/customers")     // TODO - ADMIN role required
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ResponseEntity<HttpStatus> deleteCustomers(@Parameter(schema = @Schema(example = "[2, 13, 1567]"))
                                                      @RequestBody List<Long> toDeleteList) {
        if (toDeleteList != null && !toDeleteList.isEmpty()) {
            customerService.deleteCustomers(toDeleteList);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
//    @Operation(
//        summary = "Delete customer by the id",
//        description = "Customer with ADMIN role cannot be deleted"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "OK",
//            content = { @Content(mediaType = "application/json")}),
//        @ApiResponse(responseCode = "401", description = "Unauthorized",
//            content = { @Content(mediaType = "application/json") })
//    }
//    )
//    @DeleteMapping(value = "/admin/delete/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") Long id) {
//        if (id != null) {
//            customerService.deleteCustomer(id);
//        }
//        return ResponseEntity.ok().build();
//    }
    
    
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
    @PutMapping(value = "/customers/{id}")     // TODO - update any users from admin page
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCustomer (@PathVariable(value = "id") Long id, @RequestBody CustomerUpdateDTO customerUpdateDTO) {
       
        if(!customerService.existsById(id)){
            return new ResponseEntity<>(new AppError("Customer with specified ID not exists"), HttpStatus.BAD_REQUEST);
        }
        if (customerService.findByEmail(customerUpdateDTO.getEmail()) != null &&
            !customerService.findByEmail(customerUpdateDTO.getEmail()).getId().equals(id)) {
            return new ResponseEntity<>(new AppError("Customer with specified E-mail already exists"), HttpStatus.BAD_REQUEST);
        }
        customerService.updateCustomer(id, customerUpdateDTO);
        return ResponseEntity.ok(null);
    }
    
    
    @Operation(
        summary = "Getting Active work period for current customer",
        description = "String date format: \"dd-MM-yyyy\" (\"08-02-2024\")"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = CustomerPeriodDTO.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json")})
    }
    )
    @GetMapping(value = "/period")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<CustomerPeriodDTO> getActivePeriod(Principal principal) {
        
        Customer customer = customerService.findByEmail(principal.getName());
        return ResponseEntity.ok(customerService.getActivePeriod(customer));
        
    }
    
    @Operation(
        summary = "Setting active work period for current customer",
        description = "String date format: \"dd-MM-yyyy\" (\"08-02-2024\")"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json")})
    }
    )
    @PostMapping(value = "/period")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<Void> setActivePeriod(@RequestBody CustomerPeriodDTO customerPeriodDTO, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        customerService.setActivePeriod(customerPeriodDTO, customer);

        return ResponseEntity.ok().build();
    }
    
    @Operation(
        summary = "Set TODAY as active work period for current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json")})
    }
    )
    @GetMapping(value = "/period/today")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<Void> setActivePeriodToday(Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        String today = LocalDate.now().format(dateFormatter);
        customerService.setActivePeriod(CustomerPeriodDTO.of(today, today), customer);
        return ResponseEntity.ok().build();
    }
    
    @Operation(
        summary = "Set THIS MONTH as active work period for current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json")})
    }
    )
    @GetMapping(value = "/period/month")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<Void> setActivePeriodMonth(Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        String startDay = LocalDate.now().withDayOfMonth(1).format(dateFormatter);
        String endDay = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).format(dateFormatter);
        customerService.setActivePeriod(CustomerPeriodDTO.of(startDay, endDay), customer);
        return ResponseEntity.ok().build();
    }
    
    
    @Operation(
        summary = "Getting filters for showing payments for current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = CustomerFiltersDTO.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json")})
    }
    )
    @GetMapping(value = "/filters")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<CustomerFiltersDTO> getFilters(Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        CustomerFiltersDTO filtersDTO = customerService.getFilters(customer);

        return ResponseEntity.ok(filtersDTO);
    }
    
    
    @Operation(
        summary = "Setting filters for showing payments for current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = {@Content(mediaType = "application/json")})
    }
    )
    @PostMapping(value = "/filters")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Void> setFilters(@RequestBody CustomerFiltersDTO customerFiltersDTO, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        customerService.setFilter(customerFiltersDTO, customer);
        return ResponseEntity.ok().build();
    }
    
}
