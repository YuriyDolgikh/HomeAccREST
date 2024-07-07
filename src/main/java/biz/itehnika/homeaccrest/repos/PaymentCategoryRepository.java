package biz.itehnika.homeaccrest.repos;

import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.PaymentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentCategoryRepository extends JpaRepository<PaymentCategory, Long> {

    PaymentCategory findPaymentCategoryByNameAndCustomer(String name, Customer customer);

    boolean existsPaymentCategoryByNameAndCustomer(String name, Customer customer);

    List<PaymentCategory> findPaymentCategoriesByCustomer(Customer customer);
}
