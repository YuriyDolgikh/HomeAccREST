package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.dto.AccountDTO;
import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.Payment;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.repos.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
 
    
    @Transactional
    public Account getById(Long id){
        return accountRepository.findById(id).orElseThrow();
    }
    
    @Transactional
    public boolean existsById(Long id){
        return accountRepository.existsById(id);
    }

    @Transactional
    public List<Account> getAccountsByCurrencyNameAndCustomer(CurrencyName currencyName, Customer customer){
        return accountRepository.findAccountsByCurrencyNameAndCustomer(currencyName, customer);
    }

    @Transactional
    public List<Account> getAccountsByCustomer(Customer customer){
        return accountRepository.findAccountsByCustomer(customer);
    }

    @Transactional
    public Account getAccountByNameAndCustomer(String name, Customer customer){
        return accountRepository.findByNameAndCustomer(name, customer);
    }

    @Transactional
    public Map<Long, Double> getAccountBalancesByCustomer(Customer customer){
        Map<Long, Double> balances = new HashMap<>();
        List<Account> accounts = getAccountsByCustomer(customer);
        for(Account account : accounts){
            Double sum = 0.0;
            for (Payment payment : account.getPayments()){
                if(payment.getDirection()){
                    sum += payment.getAmount();
                }else{
                    sum -= payment.getAmount();
                }
            }
            balances.put(account.getId(), sum);
        }
        return balances;
    }

    @Transactional
    public Double getTotalByCurrencyNameAndCustomer(CurrencyName currencyName, Customer customer){
        Double total = 0.0;
        List<Account> accounts = getAccountsByCurrencyNameAndCustomer(currencyName, customer);
        for(Account account : accounts){
            Double sum = 0.0;
            for (Payment payment : account.getPayments()){
                if(payment.getDirection()){
                    sum += payment.getAmount();
                }else{
                    sum -= payment.getAmount();
                }
            }
            total += sum;
        }
        return total;
    }

    @Transactional
    public void addAccount(AccountDTO accountDTO, Customer customer){
        Account account = new Account(accountDTO, customer);
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccounts(List<Long> ids, Customer customer) {
        ids.forEach(id -> {
            Optional<Account> account = accountRepository.findById(id);
            account.ifPresent(u -> {
                if (u.getCustomer().getId().equals(customer.getId())){
                    accountRepository.deleteById(u.getId());
                }
            });
        });
    }
    
    @Transactional
    public void deleteAccount(Long id, Customer customer){
        Optional<Account> account = accountRepository.findById(id);
        account.ifPresent(u -> {
            if (u.getCustomer().getId().equals(customer.getId())){
                accountRepository.deleteById(u.getId());
            }
        });
    }

    @Transactional
    public void updateAccount(Long id, AccountDTO accountDTO) {
        Account accountToUpdate = getById(id);

        accountToUpdate.setName(accountDTO.getName());
        accountToUpdate.setDescription(accountDTO.getDescription());
        accountToUpdate.setType(accountDTO.getType());
        accountToUpdate.setCurrencyName(accountDTO.getCurrencyName());
        accountRepository.save(accountToUpdate);
    }



}
