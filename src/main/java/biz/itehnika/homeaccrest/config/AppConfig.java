package biz.itehnika.homeaccrest.config;

import biz.itehnika.homeaccrest.services.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppConfig {
    public final CurrencyService currencyService;
    
    public static final String ADMIN_LOGIN = "admin";

    @Bean
    public CommandLineRunner lineRunner(){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                // TODO - Code before starting application

                currencyService.addTodayRatesIntoDB();  // TODO - Set rule to actualise exchange rates ( e.g.: every customer login || every one hour)
            }
        };
    }
}
