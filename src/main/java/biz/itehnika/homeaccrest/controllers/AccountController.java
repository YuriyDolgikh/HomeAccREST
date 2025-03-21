package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.AccountCreateUpdateDTO;
import biz.itehnika.homeaccrest.dto.AccountDTO;
import biz.itehnika.homeaccrest.exceptions.AppError;
import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.enums.AccountType;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.services.AccountService;
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
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Accounts activity",
     description = "All Accounts operations are available for customer in context with USERs role")
@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AccountController {

    public final AccountService accountService;
    public final CustomerService customerService;
    
    @Operation(
        summary = "Get a list of all accounts for the current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "OK",
            content =  @Content(mediaType = "application/json",
                schema = @Schema(
                    example = "[{\"id\":34,\"name\":\"Sparkassa\",\"description\":\"Perfect Red card\",\"type\":\"CARD\",\"currencyName\":\"EUR\",\"balance\":\"456.27\"}," +
                               "{\"id\":895,\"name\":\"To travel\",\"description\":\"Simply Cash\",\"type\":\"CASH\",\"currencyName\":\"UAH\",\"balance\":\"8154.05\"}]"))),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
        }
    )
    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public List<AccountDTO> accountsList(Principal principal){
        Customer customer = customerService.findByEmail(principal.getName());
        List<AccountDTO> accountDTOList = new ArrayList<>();
        for (Account account : accountService.getAccountsByCustomer(customer)){
            accountDTOList.add(AccountDTO.of(account));
        }
        return accountDTOList;
    }
    
    
    
    @Operation(
        summary = "Add new account for customer",
        description = "The name of account must be unique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "400", description = "Bad Request",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(implementation = AppError.class)) })
                          }
                )
    @PostMapping(value = "/accounts")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> newAccount(@Parameter(schema = @Schema(example = "{\"name\":\"To travel\",\"description\":\"Simply Cash\",\"type\":\"CASH\",\"currencyName\":\"UAH\",\"balance\":\"0.00\"}]"))
                                           @RequestBody AccountCreateUpdateDTO accountCreateUpdateDTO, Principal principal) {

        Customer customer = customerService.findByEmail(principal.getName());
        
        if (accountService.getAccountByNameAndCustomer(accountCreateUpdateDTO.getName(), customer) != null) {
            return new ResponseEntity<>(new AppError("Account with specified name for this customer already exists"), HttpStatus.BAD_REQUEST);
        }
        accountService.addAccount(accountCreateUpdateDTO, customer);
        return ResponseEntity.ok().build();
    }
    
 
    @Operation(
        summary = "Delete accounts for customer by the accounts id's list",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @DeleteMapping(value = "/accounts")
    @PreAuthorize("hasRole('ROLE_USER')")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ResponseEntity<HttpStatus> deleteAccounts(@Parameter(schema = @Schema(example = "[12, 133, 13457]"))
                                                     @RequestBody List<Long> toDeleteList, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        if (toDeleteList != null && !toDeleteList.isEmpty()) {
            accountService.deleteAccounts(toDeleteList, customer);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
 
    @Operation(
        summary = "Update account",
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
    @PutMapping(value = "/accounts/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateAccount (@PathVariable(value = "id") Long id,
                                            @RequestBody AccountCreateUpdateDTO accountCreateUpdateDTO, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        if(!accountService.existsById(id)){
            return new ResponseEntity<>(new AppError("Account with specified ID not exists"), HttpStatus.BAD_REQUEST);
        }else {
            if (!accountService.getById(id).getCustomer().getId().equals(customer.getId())){
                return new ResponseEntity<>(new AppError("Account ID is wrong for this customer"), HttpStatus.BAD_REQUEST);
            }
        }
        if (accountService.getAccountByNameAndCustomer(accountCreateUpdateDTO.getName(), customer) != null) {
            return new ResponseEntity<>(new AppError("Account with specified name already exists"), HttpStatus.BAD_REQUEST);
        }
        accountService.updateAccount(id, accountCreateUpdateDTO);
        return ResponseEntity.ok(null);
    }
    
    
    @Operation(
        summary = "Get a list of account types names",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "OK",
            content =  @Content(mediaType = "application/json",
                schema = @Schema(
                    example = "[" +
                              "\"CASH\"," +
                              "\"BANK\"," +
                              "\"CARD\"," +
                              "\"OTHER\"" +
                              "]"))),

        @ApiResponse(responseCode = "401",
            description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @GetMapping("/accounts/types")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public List<String> accountsTypesList(){
        List<String> typesList = new ArrayList<>();
        for (AccountType type : AccountType.values()){
            typesList.add(type.name());
        }
        return typesList;
    }
    
    
    @Operation(
        summary = "Get a list of currencies names",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "OK",
            content =  @Content(mediaType = "application/json",
                schema = @Schema(
                    example = "[" +
                        "\"UAH\"," +
                        "\"EUR\"," +
                        "\"USD\"" +
                        "]"))),
       
        @ApiResponse(responseCode = "401",
            description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @GetMapping("/accounts/currencies")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public List<String> currenciesList(){
        List<String> currNames = new ArrayList<>();
        for (CurrencyName currencyName : CurrencyName.values()){
            currNames.add(currencyName.name());
        }
        return currNames;
    }

}
