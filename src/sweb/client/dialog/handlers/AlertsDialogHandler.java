package sweb.client.dialog.handlers;

import java.util.ArrayList;

import sweb.shared.model.alerts.Alert;

public interface AlertsDialogHandler
{
   public void onReturn(  ArrayList<Alert> alertBaseList );
}
