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
                    new Server().url("https://acc-rest.itehnika.biz"),
                    new Server().url("https://home-acc-rest-dff398f45658.herokuapp.com"),
                    new Server().url("http://localhost:8080")
                )
            )
            .info(
                new Info().title("Home Accounting API").description("You need to send Bearer InvalidToken from Frontend " +
                                                                    "in the header of the request. In the request, " +
                                                                    "put Bearer InvalidToken as a key-value pair, where " +
                                                                    "\"Authorization\" will be the key and the \"Bearer\" " +
                                                                    "Keyword followed by Bearer InvalidToken after a space as value.")
            );
    }
    
}
