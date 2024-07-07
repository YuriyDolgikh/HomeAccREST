package biz.itehnika.homeaccrest.repos;

import biz.itehnika.homeaccrest.models.Currency;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CurrencyRepository extends JpaRepository<Currency, Long>  {

    Currency findCurrencyByNameAndDateRate(CurrencyName currencyName, LocalDate dateRate);
    List<Currency> findCurrenciesByDateRate(LocalDate dateRate);

    Boolean existsByDateRate(LocalDate localDate);
}
