package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.*;
import biz.itehnika.homeaccrest.exceptions.AppError;
import biz.itehnika.homeaccrest.models.*;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.services.*;
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
import java.time.LocalDateTime;
import java.util.List;


@Tag(name = "Payment activity",
     description = "All Payments operations are available for customer in context with USERs role")
@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {
    public final PaymentService paymentService;
    public final PaymentCategoryService paymentCategoryService;
    public final CustomerService customerService;
    public final CurrencyService currencyService;
    public final AccountService accountService;
    
    
    
    
    @Operation(
        summary = "Get a list of payments by the period and settings of the current customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "OK",
            content =  @Content(mediaType = "application/json",
                schema = @Schema(
                    example = "[{\"id\":54," +
                                "\"dateTime\":\"21-07-2024 15:18\"," +
                                "\"direction\":true," +
                                "\"status\":false," +
                                "\"amount\":456.78," +
                                "\"currencyName\":\"EUR\"," +
                                "\"description\":\"Service maintenance\"," +
                                "\"paymentCategoryName\":\"CAR\"," +
                                "\"accountName\":\"Car service\"}," +
                        
                               "{\"id\":128," +
                                "\"dateTime\":\"23-07-2024 09:30\"," +
                                "\"direction\":false," +
                                "\"status\":false," +
                                "\"amount\":99.00," +
                                "\"currencyName\":\"UAH\"," +
                                "\"description\":\"Supermarket\"," +
                                "\"paymentCategoryName\":\"FOOD\"," +
                                "\"accountName\":\"Novus\"}" +
                              "]"))),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> paymentsList(Principal principal){
        Customer customer = customerService.findByEmail(principal.getName());
        List<PaymentDTO> paymentDTOList = PaymentDTO.listOf(paymentService.getPaymentsByCustomerAndAllFilters(customer));
        return new ResponseEntity<>(paymentDTOList, HttpStatus.OK);
    }
    
    
    @Operation(
        summary = "Add new payment for customer",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") }),
        @ApiResponse(responseCode = "400", description = "Bad Request",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(implementation = AppError.class)) })
    }
    )
    @PostMapping(value = "/payments/new")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> newPayment(@Parameter(schema = @Schema(example = "{\"dateTime\":\"23-07-2024 09:30\"," +
                                                                               "\"direction\":false," +
                                                                               "\"status\":true," +
                                                                               "\"amount\":127.89," +
                                                                               "\"description\":\"Supermarket\"," +
                                                                               "\"paymentCategoryName\":\"FOOD\"," +
                                                                               "\"accountName\":\"My wallet\"}"
                                                                   )
                                                  )
                                         @RequestBody PaymentCreateUpdateDTO paymentCreateUpdateDTO, Principal principal) {
        
        Customer customer = customerService.findByEmail(principal.getName());
        
        if (paymentCategoryService.getByNameAndCustomer(paymentCreateUpdateDTO.getPaymentCategoryName(), customer) == null) {
            return new ResponseEntity<>(new AppError("Category with specified name does not exist"), HttpStatus.BAD_REQUEST);
        }
        if (accountService.getAccountByNameAndCustomer(paymentCreateUpdateDTO.getAccountName(), customer) == null) {
            return new ResponseEntity<>(new AppError("Account with specified name does not exist"), HttpStatus.BAD_REQUEST);
        }
        paymentService.addPayment(paymentCreateUpdateDTO, customer);
        return ResponseEntity.ok().build();
    }
    
    
    @Operation(
        summary = "Delete payments for customer by the payments id's list",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @PostMapping(value = "/payments/delete")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ResponseEntity<Void> deletePayments(@Parameter(schema = @Schema(example = "[56, 95, 134]"))
                                                 @RequestBody List<Long> toDeleteList, Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        if (toDeleteList != null && !toDeleteList.isEmpty()) {
            paymentService.deletePayments(toDeleteList, customer);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Delete payment by the payment id",
        description = ""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json")}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = { @Content(mediaType = "application/json") })
    }
    )
    @DeleteMapping(value = "/payments/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<Void> deletePayment(@PathVariable("id") Long id , Principal principal) {
        Customer customer = customerService.findByEmail(principal.getName());
        if (id != null) {
            paymentService.deletePayment(id, customer);
        }
        return ResponseEntity.ok().build();
    }
    
    
    @Operation(
        summary = "Update payment by ID",
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
    @PutMapping("/payments/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> updatePayment(@PathVariable(value = "id") Long id,
                                           @Parameter(schema = @Schema(example = "{\"dateTime\":\"23-07-2024 09:30\"," +
                                               "\"direction\":false," +
                                               "\"status\":true," +
                                               "\"amount\":127.89," +
                                               "\"description\":\"Supermarket\"," +
                                               "\"paymentCategoryName\":\"FOOD\"," +
                                               "\"accountName\":\"My wallet\"}"
                                           )
                                           )
                                           @RequestBody PaymentCreateUpdateDTO paymentCreateUpdateDTO, Principal principal) {
        
        Customer customer = customerService.findByEmail(principal.getName());
        if(!paymentService.existsById(id)){
            return new ResponseEntity<>(new AppError("Payment with specified ID not exists"), HttpStatus.BAD_REQUEST);
        }else {
            if (!paymentService.getById(id).getCustomer().getId().equals(customer.getId())){
                return new ResponseEntity<>(new AppError("Payment ID is wrong for this customer"), HttpStatus.BAD_REQUEST);
            }
        }

        paymentService.updatePayment(id, paymentCreateUpdateDTO);
        return ResponseEntity.ok().build();
    }
    
    
    @Operation(
        summary = "Exchange currency for the current customer",
        description = "Accounts must have different currencies"
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
    @PostMapping(value = "/payments/exchange")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> currencyExchange(@Parameter(schema = @Schema(example = "{\"dateTime\":\"23-07-2024 09:30\"," +
                                                                                     "\"srcAccountId\":152," +
                                                                                     "\"dstAccountId\":37," +
                                                                                     "\"amount\":127.89}"
                                                                         )
                                              )
                                              @RequestBody PaymentExchTransDTO paymentExchTransDTO, Principal principal) {

        Customer customer = customerService.findByEmail(principal.getName());
        if(!accountService.existsById(paymentExchTransDTO.getSrcAccountId()) ||
           !accountService.existsById(paymentExchTransDTO.getDstAccountId())){
            return new ResponseEntity<>(new AppError("Account(s) with specified ID(s) not exists"), HttpStatus.BAD_REQUEST);
        }else {
            if (!accountService.getById(paymentExchTransDTO.getSrcAccountId()).getCustomer().getId().equals(customer.getId()) ||
                !accountService.getById(paymentExchTransDTO.getDstAccountId()).getCustomer().getId().equals(customer.getId())){
                return new ResponseEntity<>(new AppError("Account ID(s) is(are) wrong for this customer"), HttpStatus.BAD_REQUEST);
            }
        }
        Account srcAccount = accountService.getById(paymentExchTransDTO.getSrcAccountId());
        Account dstAccount = accountService.getById(paymentExchTransDTO.getDstAccountId());
        Double amount = paymentExchTransDTO.getAmount();
        CurrencyName srcCurrencyName = srcAccount.getCurrencyName();
        CurrencyName dstCurrencyName = dstAccount.getCurrencyName();
        LocalDate localDate = LocalDate.parse(paymentExchTransDTO.getDateTime().split(" ")[0]);
        LocalDateTime localDateTime = LocalDateTime.parse(paymentExchTransDTO.getDateTime());

        if (srcCurrencyName.equals(dstCurrencyName)){
            return new ResponseEntity<>(new AppError("Accounts must have different currencies"), HttpStatus.BAD_REQUEST);
        }
        if (!currencyService.isRatesExistByDate(localDate)){
            return new ResponseEntity<>(new AppError("There are no exchange rates on this date"), HttpStatus.BAD_REQUEST);
        }
        Double rate;
        if (!srcCurrencyName.equals(CurrencyName.UAH)){
            Currency srcCurrencyRates = currencyService.getCurrencyByNameAndDate(srcCurrencyName, localDate);
            if (dstCurrencyName.equals(CurrencyName.UAH)){
                rate = srcCurrencyRates.getBuyRate();
            }else{
                Currency dstCurrencyRates = currencyService.getCurrencyByNameAndDate(dstCurrencyName, localDate);
                rate = srcCurrencyRates.getBuyRate() / dstCurrencyRates.getSaleRate();
            }
        }else{
            Currency dstCurrencyRates = currencyService.getCurrencyByNameAndDate(dstCurrencyName, localDate);
                rate = 1 / dstCurrencyRates.getSaleRate();
        }

        paymentService.currencyExchange(srcAccount, dstAccount, amount, amount*rate, localDateTime, customer);
        
        return ResponseEntity.ok(null);
    }
    
    
    @Operation(
        summary = "Transfer money between accounts for the current customer",
        description = "Accounts must have the same currencies"
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
    @PostMapping(value = "/payments/transfer")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> transferBetweenOwnAccounts(@Parameter(schema = @Schema(example = "{\"dateTime\":\"23-07-2024 09:30\"," +
                                                                                    "\"srcAccountId\":152," +
                                                                                    "\"dstAccountId\":37," +
                                                                                    "\"amount\":127.89}"
                                                                        )
                                                       )
                                                 @RequestBody PaymentExchTransDTO paymentExchTransDTO, Principal principal) {
        
        Customer customer = customerService.findByEmail(principal.getName());
        if(!accountService.existsById(paymentExchTransDTO.getSrcAccountId()) ||
            !accountService.existsById(paymentExchTransDTO.getDstAccountId())){
            return new ResponseEntity<>(new AppError("Account(s) with specified ID(s) not exists"), HttpStatus.BAD_REQUEST);
        }else {
            if (!accountService.getById(paymentExchTransDTO.getSrcAccountId()).getCustomer().getId().equals(customer.getId()) ||
                !accountService.getById(paymentExchTransDTO.getDstAccountId()).getCustomer().getId().equals(customer.getId())){
                return new ResponseEntity<>(new AppError("Account ID(s) is(are) wrong for this customer"), HttpStatus.BAD_REQUEST);
            }
        }
        Account srcAccount = accountService.getById(paymentExchTransDTO.getSrcAccountId());
        Account dstAccount = accountService.getById(paymentExchTransDTO.getDstAccountId());
        Double amount = paymentExchTransDTO.getAmount();
        CurrencyName srcCurrencyName = srcAccount.getCurrencyName();
        CurrencyName dstCurrencyName = dstAccount.getCurrencyName();
        LocalDateTime localDateTime = LocalDateTime.parse(paymentExchTransDTO.getDateTime());
        
        
        if (!srcCurrencyName.equals(dstCurrencyName)){
            return new ResponseEntity<>(new AppError("Accounts must have the same currencies"), HttpStatus.BAD_REQUEST);
        }

        paymentService.transferToOwnAccount(srcAccount, dstAccount, amount, localDateTime, customer);

        return ResponseEntity.ok().build();
    }

}
