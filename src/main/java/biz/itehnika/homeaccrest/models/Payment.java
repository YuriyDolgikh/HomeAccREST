package biz.itehnika.homeaccrest.models;

import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment implements Comparator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime;
    private Boolean direction;          // True - income, False - waste
    private Boolean status;             // True - Completed, False - Scheduled
    private Double amount;
    @Enumerated(EnumType.STRING)
    private CurrencyName currencyName;
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paymentCategory_id")
    private PaymentCategory paymentCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Payment(LocalDateTime dateTime,
                   Boolean direction,
                   Boolean status,
                   Double amount,
                   CurrencyName currencyName,
                   String description,
                   PaymentCategory paymentCategory,
                   Account account,
                   Customer customer) {
        this.dateTime = dateTime;
        this.direction = direction;
        this.status = status;
        this.amount = amount;
        this.currencyName = currencyName;
        this.description = description;
        this.paymentCategory = paymentCategory;
        this.account = account;
        this.customer = customer;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        LocalDateTime dt1 = ((Payment) obj1).getDateTime();
        LocalDateTime dt2 = ((Payment) obj2).getDateTime();

        return dt1.compareTo(dt2);
    }


    public Long getCustomerId() {
        return customer.getId();
    }
}
