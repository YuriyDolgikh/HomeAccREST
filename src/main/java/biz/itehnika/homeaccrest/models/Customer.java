package biz.itehnika.homeaccrest.models;

import biz.itehnika.homeaccrest.dto.CustomerRegistrationDTO;
import biz.itehnika.homeaccrest.models.enums.CustomerRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private String login;
    private String password;
    private String email;
    
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
    private List<PaymentCategory> paymentCategories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    public Customer(String login, String password, CustomerRole role,
                    String email, String phone, String address) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    public static Customer fromDTO(CustomerRegistrationDTO customerRegistrationDTO){
        return new Customer(customerRegistrationDTO.getLogin(),
                            customerRegistrationDTO.getPassword(),
                            CustomerRole.USER,
                            customerRegistrationDTO.getEmail(),
                      null,
                     null);
    }

    public Map<String, Boolean> getFilters(){
        Map<String, Boolean> filters = new HashMap<>();
        filters.put("isUAH", isUAH);
        filters.put("isEUR", isEUR);
        filters.put("isUSD", isUSD);
        filters.put("isIN", isIN);
        filters.put("isOUT", isOUT);
        filters.put("isCompleted", isCompleted);
        filters.put("isScheduled", isScheduled);
        return filters;
    }

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
