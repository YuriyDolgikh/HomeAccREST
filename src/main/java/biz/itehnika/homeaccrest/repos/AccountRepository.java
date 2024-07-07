package biz.itehnika.homeaccrest.repos;

import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAccountsByCustomer(Customer customer);
    List<Account> findAccountsByCurrencyNameAndCustomer(CurrencyName currencyName, Customer customer);

    boolean existsAccountByNameAndCustomer(String name, Customer customer);

    Account findByNameAndCustomer(String name, Customer customer);
}
