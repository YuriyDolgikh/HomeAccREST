package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.config.AppConfig;
import biz.itehnika.homeaccrest.dto.CustomerFiltersDTO;
import biz.itehnika.homeaccrest.dto.CustomerPeriodDTO;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerService{
    private final CustomerRepository customerRepository;
    private PaymentCategoryService paymentCategoryService;
    private PasswordEncoder passwordEncoder;
    
    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    
    @Autowired
    public void setPaymentCategoryService(PaymentCategoryService paymentCategoryService) {
        this.paymentCategoryService = paymentCategoryService;
    }
    
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
    public Customer findByEmail(String email) {
        return customerRepository.findCustomerByEmail(email);
    }
    
    @Transactional
    public void createNewCustomer(CustomerRegistrationDTO customerRegistrationDTO) {
        Customer customer = new Customer();
        customer.setEmail(customerRegistrationDTO.getEmail());
        customer.setPassword(passwordEncoder.encode(customerRegistrationDTO.getPassword()));
        customer.setRole(CustomerRole.USER);
        customer.setFirstName(customerRegistrationDTO.getFirstName());
        customer.setLastName(customerRegistrationDTO.getLastName());
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
                if ( ! AppConfig.ADMIN_EMAIL.equals(u.getFirstName())) {
                    customerRepository.deleteById(u.getId());
                }
            });
    }
    
    @Transactional
    public void deleteCustomers(List<Long> toDeleteList) {
        toDeleteList.forEach(toDelete -> {
            Optional<Customer> customer = customerRepository.findById(toDelete);
            customer.ifPresent(u -> {
                if ( ! AppConfig.ADMIN_EMAIL.equals(u.getFirstName())) {
                    customerRepository.deleteById(u.getId());
                }
            });
        });
    }

    @Transactional
    public void updateCustomer(Long id, CustomerUpdateDTO customerUpdateDTO) {
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow();
        customerToUpdate.setEmail(customerUpdateDTO.getEmail());
        customerToUpdate.setFirstName(customerUpdateDTO.getFirstName());
        customerToUpdate.setLastName(customerUpdateDTO.getLastName());
        
        customerRepository.save(customerToUpdate);
    }

    @Transactional(readOnly = true)
    public CustomerPeriodDTO getActivePeriod(Customer customer){
        LocalDate startDate = customer.getStartDate();
        LocalDate endDate = customer.getEndDate();
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return CustomerPeriodDTO.of(startDate.format(dateFormatter), endDate.format(dateFormatter));
    }

    @Transactional
    public void setActivePeriod(CustomerPeriodDTO customerPeriodDTO, Customer customer){

        customer.setStartDate(LocalDate.parse(customerPeriodDTO.getStartDate(), dateFormatter));
        customer.setEndDate(LocalDate.parse(customerPeriodDTO.getEndDate(), dateFormatter));
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerFiltersDTO getFilters(Customer customer){
        return CustomerFiltersDTO.of(customer.getIsUAH(),
                                     customer.getIsEUR(),
                                     customer.getIsUSD(),
                                     customer.getIsIN(),
                                     customer.getIsOUT(),
                                     customer.getIsCompleted(),
                                     customer.getIsScheduled()
                                    );
    }

    @Transactional
    public void setFilter(CustomerFiltersDTO customerFiltersDTO, Customer customer){
        customer.setFilters(customerFiltersDTO.getIsUAH(),
                            customerFiltersDTO.getIsEUR(),
                            customerFiltersDTO.getIsUSD(),
                            customerFiltersDTO.getIsIN(),
                            customerFiltersDTO.getIsOUT(),
                            customerFiltersDTO.getIsCompleted(),
                            customerFiltersDTO.getIsScheduled());
        customerRepository.save(customer);
    }
    
    @Transactional
    public void setAdmin(Customer customer){
        customer.setRole(CustomerRole.ADMIN);
        customerRepository.save(customer);
    }

}
