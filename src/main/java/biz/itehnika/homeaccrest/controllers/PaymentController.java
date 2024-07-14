package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.CustomerFiltersDTO;
import biz.itehnika.homeaccrest.dto.CustomerPeriodDTO;
import biz.itehnika.homeaccrest.models.*;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static biz.itehnika.homeaccrest.services.CustomerService.getCustomerDateTime;


@Controller
@RequiredArgsConstructor
public class PaymentController {
    public final PaymentService paymentService;
    public final PaymentCategoryService paymentCategoryService;
    public final CustomerService customerService;
    public final CurrencyService currencyService;
    public final AccountService accountService;

 
    

//
//    @GetMapping("/accounting")      //TODO Don't set Dates
//    public String accounting(Model model) {
//        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
//
//        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
//        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
//
//        LocalDate startDate = workPeriod.get("startDate");
//        LocalDate endDate = workPeriod.get("endDate");
//        List<Payment> payments = paymentService.getPaymentsByCustomerAndAllFilters(customer);
//        Map<String, Double> statistic = paymentService.getStatistic(customer);
//        model.addAttribute("statistic", statistic);
//        model.addAttribute("startDate", startDate);
//        model.addAttribute("endDate", endDate);
//        model.addAttribute("payments", payments);
//        model.addAttribute("filters", filters);
//
//        return "accounting";
//    }

    
    @PostMapping(value = "/payments/add")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
        public String addNewPayment(@RequestParam String dateTime,
                                @RequestParam String accountName,
                                @RequestParam String paymentCategoryName,
                                @RequestParam String description,
                                @RequestParam String direction,
                                @RequestParam String status,
                                @RequestParam Double amount,
                                Model model) {

        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        Account account = accountService.getAccountByNameAndCustomer(accountName, customer);
        PaymentCategory paymentCategory = paymentCategoryService.getByNameAndCustomer(paymentCategoryName, customer);
        CurrencyName currencyName = account.getCurrencyName();

        paymentService.addPayment(LocalDateTime.parse(dateTime),
                                Boolean.valueOf(direction),
                                Boolean.valueOf(status),
                                amount,
                                currencyName,
                                description,
                                paymentCategory,
                                account,
                                customer);
        CustomerFiltersDTO customerFiltersDTO = customerService.getFilters(customer);
//        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
//        model.addAttribute("added", true);
//        model.addAttribute("filters", filters);

        return "redirect:/accounting";
    }

    @PostMapping(value = "/payments/delete")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String deletePayments(@RequestParam(name = "toDelete", required = false) List<Long> ids, Model model) {
        if (ids != null && !ids.isEmpty()) {
            paymentService.deletePayments(ids);
        }
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        CustomerPeriodDTO customerPeriodDTO = customerService.getActivePeriod(customer);
        CustomerFiltersDTO customerFiltersDTO = customerService.getFilters(customer);
//        model.addAttribute("filters", filters);
//        model.addAttribute("startDate", workPeriod.get("startDate").format(dateFormatter));
//        model.addAttribute("endDate", workPeriod.get("endDate").format(dateFormatter));
//        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));

        return "redirect:/accounting";
    }
    
    @DeleteMapping(value = "/payments/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String deletePayment(@RequestParam(name = "toDelete", required = false) List<Long> ids, Model model) {
        if (ids != null && !ids.isEmpty()) {
            paymentService.deletePayments(ids);
        }
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        CustomerPeriodDTO customerPeriodDTO = customerService.getActivePeriod(customer);
        CustomerFiltersDTO customerFiltersDTO = customerService.getFilters(customer);
//        model.addAttribute("filters", filters);
//        model.addAttribute("startDate", workPeriod.get("startDate").format(dateFormatter));
//        model.addAttribute("endDate", workPeriod.get("endDate").format(dateFormatter));
//        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        
        return "redirect:/accounting";
    }

    @GetMapping("/payments/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String updatePayment(@PathVariable(value = "id") Long id, Model model) {

        Payment payment = paymentService.getById(id);
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());

