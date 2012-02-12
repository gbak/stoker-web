package sweb.client.gauge;

import sweb.shared.model.stoker.StokerProbe;
import sweb.shared.model.stoker.StokerProbe.AlarmType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DigitDisplayBinder extends InstantTempDisplay implements HasText
{

    private static DigitDisplayBinderUiBinder uiBinder = GWT
            .create(DigitDisplayBinderUiBinder.class);

    interface DigitDisplayBinderUiBinder extends
            UiBinder<Widget, DigitDisplayBinder>
    {
    }
    
    @UiField
    Label displayTemp;

    @UiField
    DecoratorPanel tempDecorator;
    
    @UiField
    Label lowTemp;
    
    @UiField
    Label highTemp;
    
    @UiField
    HorizontalPanel digitHPanelHighLow;
    
    public DigitDisplayBinder()
    {
       
        initWidget(uiBinder.createAndBindUi(this));
        displayTemp.setText("0");
        displayTemp.addStyleName("sweb-displayTemp");
     //   displayTemp.setStyleName("sweb-DisplayTemp");
        tempDecorator.addStyleName("sweb-tempDecorator");
        
        lowTemp.addStyleName("sweb-digitTempLowLabel");
        highTemp.addStyleName("sweb-digitTempHighLabel");
        digitHPanelHighLow.setWidth("100%");
    }



    public void setText(String text)
    {
        displayTemp.setText(text);
    }

    public String getText()
    {
        return displayTemp.getText();
    }


    public void init(StokerProbe sp)
    {
        localProbe = sp;
    }

    public void setAlarmRange(StokerProbe stokerProbe)
    {
        AlarmType at = stokerProbe.getAlarmEnabled();
        float currentTemp = stokerProbe.getCurrentTemp();
        localProbe = stokerProbe;
        
        if ( at == AlarmType.ALARM_FIRE )
        {
           lowTemp.setText(NumberFormat.getFormat("###").format(stokerProbe.getLowerTempAlarm()));
           highTemp.setText(NumberFormat.getFormat("###").format(stokerProbe.getUpperTempAlarm()));
           
        }
        else if ( at == AlarmType.ALARM_FOOD )
        {
            lowTemp.setVisible(false);
            highTemp.setText(NumberFormat.getFormat("###").format(stokerProbe.getTargetTemp()));
        }
        else if ( at == AlarmType.NONE )
        {
            lowTemp.setVisible(false);
            highTemp.setVisible(false);
        }
    }



    public void draw()
    {
        // TODO Auto-generated method stub
        
    }

    public void checkAlarms( int i )
    {
        super.checkAlarms(i);
        
        if ( change == true )
        {
            if ( tempAlert == TempAlert.HIGH)
            {
                lowTemp.removeStyleName("flash");
                highTemp.addStyleName("flash");
                displayTemp.addStyleName("red");
            }
            else if ( tempAlert == TempAlert.LOW )
            {
                highTemp.removeStyleName("flash");
                lowTemp.addStyleName("flash");
                displayTemp.addStyleName("blue");
            }
            else if ( tempAlert == TempAlert.NONE )
            {
                highTemp.removeStyleName("flash");
                lowTemp.removeStyleName("flash");
                displayTemp.removeStyleName("blue");
            }
            change = false;
        }
    }
    
    public void setTemp(float f)
    {
        String s = NumberFormat.getFormat("###").format(f);
        displayTemp.setText(s );
        checkAlarms((int)f);   
    }

    public void setTemp(int i)
    {
        String s = NumberFormat.getFormat("###").format(i);
        displayTemp.setText(s );
        checkAlarms(i);
    }

}
