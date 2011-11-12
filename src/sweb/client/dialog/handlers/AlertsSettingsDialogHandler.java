package sweb.client.dialog.handlers;

import java.util.ArrayList;

import sweb.shared.model.alerts.AlertModel;

public interface AlertsSettingsDialogHandler
{
   public void onReturn(  ArrayList<AlertModel> alertBaseList );
}
