package customer.procureflow.custom.handlers;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sap.cds.CdsData;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;

import customer.procureflow.custom.utils.ResponseUtils; 

@Component
@ServiceName("OrderService")
public  class ProductHandler implements EventHandler {
    
    @Before(event = CqnService.EVENT_CREATE, entity = "OrderService.Products")
    public  void validateProduct(CdsCreateEventContext context, List<CdsData> products){
         for(CdsData  product :products){
            String productName = (String) product.get("name");
            BigDecimal  price = (BigDecimal) product.get("price"); 

            if(Objects.isNull(productName) || productName.isBlank() ){
                throw ResponseUtils.returnBadRequest( "Product Name canot be empty"); 
            }

            if(Objects.isNull(price)){
                throw ResponseUtils.returnBadRequest("Product Name canot be empty"); 
            }

            int result = price.compareTo(BigDecimal.ZERO);
            if(result == 0){
                throw  ResponseUtils.returnBadRequest("Price cannot be Zero");
            }else if(result < 0){
                throw ResponseUtils.returnBadRequest( "Price cannot  be Less than zero");
            }
         }  
    }

}