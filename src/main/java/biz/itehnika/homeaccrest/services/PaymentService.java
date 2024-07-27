package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.dto.CustomerFiltersDTO;
import biz.itehnika.homeaccrest.dto.CustomerPeriodDTO;
import biz.itehnika.homeaccrest.dto.PaymentCreateUpdateDTO;
import biz.itehnika.homeaccrest.models.*;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.repos.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final PaymentCategoryService paymentCategoryService;
    
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final AccountService accountService;
    
    @Transactional(readOnly = true)
    public Payment getById(Long id){
        return paymentRepository.findById(id).orElseThrow();
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id){
        return paymentRepository.existsById(id);
    }

   
    @Transactional(readOnly = true)
    public List<Payment> getAllPaymentsByCurrencyName(CurrencyName currencyName, Customer customer){
        return paymentRepository.findByCustomerAndCurrencyName(customer, currencyName);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPaymentsByPeriod(CustomerPeriodDTO customerPeriodDTO, Customer customer){
        return paymentRepository.findByCustomerAndDateTimeBetweenOrderByDateTime(customer,
                                              LocalDateTime.of(LocalDate.parse(customerPeriodDTO.getStartDate(), dateFormatter), LocalTime.MIN),
                                              LocalDateTime.of(LocalDate.parse(customerPeriodDTO.getEndDate(), dateFormatter), LocalTime.MAX));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomerAndAllFilters(Customer customer){
        CustomerFiltersDTO customerFiltersDTO = customerService.getFilters(customer);
        CustomerPeriodDTO customerPeriodDTO = customerService.getActivePeriod(customer);
        List<CurrencyName> currencyNames = new ArrayList<>();
        List<Boolean> directions = new ArrayList<>();
        List<Boolean> statuses = new ArrayList<>();
        if (customerFiltersDTO.getIsUAH()) currencyNames.add(CurrencyName.UAH); //TODO Rewrite
        if (customerFiltersDTO.getIsEUR()) currencyNames.add(CurrencyName.EUR);
        if (customerFiltersDTO.getIsUSD()) currencyNames.add(CurrencyName.USD);
        if (customerFiltersDTO.getIsIN()) directions.add(true);
        if (customerFiltersDTO.getIsOUT()) directions.add(false);
        if (customerFiltersDTO.getIsCompleted()) statuses.add(true);
        if (customerFiltersDTO.getIsScheduled()) statuses.add(false);
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(customerPeriodDTO.getStartDate(), dateFormatter), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(customerPeriodDTO.getEndDate(), dateFormatter), LocalTime.MAX);

        return paymentRepository.findByCustomerAndCurrencyNameInAndDirectionInAndStatusInAndDateTimeBetweenOrderByDateTimeAsc(
                                                    customer,
                                                    currencyNames,
                                                    directions,
                                                    statuses,
                                                    startDateTime,
                                                    endDateTime);
    }

    @Transactional
    public void addPayment(PaymentCreateUpdateDTO paymentCreateUpdateDTO, Customer customer){
        Payment payment = new Payment(
                        LocalDateTime.parse(paymentCreateUpdateDTO.getDateTime(), dateTimeFormatter),
                        paymentCreateUpdateDTO.getDirection(),
                        paymentCreateUpdateDTO.getStatus(),
                        paymentCreateUpdateDTO.getAmount(),
                        accountService.getAccountByNameAndCustomer(paymentCreateUpdateDTO.getAccountName(), customer)
                                      .getCurrencyName(),
                        paymentCreateUpdateDTO.getDescription(),
                        paymentCategoryService.getByNameAndCustomer(paymentCreateUpdateDTO.getPaymentCategoryName(), customer),
                        accountService.getAccountByNameAndCustomer(paymentCreateUpdateDTO.getAccountName(), customer),
                                      customer);
        paymentRepository.save(payment);
    }

    @Transactional
    public void deletePayment(Long id, Customer customer) {
        Optional<Payment> payment = paymentRepository.findById(id);
        payment.ifPresent(u -> {
            if (u.getCustomer().getId().equals(customer.getId())){
                paymentRepository.deleteById(u.getId());
            }
        });
    }
    
    @Transactional
    public void deletePayments(List<Long> ids, Customer customer) {
        ids.forEach(id -> {
            Optional<Payment> payment = paymentRepository.findById(id);
            payment.ifPresent(u -> {
                if (u.getCustomer().getId().equals(customer.getId())){
                    paymentRepository.deleteById(u.getId());
                }
            });
        });
    }
    
    @Transactional
    public void updatePayment(Long id, PaymentCreateUpdateDTO paymentCreateUpdateDTO) {
        Payment paymentToUpdate = getById(id);
        Customer customer = paymentToUpdate.getCustomer();
       
        paymentToUpdate.setDateTime(LocalDateTime.parse(paymentCreateUpdateDTO.getDateTime(), dateTimeFormatter));
        paymentToUpdate.setDirection(paymentCreateUpdateDTO.getDirection());
        paymentToUpdate.setStatus(paymentCreateUpdateDTO.getStatus());
        paymentToUpdate.setAmount(paymentCreateUpdateDTO.getAmount());
        paymentToUpdate.setCurrencyName(accountService.getAccountByNameAndCustomer(paymentCreateUpdateDTO.getAccountName(), customer)
            .getCurrencyName());
        paymentToUpdate.setDescription(paymentCreateUpdateDTO.getDescription());
        paymentToUpdate.setPaymentCategory(paymentCategoryService.getByNameAndCustomer(paymentCreateUpdateDTO.getPaymentCategoryName(), customer));
        paymentToUpdate.setAccount(accountService.getAccountByNameAndCustomer(paymentCreateUpdateDTO.getAccountName(), customer));
 
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
    public Double getTotalSumByCurrency(CurrencyName currencyName, Customer customer){
        Double totalSum = 0.0;
        List<Payment> payments = getAllPaymentsByCurrencyName(currencyName, customer);
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
    public Double getOnScreenSumByCurrency(CurrencyName currencyName, Customer customer){
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
    public Double getDailySumByCurrency(CurrencyName currencyName, Customer customer){
        Double dailySum = 0.0;
        CustomerPeriodDTO customerPeriodDTO = CustomerPeriodDTO.of(LocalDate.now().format(dateFormatter),
                                                                   LocalDate.now().format(dateFormatter));
        List<Payment> payments = getAllPaymentsByPeriod(customerPeriodDTO, customer);
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

        Double totalSumUAH = getTotalSumByCurrency(CurrencyName.UAH, customer);
        Double totalSumEUR = getTotalSumByCurrency(CurrencyName.EUR, customer);
        Double totalSumUSD = getTotalSumByCurrency(CurrencyName.USD, customer);
        Double onScreenSumUAH = getOnScreenSumByCurrency(CurrencyName.UAH, customer);
        Double onScreenSumEUR = getOnScreenSumByCurrency(CurrencyName.EUR, customer);
        Double onScreenSumUSD = getOnScreenSumByCurrency(CurrencyName.USD, customer);
        Double dailySumUAH = getDailySumByCurrency(CurrencyName.UAH, customer);
        Double dailySumEUR = getDailySumByCurrency(CurrencyName.EUR, customer);
        Double dailySumUSD = getDailySumByCurrency(CurrencyName.USD, customer);

        statistic.put("totalSumUAH", totalSumUAH);
        statistic.put("totalSumEUR", totalSumEUR);
        statistic.put("totalSumUSD", totalSumUSD);
        statistic.put("onScreenSumUAH", onScreenSumUAH);
        statistic.put("onScreenSumEUR", onScreenSumEUR);
        statistic.put("onScreenSumUSD", onScreenSumUSD);
        statistic.put("dailySumUAH", dailySumUAH);
        statistic.put("dailySumEUR", dailySumEUR);
        statistic.put("dailySumUSD", dailySumUSD);
        return statistic;
    }

}
