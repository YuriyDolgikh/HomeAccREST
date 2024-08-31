package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.dto.CurrencyDTO;
import biz.itehnika.homeaccrest.models.Currency;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import biz.itehnika.homeaccrest.repos.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class CurrencyService {


    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public Boolean isRatesExistByDate(LocalDate localDate){
        return currencyRepository.existsByDateRate(localDate);
    }

    @Transactional
    public void addTodayRatesIntoDB(){  //TODO - make this every time when customer do firstName
        LocalDate localDate = LocalDate.now();
        List<Currency> listTodayRates = currencyRepository.findCurrenciesByDateRate(localDate);
        if (!listTodayRates.isEmpty()){
            return;
        }
        Currency currency;
        for (CurrencyDTO currencyDTO : getTodayRatesFromBank()){
            currency = CurrencyDTO.fromDTO(currencyDTO);
            currencyRepository.save(currency);
        }
    }

    @Transactional
    public Currency getCurrencyByNameAndDate(CurrencyName currencyName, LocalDate localDate){
        return currencyRepository.findCurrencyByNameAndDateRate(currencyName, localDate);
    }

    @Transactional
    public Currency getCurrencyByNameToday(CurrencyName currencyName){
        return currencyRepository.findCurrencyByNameAndDateRate(currencyName, LocalDate.now());
    }


    public static List<CurrencyDTO> getTodayRatesFromBank(){
        final RestTemplate restTemplate = new RestTemplate();
        CurrencyDTO[] ratesArray = restTemplate.getForObject("https://api.privatbank.ua/p24api/pubinfo?exchange&json&coursid=11", CurrencyDTO[].class);
        assert ratesArray != null;  // check API PrivatBank is OK
        return Arrays.asList(ratesArray);
    }
}
