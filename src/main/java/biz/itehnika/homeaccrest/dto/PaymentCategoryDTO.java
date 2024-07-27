package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.PaymentCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCategoryDTO {

    private Long id;
    private String name;
    private String description;

    public static PaymentCategoryDTO of(Long id, String name, String description){
        return new PaymentCategoryDTO(id, name, description);
    }

    public static List<PaymentCategoryDTO> listOf(List<PaymentCategory> paymentCategories) {
        List<PaymentCategoryDTO> paymentCategoryDTOList = new ArrayList<>();
        for(PaymentCategory category : paymentCategories){
            paymentCategoryDTOList.add(of(category.getId(), category.getName(), category.getDescription()));
        }
        return paymentCategoryDTOList;
    }
}
