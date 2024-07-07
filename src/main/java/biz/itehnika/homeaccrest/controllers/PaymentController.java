package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.models.*;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.services.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static biz.itehnika.homeaccrest.services.CustomerService.getCustomerDateTime;


@Controller
public class PaymentController {
    public final PaymentService paymentService;
    public final PaymentCategoryService paymentCategoryService;
    public final CustomerService customerService;
    public final CurrencyService currencyService;
    public final AccountService accountService;

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PaymentController(PaymentService paymentService, PaymentCategoryService paymentCategoryService, CustomerService customerService, CurrencyService currencyService, AccountService accountService) {
        this.paymentService = paymentService;
        this.paymentCategoryService = paymentCategoryService;
        this.customerService = customerService;
        this.currencyService = currencyService;
        this.accountService = accountService;
    }

    @PostMapping(value = "/setPeriod")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String setPeriod(@RequestParam(value = "startDate") String startDate,
                            @RequestParam(value = "endDate") String endDate,
                            Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        customerService.setWorkPeriod(customer.getId(),LocalDate.parse(startDate), LocalDate.parse(endDate));
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
        Map<String, Double> statistic = paymentService.getStatistic(customer);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("filters", filters);
        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("statistic", statistic);
        return "accounting";
    }

    @GetMapping(value = "/setPeriodToday")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String setPeriodToday(Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        LocalDate now = LocalDate.now();
        customerService.setWorkPeriod(customer.getId(),now, now);
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
        Map<String, Double> statistic = paymentService.getStatistic(customer);
        model.addAttribute("startDate", now.format(dateFormatter));
        model.addAttribute("endDate", now.format(dateFormatter));
        model.addAttribute("filters", filters);
        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("statistic", statistic);
        return "accounting";
    }

    @GetMapping(value = "/setPeriodMonth")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String setPeriodMonth(Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        customerService.setWorkPeriod(customer.getId(),startDate, endDate);
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
        Map<String, Double> statistic = paymentService.getStatistic(customer);
        model.addAttribute("startDate", startDate.format(dateFormatter));
        model.addAttribute("endDate", endDate.format(dateFormatter));
        model.addAttribute("filters", filters);
        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("statistic", statistic);
        return "accounting";
    }

    @PostMapping(value = "/setFilters")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String setFilters(@RequestParam(name = "newFilters", required = false) List<String> filtersList, Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());

        customerService.setFilter(customer.getId(), customerService.translateFiltersToMap(filtersList));
        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
        List<Payment> payments = paymentService.getPaymentsByCustomerAndAllFilters(customer);
        Map<String, Double> statistic = paymentService.getStatistic(customer);
        model.addAttribute("statistic", statistic);
        model.addAttribute("startDate", workPeriod.get("startDate").format(dateFormatter));
        model.addAttribute("endDate", workPeriod.get("endDate").format(dateFormatter));
        model.addAttribute("payments", payments);
        model.addAttribute("filters", filters);
        return "accounting";
    }

    @GetMapping("/accounting")      //TODO Don't set Dates
    public String accounting(Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());

        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());

        LocalDate startDate = workPeriod.get("startDate");
        LocalDate endDate = workPeriod.get("endDate");
        List<Payment> payments = paymentService.getPaymentsByCustomerAndAllFilters(customer);
        Map<String, Double> statistic = paymentService.getStatistic(customer);
        model.addAttribute("statistic", statistic);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("payments", payments);
        model.addAttribute("filters", filters);

        return "accounting";
    }

    @GetMapping("/addNewPayment")
    public String addNewPayment(Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());

        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
        model.addAttribute("paymentCategories", paymentCategoryService.getPaymentCategoriesByCustomer(customer));
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
//        model.addAttribute("filters", filters);
//        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("balances", accountService.getAccountBalancesByCustomer(customer));

        return "addNewPayment";
    }

    @PostMapping(value = "/addNewPayment")
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
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());

        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("added", true);
        model.addAttribute("filters", filters);

        return "redirect:/accounting";
    }

    @PostMapping(value = "/deletePayment")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public String deletePayment(@RequestParam(name = "toDelete", required = false) List<Long> ids, Model model) {
        if (ids != null && !ids.isEmpty()) {
            paymentService.deletePayments(ids);
        }
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
        model.addAttribute("filters", filters);
        model.addAttribute("startDate", workPeriod.get("startDate").format(dateFormatter));
        model.addAttribute("endDate", workPeriod.get("endDate").format(dateFormatter));
        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));

        return "redirect:/accounting";
    }

    @GetMapping("/updatePayment/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
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

    @PostMapping(value = "/updatePayment")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String updatePayment(@RequestParam Long id,
                                @RequestParam String dateTime,
                                @RequestParam Boolean direction,
                                @RequestParam Boolean status,
                                @RequestParam Double amount,
                                @RequestParam String description,
                                @RequestParam String paymentCategoryName,
                                @RequestParam String accountName,
                                Model model) {
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        Account account = accountService.getAccountByNameAndCustomer(accountName, customer);
        PaymentCategory  paymentCategory = paymentCategoryService.getByNameAndCustomer(paymentCategoryName,customer);
        paymentService.updatePayment(id,
                                     LocalDateTime.parse(dateTime),
                                     direction,
                                     status,
                                     amount,
                                     account.getCurrencyName(),
                                     description,
                                     paymentCategory,
                                     account);

        Payment payment = paymentService.getById(id);
        model.addAttribute("updated", true);
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

    @GetMapping(value = "/currencyExchange")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String currencyExchange(Model model){
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        model.addAttribute("dateTime", getCustomerDateTime());
        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
        model.addAttribute("balances", accountService.getAccountBalancesByCustomer(customer));
        model.addAttribute("currencyRatesEUR", currencyService.getCurrencyByNameToday(CurrencyName.EUR));
        model.addAttribute("currencyRatesUSD", currencyService.getCurrencyByNameToday(CurrencyName.USD));

        return "currencyExchange";
    }

    @PostMapping(value = "/currencyExchange")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
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

        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());

        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("workPeriod", workPeriod);
        model.addAttribute("filters", filters);

        return "redirect:/accounting";
    }

    @GetMapping(value = "/transfer")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String transferBetweenOwnAccounts(Model model){
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        model.addAttribute("dateTime", getCustomerDateTime());
        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
        model.addAttribute("balances", accountService.getAccountBalancesByCustomer(customer));

        return "transfer";
    }

    @PostMapping(value = "/transfer")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
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
        model.addAttribute("dateTime", ZonedDateTime.now().format(dateTimeFormatter));
        model.addAttribute("accounts", accountService.getAccountsByCustomer(customer));
        model.addAttribute("balances", accountService.getAccountBalancesByCustomer(customer));

        if (!srcCurrencyName.equals(dstCurrencyName)){
            model.addAttribute("statusMsg", "Accounts must have the same currencies!");
            return "transfer";
        }

        paymentService.transferToOwnAccount(accountSrc, accountDst, amount, localDateTime, customer);

        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());

        model.addAttribute("payments", paymentService.getPaymentsByCustomerAndAllFilters(customer));
        model.addAttribute("workPeriod", workPeriod);
        model.addAttribute("filters", filters);

        return "redirect:/accounting";
    }

}
