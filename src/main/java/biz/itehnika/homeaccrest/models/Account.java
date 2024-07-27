package biz.itehnika.homeaccrest.models;

import biz.itehnika.homeaccrest.dto.AccountCreateUpdateDTO;
import biz.itehnika.homeaccrest.models.enums.AccountType;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private AccountType type;
    @Enumerated(EnumType.STRING)
    private CurrencyName currencyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    public Account(String name, String description, AccountType type, CurrencyName currencyName, Customer customer) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.currencyName = currencyName;
        this.customer = customer;
    }

    public Account(AccountCreateUpdateDTO accountCreateUpdateDTO, Customer customer){
        this.name = accountCreateUpdateDTO.getName();
        this.description = accountCreateUpdateDTO.getDescription();
        this.type = accountCreateUpdateDTO.getType();
        this.currencyName = accountCreateUpdateDTO.getCurrencyName();
        this.customer = customer;
    }
}
