package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.dto.AccountCreateUpdateDTO;
import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.Payment;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.repos.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

//    @Transactional
//    public Map<Long, Double> getAccountBalancesByCustomer(Customer customer){
//        Map<Long, Double> balances = new HashMap<>();
//        List<Account> accounts = getAccountsByCustomer(customer);
//        for(Account account : accounts){
//            Double sum = 0.0;
//            for (Payment payment : account.getPayments()){
//                if(payment.getDirection()){
//                    sum += payment.getAmount();
//                }else{
//                    sum -= payment.getAmount();
//                }
//            }
//            balances.put(account.getId(), sum);
//        }
//        return balances;
//    }
//
    
    @Transactional
    public void updateAccountBalanceInDB(Account account){
        Double balance = 0.0;
        for (Payment payment : account.getPayments()){
            if(payment.getDirection()){
                balance += payment.getAmount();
            }else{
                balance -= payment.getAmount();
            }
        }
        account.setBalance(balance);
        accountRepository.save(account);
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
    public void addAccount(AccountCreateUpdateDTO accountCreateUpdateDTO, Customer customer){
        Account account = new Account(accountCreateUpdateDTO, customer);
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
    public void updateAccount(Long id, AccountCreateUpdateDTO accountCreateUpdateDTO) {
        Account accountToUpdate = getById(id);

        accountToUpdate.setName(accountCreateUpdateDTO.getName());
        accountToUpdate.setDescription(accountCreateUpdateDTO.getDescription());
        accountToUpdate.setType(accountCreateUpdateDTO.getType());
        accountToUpdate.setCurrencyName(accountCreateUpdateDTO.getCurrencyName());
        accountToUpdate.setBalance(accountCreateUpdateDTO.getBalance());
        accountRepository.save(accountToUpdate);
    }



}
