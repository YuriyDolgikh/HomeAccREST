package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.PaymentCategoryCreateUpdateDTO;
import biz.itehnika.homeaccrest.dto.PaymentCategoryDTO;
import biz.itehnika.homeaccrest.exceptions.AppError;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.services.CustomerService;
import biz.itehnika.homeaccrest.services.PaymentCategoryService;
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
import java.util.List;

@Tag(name = "Payment category activity",
     description = "All Payment Categories operations are available for customer in context with USERs role")
@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class PaymentCategoryController {

    private final CustomerService customerService;
    private final PaymentCategoryService paymentCategoryService;
    
    
    @Operation(
        summary = "Get a list of all payment categories of the current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "OK",
            content =  @Content(mediaType = "application/json",
                schema = @Schema(
                    example = "[{\"id\":54,\"name\":\"HEALTH\",\"description\":\"Medicines, clinics, food additives ...\"}," +
                              "{\"id\":734,\"name\":\"FOOD\",\"description\":\"Supermarkets, farmers markets, bakeries\"}]"))),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
        }
    )
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> categoriesList(Principal principal){
        Customer customer = customerService.findByEmail(principal.getName());
        List<PaymentCategoryDTO> paymentCategoryDTOList = PaymentCategoryDTO.listOf(paymentCategoryService.getPaymentCategoriesByCustomer(customer));
        return new ResponseEntity<>(paymentCategoryDTOList, HttpStatus.OK);
    }
    
    
    @Operation(
        summary = "Add new payment category for customer",
        description = "The name of category must be unique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "400", description = "Bad Request",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(implementation = AppError.class)) })
        }
    )
    @PostMapping(value = "/categories")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> newCategory(@Parameter(schema = @Schema(example = "{\"name\":\"HEALTH\",\"description\":\"Medicines, clinics, food additives ...\"}"))
                                         @RequestBody PaymentCategoryCreateUpdateDTO paymentCategoryCreateUpdateDTO, Principal principal) {

        Customer customer = customerService.findByEmail(principal.getName());
        
        if (paymentCategoryService.getByNameAndCustomer(paymentCategoryCreateUpdateDTO.getName(), customer) != null) {
            return new ResponseEntity<>(new AppError("Category with specified name for this customer already exists"), HttpStatus.BAD_REQUEST);
        }
        paymentCategoryService.addPaymentCategory(paymentCategoryCreateUpdateDTO, customer);
        return ResponseEntity.ok().build();
    }
    
    
    
    @Operation(
        summary = "Delete payment categories for customer by the categories id's list",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
        }
    )
    @DeleteMapping(value = "/categories")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ResponseEntity<Void> deleteCategories(@Parameter(schema = @Schema(example = "[56, 95, 134]"))
                                                       @RequestBody List<Long> toDeleteList, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        if (toDeleteList != null && !toDeleteList.isEmpty()) {
            paymentCategoryService.deletePaymentCategories(toDeleteList, customer);
        }
        return ResponseEntity.ok().build();
    }
    
   
    @Operation(
        summary = "Update payment category by ID",
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
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<?> updateCategory (@PathVariable(value = "id") Long id,
                                             @RequestBody PaymentCategoryCreateUpdateDTO categoryCreateUpdateDTO, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        if(!paymentCategoryService.existsById(id)){
            return new ResponseEntity<>(new AppError("Category with specified ID not exists"), HttpStatus.BAD_REQUEST);
        }else {
            if (!paymentCategoryService.getById(id).getCustomer().getId().equals(customer.getId())){
                return new ResponseEntity<>(new AppError("Category ID is wrong for this customer"), HttpStatus.BAD_REQUEST);
            }
        }
        if (paymentCategoryService.getByNameAndCustomer(categoryCreateUpdateDTO.getName(), customer) != null) {
            return new ResponseEntity<>(new AppError("Category with specified name already exists"), HttpStatus.BAD_REQUEST);
        }
        paymentCategoryService.updatePaymentCategory(id, categoryCreateUpdateDTO);
        return ResponseEntity.ok().build();
    }
    
    
    @Operation(
        summary = "Initialisation payment categories for current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @GetMapping(value = "/categories/init")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> initCategories(Principal principal){
        Customer customer = customerService.findByEmail(principal.getName());
        paymentCategoryService.initPaymentCategoriesForCustomer(customer);
        return ResponseEntity.ok().build();
    }
    
    
    @Operation(
        summary = "Initialisation payment categories for ADMIN",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @GetMapping(value = "/categories/init/admin")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> initAdminsCategories(){
        paymentCategoryService.initForAdmin();
        return ResponseEntity.ok().build();
    }

}
