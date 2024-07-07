package biz.itehnika.homeaccrest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI api(){
        return new OpenAPI()
            .servers(
                List.of(
                    new Server().url("http://localhost:8080"),
                    new Server().url("https://home-acc-rest-dff398f45658.herokuapp.com"),
                    new Server().url("https://acc-rest.itehnika.biz")
                )
            )
            .info(
                new Info().title("Home Accounting API")
            );
    }
    
}
