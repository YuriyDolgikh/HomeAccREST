package biz.itehnika.homeaccrest.config;

import biz.itehnika.homeaccrest.dto.CustomerRegistrationDTO;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppConfig {
    public final CustomerService customerService;
    
    public static final String ADMIN_LOGIN = "admin";
    @Value("${admin.password}")
    private String passAdm;

    @Bean
    public CommandLineRunner lineRunner(){
        return new CommandLineRunner() {
            @Override
            public void run(String... args){
                // TODO - Code before starting application
                if (customerService.findByLogin(ADMIN_LOGIN) == null){
                    customerService.createNewCustomer(new CustomerRegistrationDTO(ADMIN_LOGIN,passAdm,passAdm, "admin@example.com"));
                    Customer customer = customerService.findByLogin(ADMIN_LOGIN);
                    customerService.setAdmin(customer);
                }
            }
        };
    }
}
