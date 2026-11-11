package customer.procureflow.custom.handlers;

import org.springframework.stereotype.Component;

import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;

@Component 
@ServiceName("OrderService")
public class OrderHandler implements EventHandler {

    @On(event = CqnService.EVENT_CREATE, entity = "OrderService.Orders")
    public void createOrder(){
        
    }
}
