package biz.itehnika.homeaccrest.repos;

import biz.itehnika.homeaccrest.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findCustomerByEmail(String email);

    boolean existsCustomerByEmail(String email);
}