        model.addAttribute("id", id);
        model.addAttribute("dateTime", payment.getDateTime());
        model.addAttribute("direction", payment.getDirection());
        model.addAttribute("status", payment.getStatus());
        model.addAttribute("amount", payment.getAmount());
        model.addAttribute("account", payment.getAccount());
        model.addAttribute("description", payment.getDescription());
        model.addAttribute("paymentCategory", payment.getPaymentCategory());
        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
        model.addAttribute("paymentCategories", paymentCategoryService.getPaymentCategoriesByCustomer(customer));

        return "updatePayment";
    }


    @PostMapping(value = "/payments/exchange")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String currencyExchange(@RequestParam String dateTime,
                                   @RequestParam String srcAccountName,
                                   @RequestParam String dstAccountName,
                                   @RequestParam Double amount,
                                   Model model) {

        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        Account accountSrc = accountService.getAccountByNameAndCustomer(srcAccountName, customer);
        Account accountDst = accountService.getAccountByNameAndCustomer(dstAccountName, customer);
        CurrencyName srcCurrencyName = accountSrc.getCurrencyName();
        CurrencyName dstCurrencyName = accountDst.getCurrencyName();
        LocalDate localDate = LocalDate.parse(dateTime.split("T")[0]);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);
        model.addAttribute("dateTime", getCustomerDateTime());
        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
        model.addAttribute("balances", accountService.getAccountBalancesByCustomer(customer));
        model.addAttribute("currencyRatesEUR", currencyService.getCurrencyByNameToday(CurrencyName.EUR));
        model.addAttribute("currencyRatesUSD", currencyService.getCurrencyByNameToday(CurrencyName.USD));

        if (srcCurrencyName.equals(dstCurrencyName)){
            model.addAttribute("statusMsg", "Accounts must have different currencies!");
            return "currencyExchange";
        }
        if (!currencyService.isRatesExistByDate(localDate)){
            model.addAttribute("statusMsg", "There are no exchange rates on this date!");
            return "currencyExchange";
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

        paymentService.currencyExchange(accountSrc, accountDst, amount, amount*rate, localDateTime, customer);

        CustomerPeriodDTO customerPeriodDTO = customerService.getActivePeriod(customer);
        CustomerFiltersDTO customerFiltersDTO = customerService.getFilters(customer);

//        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
//        model.addAttribute("workPeriod", workPeriod);
//        model.addAttribute("filters", filters);

        return "redirect:/accounting";
    }


    @PostMapping(value = "/payments/transfer")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String transferBetweenOwnAccounts(@RequestParam String dateTime,
                                   @RequestParam String srcAccountName,
                                   @RequestParam String dstAccountName,
                                   @RequestParam Double amount,
                                   Model model) {

        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        Account accountSrc = accountService.getAccountByNameAndCustomer(srcAccountName, customer);
        Account accountDst = accountService.getAccountByNameAndCustomer(dstAccountName, customer);
        CurrencyName srcCurrencyName = accountSrc.getCurrencyName();
        CurrencyName dstCurrencyName = accountDst.getCurrencyName();
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);
//        model.addAttribute("dateTime", ZonedDateTime.now().format(dateTimeFormatter));
//        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
//        model.addAttribute("balances", accountService.getAccountBalancesByCustomer(customer));

        if (!srcCurrencyName.equals(dstCurrencyName)){
            model.addAttribute("statusMsg", "Accounts must have the same currencies!");
            return "transfer";
        }

        paymentService.transferToOwnAccount(accountSrc, accountDst, amount, localDateTime, customer);
        
        CustomerPeriodDTO customerPeriodDTO = customerService.getActivePeriod(customer);
        CustomerFiltersDTO customerFiltersDTO = customerService.getFilters(customer);

//        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
//        model.addAttribute("workPeriod", workPeriod);
//        model.addAttribute("filters", filters);

        return "redirect:/accounting";
    }

}
