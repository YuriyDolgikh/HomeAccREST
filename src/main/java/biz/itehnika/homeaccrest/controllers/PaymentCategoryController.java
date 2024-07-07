package biz.itehnika.homeaccrest.controllers;

import biz.itehnika.homeaccrest.dto.PaymentCategoryDTO;
import biz.itehnika.homeaccrest.models.Customer;
import biz.itehnika.homeaccrest.models.PaymentCategory;
import biz.itehnika.homeaccrest.services.CustomerService;
import biz.itehnika.homeaccrest.services.PaymentCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PaymentCategoryController {

    private final CustomerService customerService;
    private final PaymentCategoryService paymentCategoryService;
    
    @GetMapping("/PaymentCategoriesList")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<List<PaymentCategoryDTO>> getPaymentCategories(Principal principal){
        Customer customer = customerService.findByLogin(principal.getName());
        List<PaymentCategoryDTO> paymentCategoryDTOList = PaymentCategoryDTO.listOf(paymentCategoryService.getPaymentCategoriesByCustomer(customer));
        return new ResponseEntity<>(paymentCategoryDTOList, HttpStatus.OK);
    }
    
    @GetMapping("/addNewCategory")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public String addNewCategory() {
        return "addNewCategory";
    }

    @PostMapping(value = "/addNewCategory")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public String addNewCategory(@RequestParam String name,
                                 @RequestParam String description,
                                 Model model) {

        User user = CustomerController.getCurrentUser();
        Customer customer = customerService.findByLogin(user.getUsername());

        if ( ! paymentCategoryService.addPaymentCategory(name, description, customer)) {
            model.addAttribute("exists", true);
            model.addAttribute("name", name);
            model.addAttribute("description", description);
            return "addNewCategory";
        }
        model.addAttribute("added", true);

        if (CustomerController.isAdmin(user)){
            return "redirect:/admin";
        }
        return "redirect:/settingsCategory";
    }

    @PostMapping(value = "/deleteCategory")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public String deleteCategory(@RequestParam(name = "toDelete", required = false) List<Long> ids, Model model) {
        if (ids != null && !ids.isEmpty()) {
            paymentCategoryService.deletePaymentCategories(ids);
        }
        User user = CustomerController.getCurrentUser();
        Customer customer = customerService.findByLogin(user.getUsername());
        model.addAttribute("categories", paymentCategoryService.getPaymentCategoriesByCustomer(customer));

        if (CustomerController.isAdmin(user)){
            return "redirect:/admin";
        }
        return "redirect:/settingsCategory";
    }

    @GetMapping("/updateCategory/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String updateCategory(@PathVariable(value = "id") Long id, Model model) {
        PaymentCategory paymentCategory = paymentCategoryService.getById(id);
        model.addAttribute("name", paymentCategory.getName());
        model.addAttribute("description", paymentCategory.getDescription());
        return "updateCategory";
    }

    @PostMapping(value = "/updateCategory")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public String updateCategory(@RequestParam() Long id,
                                 @RequestParam() String name,
                                 @RequestParam(required = false) String description,
                                 Model model) {
        User user = CustomerController.getCurrentUser();
        Customer customer = customerService.findByLogin(user.getUsername());
        PaymentCategory paymentCategory = paymentCategoryService.getById(id);

        if ( ! paymentCategoryService.updatePaymentCategory(paymentCategory.getId(), name, description, customer)) {
            model.addAttribute("exists", true);
            model.addAttribute("id", paymentCategory.getId());
            model.addAttribute("name", paymentCategory.getName());
            model.addAttribute("description", paymentCategory.getDescription());
        }
        if (CustomerController.isAdmin(user)){
            return "redirect:/admin";
        }
        return "redirect:/settingsCategory";
    }

    @GetMapping(value = "/initCategories")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String initCategories(Model model){
        Customer customer = customerService.findByLogin(CustomerController.getCurrentUser().getUsername());
        paymentCategoryService.initPaymentCategoriesForCustomer(customer);
        model.addAttribute("paymentCategories", paymentCategoryService.getPaymentCategoriesByCustomer(customer));
        model.addAttribute("updated", true);
        return "settingsCategory";
    }

}
