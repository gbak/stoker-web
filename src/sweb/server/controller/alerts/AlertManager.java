package sweb.server.controller.alerts;

import java.util.ArrayList;
import java.util.Set;

import sweb.shared.model.alerts.AlertModel;



public interface AlertManager
{

    /*
     * Set configuration options sent by Client. All configuration comes in as
     * the base AlertModel class.
     */
    public void setConfiguration(ArrayList<AlertModel> alertBaseList);

    public ArrayList<AlertModel> getConfiguration();

    public Set<String> getAvailableDeliveryMethods();

}
