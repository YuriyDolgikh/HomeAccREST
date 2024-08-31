package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    
    public static CustomerDTO of(Customer customer){
        return new CustomerDTO(customer.getId(), customer.getEmail(), customer.getFirstName(), customer.getLastName());
    }
}
