package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.enums.AccountType;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateUpdateDTO {

    private String name;
    private String description;
    private AccountType type;
    private CurrencyName currencyName;


    public static AccountCreateUpdateDTO of(String name, String description, AccountType accountType, CurrencyName currencyName){
        return new AccountCreateUpdateDTO(name, description, accountType, currencyName);
    }

    public static AccountCreateUpdateDTO of(Account account) {
        return new AccountCreateUpdateDTO(account.getName(), account.getDescription(), account.getType(), account.getCurrencyName());
    }
}
