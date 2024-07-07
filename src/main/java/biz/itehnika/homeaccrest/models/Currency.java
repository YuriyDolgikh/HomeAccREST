package biz.itehnika.homeaccrest.models;

import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CurrencyName name;
    @Enumerated(EnumType.STRING)
    private CurrencyName nameBase;
    private Double buyRate;
    private Double saleRate;
    private LocalDate dateRate;

    public Currency(CurrencyName name, CurrencyName nameBase, Double buyRate, Double saleRate, LocalDate dateRate) {
        this.name = name;
        this.nameBase = nameBase;
        this.buyRate = buyRate;
        this.saleRate = saleRate;
        this.dateRate = dateRate;
    }

}
