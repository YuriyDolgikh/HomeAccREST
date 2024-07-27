package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.config.AppConfig;
import biz.itehnika.homeaccrest.dto.PaymentCategoryCreateUpdateDTO;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.PaymentCategory;
import biz.itehnika.homeaccrest.repos.CustomerRepository;
import biz.itehnika.homeaccrest.repos.PaymentCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCategoryService {

    private final PaymentCategoryRepository paymentCategoryRepository;
    private final CustomerRepository customerRepository;
    
    @Transactional
    public PaymentCategory getByNameAndCustomer(String name, Customer customer) {
        return paymentCategoryRepository.findPaymentCategoryByNameAndCustomer(name, customer);
    }

    @Transactional(readOnly = true)
    public List<PaymentCategory> getPaymentCategoriesByCustomer(Customer customer){
        return paymentCategoryRepository.findPaymentCategoriesByCustomer(customer);
    }

    @Transactional
    public boolean existsById(Long id){
        return paymentCategoryRepository.existsById(id);
    }
    
    @Transactional
    public PaymentCategory getById(Long id){
        return paymentCategoryRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void addPaymentCategory(PaymentCategoryCreateUpdateDTO categoryCreateUpdateDTO, Customer customer){
        PaymentCategory paymentCategory = new PaymentCategory(categoryCreateUpdateDTO.getName(),
                                                              categoryCreateUpdateDTO.getDescription(),
                                                              customer);
        paymentCategoryRepository.save(paymentCategory);
    }
    
    @Transactional
    public void addPaymentCategory(String name, String description, Customer customer){
        PaymentCategory paymentCategory = new PaymentCategory(name, description, customer);
        paymentCategoryRepository.save(paymentCategory);
    }

    @Transactional
    public void initPaymentCategoriesForCustomer(Customer customer){
        Customer customerAdmin = customerRepository.findCustomerByLogin(AppConfig.ADMIN_LOGIN);

        List<PaymentCategory> paymentCategories = getPaymentCategoriesByCustomer(customerAdmin);
        for (PaymentCategory category : paymentCategories){
            addPaymentCategory(PaymentCategoryCreateUpdateDTO.of(category.getName(), category.getDescription()), customer);
        }
    }
    
    @Transactional
    public void deletePaymentCategory(Long id, Customer customer) {
            Optional<PaymentCategory> paymentCategory = paymentCategoryRepository.findById(id);
            paymentCategory.ifPresent(u -> {
                if (u.getCustomer().getId().equals(customer.getId())){
                    paymentCategoryRepository.deleteById(u.getId());
                }
            });
    }

    @Transactional
    public void deletePaymentCategories(List<Long> ids, Customer customer) {
        ids.forEach(id -> {
            Optional<PaymentCategory> paymentCategory = paymentCategoryRepository.findById(id);
            paymentCategory.ifPresent(u -> {
                if (u.getCustomer().getId().equals(customer.getId())){
                    paymentCategoryRepository.deleteById(u.getId());
                }
            });
        });
    }

    @Transactional
    public void updatePaymentCategory(Long id, PaymentCategoryCreateUpdateDTO categoryCreateUpdateDTO) {
        PaymentCategory categoryToUpdate = getById(id);

        categoryToUpdate.setName(categoryCreateUpdateDTO.getName());
        categoryToUpdate.setDescription(categoryCreateUpdateDTO.getDescription());
        paymentCategoryRepository.save(categoryToUpdate);
    }
    
    @Transactional
    public void initForAdmin(){
        Customer customerAdmin = customerRepository.findCustomerByLogin(AppConfig.ADMIN_LOGIN);
        addPaymentCategory("DEFAULT", "Default payment category", customerAdmin);
        addPaymentCategory("SALARY", "Income earned from work", customerAdmin);
        addPaymentCategory("HEALTH", "Medicines, clinics, food additives ...", customerAdmin);
        addPaymentCategory("BANK", "Banking operations, payment for banking services", customerAdmin);
        addPaymentCategory("BEAUTY", "Beauty salons, cosmetics...", customerAdmin);
        addPaymentCategory("CAR", "Spare parts, fuel, repairs", customerAdmin);
        addPaymentCategory("CHILDREN", "Schools, kindergartens, entertainment, toys", customerAdmin);
        addPaymentCategory("GIFT", "Something given or received as a gift", customerAdmin);
        addPaymentCategory("RESTAURANT", "Restaurants, cafes, bars...", customerAdmin);
        addPaymentCategory("ENTERTAINMENT", "Clubs, discos, parties", customerAdmin);
        addPaymentCategory("TRAVEL", "Hotels, tours...", customerAdmin);
        addPaymentCategory("COMMUNAL PAYMENTS", "Rent and utility costs", customerAdmin);
        addPaymentCategory("SERVICES", "Services received and rendered", customerAdmin);
        addPaymentCategory("TICKETS", "Plane, train, bus, ship", customerAdmin);
        addPaymentCategory("FOOD", "Supermarkets, farmers markets, bakeries", customerAdmin);
        addPaymentCategory("EQUIPMENTS", "Specialized tools and equipment", customerAdmin);
        addPaymentCategory("TRANSPORT", "Taxi and public transport costs", customerAdmin);
        addPaymentCategory("HOUSEHOLD", "Various household appliances, dishes", customerAdmin);
        addPaymentCategory("HOBBY", "Everything for body and soul", customerAdmin);
        addPaymentCategory("EXCHANGE", "Exchange currency (don't delete!)", customerAdmin);
        addPaymentCategory("TRANSFER", "Send money to my another account (don't delete!)", customerAdmin);
        addPaymentCategory("OTHER", "Other income and expenses", customerAdmin);
    }

}
