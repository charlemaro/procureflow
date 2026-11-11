package customer.procureflow.custom.utils;

import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;

public class ResponseUtils {

    private ResponseUtils(){
    }

    public  static ServiceException returnBadRequest(String message){
        return new ServiceException(ErrorStatuses.BAD_REQUEST, message);
    }
}
