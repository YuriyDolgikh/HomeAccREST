package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.enums.AccountType;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long id;
    private String name;
    private String description;
    private AccountType type;
    private CurrencyName currencyName;


    public static AccountDTO of(Long id, String name, String description, AccountType accountType, CurrencyName currencyName){
        return new AccountDTO(id, name, description, accountType, currencyName);
    }

    public static AccountDTO of(Account account) {
        return new AccountDTO(account.getId(), account.getName(), account.getDescription(), account.getType(), account.getCurrencyName());
    }
}
