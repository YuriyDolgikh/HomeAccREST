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
    
    public static final String ADMIN_EMAIL = "admin@example.com";
    
    @Value("${admin.password}")
    private String passAdm;

    @Bean
    public CommandLineRunner lineRunner(){
        return new CommandLineRunner() {
            @Override
            public void run(String... args){
                // TODO - Code before starting application
                if (customerService.findByEmail(ADMIN_EMAIL) == null){
                    customerService.createNewCustomer(new CustomerRegistrationDTO(ADMIN_EMAIL,passAdm,passAdm, "adminFirstName" , "adminLastName"));
                    Customer customer = customerService.findByEmail(ADMIN_EMAIL);
                    customerService.setAdmin(customer);
                }
            }
        };
    }
    
   
}
