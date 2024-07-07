package biz.itehnika.homeaccrest.repos;

import biz.itehnika.homeaccrest.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findCustomerByLogin(String login);
    Customer findCustomerByEmail(String email);

    boolean existsCustomerByLogin(String login);
    boolean existsCustomerByEmail(String email);
}
