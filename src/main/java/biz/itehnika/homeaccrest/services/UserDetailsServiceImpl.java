package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.repos.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final CustomerRepository customerRepository;
    
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Customer customer = customerRepository.findCustomerByLogin(login);
        if (customer == null)
            throw new UsernameNotFoundException(login + " not found");

        List<GrantedAuthority> roles = List.of(
                new SimpleGrantedAuthority(customer.getRole().toString())   // Customer have only one Role
        );

        return new User(customer.getLogin(), customer.getPassword(), roles);
    }
}