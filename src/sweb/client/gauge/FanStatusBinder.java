package sweb.client.gauge;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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


    @UiField Button fanStatusButton;
    @UiField Label  time;

    public void setText(String text )
    {
       time.setText(text);
    }

    public void fanOn(long updatedTime)
    {
        fanStatusButton.addStyleName("sweb-fanDisplayButtonOn");
        fanStatusButton.setText("ON");
        if ( fanTimer == null )
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
    }
    
    public void fanOff(long updatedTime)
    {
        fanStatusButton.removeStyleName("sweb-fanDisplayButtonOn");
        fanStatusButton.setText("OFF");
        if ( fanTimer != null )
        {
            fanTimer.cancel();
            fanTimer = null;
        }
        else
        {
            //System.out.println("fanTimer is null!!");  // This may or may not be an error, 
        }

        lFanCounter = (long)Math.floor(updatedTime/1000);
      //  System.out.println("Client: updateTime: " + updatedTime );
        time.setText(formatMinutes(lFanCounter).toString());

        
    }

    private String formatMinutes( long t )
    {
       int hh = (int) t / 3600;
       t = t % 3600;
       int mm = (int) t / 60; //get minutes
       int ss = (int) t % 60; //get Seconds
       
       int phh = Math.abs(hh);
       int pmm = Math.abs(mm); //convert to positive int
       int pss = Math.abs(ss); //convert to positive int
       
       String shh = getDoubleDigit2(phh);
       String smm = getDoubleDigit2(pmm); //convert it to double digit string '1' = '01', '2' = '02'
       String sss = getDoubleDigit2(pss);
        
       return new String(shh + ":" + smm + ":" + sss);
        
    }
    
    private String getDoubleDigit2( int t )
    {
       return NumberFormat.getFormat("00").format( t );
    }
}
