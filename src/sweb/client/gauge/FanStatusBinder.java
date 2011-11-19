package sweb.client.gauge;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FanStatusBinder extends Composite
{

    private static FanStatusBinderUiBinder uiBinder = GWT
            .create(FanStatusBinderUiBinder.class);
    private long lFanCounter = new Long(0);
    private Timer fanTimer;

    interface FanStatusBinderUiBinder extends UiBinder<Widget, FanStatusBinder>
    {
    }

    public FanStatusBinder()
    {
        initWidget(uiBinder.createAndBindUi(this));
    }

  //  @UiField Button fanStatusButton;
  //  @UiField Label  time;
    @UiField Button fanStatusButton;
    @UiField Label  time;

    public void setText(String text )
    {
       time.setText(text);
    }

    public void fanOn(long updatedTime)
    {
        fanTimer = new Timer() {
            public void run() {
                if(!time.getText().equals("")) {
                    lFanCounter = lFanCounter + 1;
                time.setText(formatMinutes(lFanCounter).toString());
                }
            }
        };
        
        fanTimer.scheduleRepeating(1000);
    }
    
    public void fanOff(long updatedTime)
    {
        fanTimer.cancel();
    }

    private String formatMinutes( long t )
    {
       int mm = (int) t / 60; //get minutes
       int ss = (int) t % 60; //get Seconds
       
       int pmm = Math.abs(mm); //convert to positive int
       int pss = Math.abs(ss); //convert to positive int
       
       String smm = getDoubleDigit(pmm); //convert it to double digit string '1' = '01', '2' = '02'
       String sss = getDoubleDigit(pss);
        
       return new String(smm + ":" + sss);
        
    }
    
    protected static String getDoubleDigit(int i) {
       String newI = null;
       switch (i) {
       case 0:
               newI = "00";
               break;
       case 1:
               newI = "01";
               break;
       case 2:
               newI = "02";
               break;
       case 3:
               newI = "03";
               break;
       case 4:
               newI = "04";
               break;
       case 5:
               newI = "05";
               break;
       case 6:
               newI = "06";
               break;
       case 7:
               newI = "07";
               break;
       case 8:
               newI = "08";
               break;
       case 9:
               newI = "09";
               break;
       default:
               newI = Integer.toString(i);
       }
       return newI;
     }
    
}
