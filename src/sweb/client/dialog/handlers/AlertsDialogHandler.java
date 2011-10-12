package sweb.client.dialog.handlers;

import java.util.ArrayList;

import sweb.shared.model.alerts.AlertBase;

public interface AlertsDialogHandler
{
   public void onReturn(  ArrayList<AlertBase> alertBaseList );
}
