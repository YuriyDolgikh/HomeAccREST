package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.models.*;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.repos.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final PaymentCategoryService paymentCategoryService;

    public PaymentService(PaymentRepository paymentRepository, CustomerService customerService, PaymentCategoryService paymentCategoryService) {
        this.paymentRepository = paymentRepository;
        this.customerService = customerService;
        this.paymentCategoryService = paymentCategoryService;
    }

    @Transactional(readOnly = true)
    public Payment getById(Long id){
        return paymentRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPaymentsByCustomerAndCurrencyName(Customer customer, CurrencyName currencyName){
        return paymentRepository.findByCustomerAndCurrencyName(customer, currencyName);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomerAndPeriod(Customer customer, LocalDate startDate, LocalDate endDate){
        return paymentRepository.findByCustomerAndDateTimeBetweenOrderByDateTime(customer,
                                              LocalDateTime.of(startDate, LocalTime.MIN),
                                              LocalDateTime.of(endDate, LocalTime.MAX));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomerAndAllFilters(Customer customer){
        Map<String, Boolean> filters = customerService.getFilters(customer.getId());
        Map<String, LocalDate> workPeriod = customerService.getWorkPeriod(customer.getId());
        List<CurrencyName> currencyNames = new ArrayList<>();
        List<Boolean> directions = new ArrayList<>();
        List<Boolean> statuses = new ArrayList<>();
        if (filters.get("isUAH")) currencyNames.add(CurrencyName.UAH);
        if (filters.get("isEUR")) currencyNames.add(CurrencyName.EUR);
        if (filters.get("isUSD")) currencyNames.add(CurrencyName.USD);
        if (filters.get("isIN")) directions.add(true);
        if (filters.get("isOUT")) directions.add(false);
        if (filters.get("isCompleted")) statuses.add(true);
        if (filters.get("isScheduled")) statuses.add(false);
        LocalDate startDate = workPeriod.get("startDate");
        LocalDate endDate = workPeriod.get("endDate");

        return paymentRepository.findByCustomerAndCurrencyNameInAndDirectionInAndStatusInAndDateTimeBetweenOrderByDateTimeAsc(
                                                    customer,
                                                    currencyNames,
                                                    directions,
                                                    statuses,
                                                    LocalDateTime.of(startDate, LocalTime.MIN),
                                                    LocalDateTime.of(endDate, LocalTime.MAX));
    }

    @Transactional
    public void addPayment(LocalDateTime dateTime,
                           Boolean direction,
                           Boolean status,
                           Double amount,
                           CurrencyName currencyName,
                           String description,
                           PaymentCategory paymentCategory,
                           Account account,
                           Customer customer){
        Payment payment = new Payment(dateTime, direction, status, amount, currencyName, description, paymentCategory, account, customer);
        paymentRepository.save(payment);
    }

    @Transactional
    public void deletePayments(List<Long> ids) {
        ids.forEach(id -> {
            Optional<Payment> payment = paymentRepository.findById(id);
            payment.ifPresent(u -> paymentRepository.deleteById(u.getId()));
        });
    }

    @Transactional
    public void updatePayment(Long id,
                                 LocalDateTime dateTime,
                                 Boolean direction,
                                 Boolean status,
                                 Double amount,
                                 CurrencyName currencyName,
                                 String description,
                                 PaymentCategory paymentCategory,
                                 Account account) {
        Payment paymentToUpdate = getById(id);

        paymentToUpdate.setDateTime(dateTime);
        paymentToUpdate.setDirection(direction);
        paymentToUpdate.setStatus(status);
        paymentToUpdate.setAmount(amount);
        paymentToUpdate.setCurrencyName(currencyName);
        paymentToUpdate.setDescription(description);
        paymentToUpdate.setPaymentCategory(paymentCategory);
        paymentToUpdate.setAccount(account);
        paymentRepository.save(paymentToUpdate);
    }

    @Transactional      //TODO need to do sums round ?
    public void currencyExchange(Account accountSrc, Account accountDst, Double sumSrc, Double sumDst,
                                                                    LocalDateTime dateTime, Customer customer){
        CurrencyName currencyNameSrc = accountSrc.getCurrencyName();
        CurrencyName currencyNameDst = accountDst.getCurrencyName();
        PaymentCategory paymentCategory = paymentCategoryService.getByNameAndCustomer("EXCHANGE", customer);
        String descriptionSrc = "Exchange " + String.format("%.2f",sumSrc) + " " + currencyNameSrc + " --> "
                       + String.format("%.2f",sumDst) + " " + currencyNameDst + " (account: '" + accountDst.getName() + "')";
        String descriptionDst = "Exchange " + String.format("%.2f",sumDst) + " " + currencyNameDst + " <-- "
                       + String.format("%.2f",sumSrc) + " " + currencyNameSrc + " (account: '" + accountSrc.getName() + "')";
        Payment paymentSrc = new Payment(dateTime, false, true, sumSrc,
                                                currencyNameSrc, descriptionSrc, paymentCategory, accountSrc, customer);
        Payment paymentDst = new Payment(dateTime, true, true, sumDst,
                                                currencyNameDst, descriptionDst, paymentCategory, accountDst, customer);
        paymentRepository.save(paymentSrc);
        paymentRepository.save(paymentDst);
    }

    @Transactional      //TODO need to do sums round ?
    public void transferToOwnAccount(Account accountSrc, Account accountDst, Double sum,
                                 LocalDateTime dateTime, Customer customer){
        CurrencyName currencyName = accountSrc.getCurrencyName();
        PaymentCategory paymentCategory = paymentCategoryService.getByNameAndCustomer("TRANSFER", customer);
        String descriptionSrc = "Send to account '" + accountDst.getName() + "'";
        String descriptionDst = "Receive from account '" + accountSrc.getName() + "'";
        Payment paymentSrc = new Payment(dateTime, false, true, sum,
                currencyName, descriptionSrc, paymentCategory, accountSrc, customer);
        Payment paymentDst = new Payment(dateTime, true, true, sum,
                currencyName, descriptionDst, paymentCategory, accountDst, customer);
        paymentRepository.save(paymentSrc);
        paymentRepository.save(paymentDst);
    }

    @Transactional(readOnly = true)
    public Double getTotalSumByCurrency(Customer customer, CurrencyName currencyName){
        Double totalSum = 0.0;
        List<Payment> payments = getAllPaymentsByCustomerAndCurrencyName(customer, currencyName);
        for (Payment payment : payments){
            if (payment.getDirection()){
                totalSum += payment.getAmount();
            }else {
                totalSum -= payment.getAmount();
            }
        }
        return totalSum;
    }
    @Transactional(readOnly = true)
    public Double getOnScreenSumByCurrency(Customer customer, CurrencyName currencyName){
        Double onScreenSum = 0.0;
        List<Payment> payments = getPaymentsByCustomerAndAllFilters(customer);
        for (Payment payment : payments){
            if (payment.getCurrencyName().equals(currencyName)){
                if (payment.getDirection()){
                    onScreenSum += payment.getAmount();
                }else {
                    onScreenSum -= payment.getAmount();
                }
            }
        }
        return onScreenSum;
    }

    @Transactional(readOnly = true)
    public Double getDailySumByCurrency(Customer customer, CurrencyName currencyName){
        Double dailySum = 0.0;
        List<Payment> payments = getPaymentsByCustomerAndPeriod(customer, LocalDate.now(), LocalDate.now());
        for (Payment payment : payments){
            if (payment.getCurrencyName().equals(currencyName)){
                if (payment.getDirection()){
                    dailySum += payment.getAmount();
                }else {
                    dailySum -= payment.getAmount();
                }
            }

        }
        return dailySum;
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getStatistic(Customer customer){     // TODO make strings inline
        Map<String, Double> statistic = new HashMap<>();

        Double totalSumUAH = getTotalSumByCurrency(customer, CurrencyName.UAH);
        Double totalSumEUR = getTotalSumByCurrency(customer, CurrencyName.EUR);
        Double totalSumUSD = getTotalSumByCurrency(customer, CurrencyName.USD);
        Double onScreenSumUAH = getOnScreenSumByCurrency(customer, CurrencyName.UAH);
        Double onScreenSumEUR = getOnScreenSumByCurrency(customer, CurrencyName.EUR);
        Double onScreenSumUSD = getOnScreenSumByCurrency(customer, CurrencyName.USD);
        Double dailySumUAH = getDailySumByCurrency(customer, CurrencyName.UAH);
        Double dailySumEUR = getDailySumByCurrency(customer, CurrencyName.EUR);
        Double dailySumUSD = getDailySumByCurrency(customer, CurrencyName.USD);
//        Currency currencyUAH = currencyService.getCurrencyByNameToday(CurrencyName.UAH);
//        Currency currencyEUR = currencyService.getCurrencyByNameToday(CurrencyName.EUR);
//        Currency currencyUSD = currencyService.getCurrencyByNameToday(CurrencyName.USD);

        statistic.put("totalSumUAH", totalSumUAH);
        statistic.put("totalSumEUR", totalSumEUR);
        statistic.put("totalSumUSD", totalSumUSD);
        statistic.put("onScreenSumUAH", onScreenSumUAH);
        statistic.put("onScreenSumEUR", onScreenSumEUR);
        statistic.put("onScreenSumUSD", onScreenSumUSD);
        statistic.put("dailySumUAH", dailySumUAH);
        statistic.put("dailySumEUR", dailySumEUR);
        statistic.put("dailySumUSD", dailySumUSD);
//        statistic.put("currencyUAH", currencyUAH);
//        statistic.put("currencyEUR", currencyEUR);
//        statistic.put("currencyUSD", currencyUSD);

        return statistic;
    }


}
