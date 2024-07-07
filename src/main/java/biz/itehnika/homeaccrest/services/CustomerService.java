package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.config.AppConfig;
import biz.itehnika.homeaccrest.dto.CustomerRegistrationDTO;
import biz.itehnika.homeaccrest.dto.CustomerUpdateDTO;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.enums.CustomerRole;
import biz.itehnika.homeaccrest.repos.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerService{
    private final CustomerRepository customerRepository;
    private final PaymentCategoryService paymentCategoryService;
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id){
        return customerRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id).orElseThrow();
    }
    
    @Transactional(readOnly = true)
    public Customer findByLogin(String login) {
        return customerRepository.findCustomerByLogin(login);
    }
    
    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {
        return customerRepository.findCustomerByEmail(email);
    }
    
    @Transactional
    public void createNewCustomer(CustomerRegistrationDTO customerRegistrationDTO) {
        Customer customer = new Customer();
        customer.setLogin(customerRegistrationDTO.getLogin());
        customer.setEmail(customerRegistrationDTO.getEmail());
        customer.setPassword(passwordEncoder.encode(customerRegistrationDTO.getPassword()));
        customer.setRole(CustomerRole.USER);
        customer.setFilters(true, true, true, true, true, true, true);
        customerRepository.save(customer);
        paymentCategoryService.initPaymentCategoriesForCustomer(customer);
        
    }
    
    
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    @Transactional
    public void deleteCustomer(Long idToDelete) {
            Optional<Customer> customer = customerRepository.findById(idToDelete);
            customer.ifPresent(u -> {
                if ( ! AppConfig.ADMIN_LOGIN.equals(u.getLogin())) {
                    customerRepository.deleteById(u.getId());
                }
            });
    }
    
    @Transactional
    public void deleteCustomers(List<Long> toDeleteList) {
        toDeleteList.forEach(toDelete -> {
            Optional<Customer> customer = customerRepository.findById(toDelete);
            customer.ifPresent(u -> {
                if ( ! AppConfig.ADMIN_LOGIN.equals(u.getLogin())) {
                    customerRepository.deleteById(u.getId());
                }
            });
        });
    }

    @Transactional
    public void updateCustomer(Long id, CustomerUpdateDTO customerUpdateDTO) {
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow();
        customerToUpdate.setLogin(customerUpdateDTO.getLogin());
        customerToUpdate.setEmail(customerUpdateDTO.getEmail());
        customerRepository.save(customerToUpdate);
    }

    @Transactional(readOnly = true)
    public Map<String, LocalDate> getWorkPeriod(Long customerId){
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        Map<String, LocalDate> period = new HashMap<>();
        LocalDate startDate = customer.getStartDate();
        LocalDate endDate = customer.getEndDate();
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        period.put("startDate", startDate);
        period.put("endDate", endDate);
        return period;
    }

    @Transactional
    public void setWorkPeriod(Long customerId, LocalDate startDate, LocalDate endDate){
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        customer.setStartDate(startDate);
        customer.setEndDate(endDate);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getFilters(Long customerId){
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        return customer.getFilters();
    }

    @Transactional
    public void setFilter(Long customerId, Map<String, Boolean> filters){
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        customer.setFilters(filters.get("isUAH"),
                            filters.get("isEUR"),
                            filters.get("isUSD"),
                            filters.get("isIN"),
                            filters.get("isOUT"),
                            filters.get("isCompleted"),
                            filters.get("isScheduled"));
        customerRepository.save(customer);
    }

    public Map<String, Boolean> translateFiltersToMap(List<String> filtersList){
        Map<String, Boolean> filters = new HashMap<>();

        filters.put("isUAH", filtersList.contains("UAH"));
        filters.put("isEUR", filtersList.contains("EUR"));
        filters.put("isUSD", filtersList.contains("USD"));
        filters.put("isIN", filtersList.contains("IN"));
        filters.put("isOUT", filtersList.contains("OUT"));
        filters.put("isCompleted", filtersList.contains("Completed"));
        filters.put("isScheduled", filtersList.contains("Scheduled"));

        return filters;
    }

    public static String getCustomerDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        ZoneId zoneId = ZoneId.of(System.getProperty("user.timezone"));
        return sdf.format(new Date(System.currentTimeMillis()));
    }

}
