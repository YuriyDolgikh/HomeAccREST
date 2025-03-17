package biz.itehnika.homeaccrest.models;

import biz.itehnika.homeaccrest.dto.CustomerRegistrationDTO;
import biz.itehnika.homeaccrest.models.enums.CustomerRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private CustomerRole role;


    private String phone;
    private String address;

    // Work period
    private LocalDate startDate;
    private LocalDate endDate;

    // Filters for payments list
    private Boolean isUAH;
    private Boolean isEUR;
    private Boolean isUSD;
    private Boolean isIN;
    private Boolean isOUT;
    private Boolean isCompleted;
    private Boolean isScheduled;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private List<PaymentCategory> paymentCategories;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private List<Account> accounts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private List<Payment> payments;
    
    public Customer(String email, String password, String firstName, String lastName, CustomerRole role, String phone, String address) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.phone = phone;
        this.address = address;
    }

    
    public static Customer fromDTO(CustomerRegistrationDTO customerRegistrationDTO){
        return new Customer(customerRegistrationDTO.getEmail(),
                            customerRegistrationDTO.getPassword(),
                            customerRegistrationDTO.getFirstName(),
                            customerRegistrationDTO.getLastName(),
                            CustomerRole.USER,
                            null,
                            null);
    }

//    public Map<String, Boolean> getFilters(){
//        Map<String, Boolean> filters = new HashMap<>();
//        filters.put("isUAH", isUAH);
//        filters.put("isEUR", isEUR);
//        filters.put("isUSD", isUSD);
//        filters.put("isIN", isIN);
//        filters.put("isOUT", isOUT);
//        filters.put("isCompleted", isCompleted);
//        filters.put("isScheduled", isScheduled);
//        return filters;
//    }

    public void setFilters(Boolean isUAH,
                           Boolean isEUR,
                           Boolean isUSD,
                           Boolean isIN,
                           Boolean isOUT,
                           Boolean isCompleted,
                           Boolean isScheduled){
        this.isUAH = isUAH;
        this.isEUR = isEUR;
        this.isUSD = isUSD;
        this.isIN = isIN;
        this.isOUT = isOUT;
        this.isCompleted = isCompleted;
        this.isScheduled = isScheduled;
    }

}
