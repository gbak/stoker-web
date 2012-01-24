package sweb.client.gauge;

import sweb.shared.model.StokerProbe;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DigitDisplayBinder extends Composite implements HasText, InstantTempDisplay
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
    
    public DigitDisplayBinder()
    {
       
        initWidget(uiBinder.createAndBindUi(this));
        displayTemp.setText("0");
        displayTemp.addStyleName("sweb-displayTemp");
     //   displayTemp.setStyleName("sweb-DisplayTemp");
        tempDecorator.addStyleName("sweb-tempDecorator");
    }



    public void setText(String text)
    {
        displayTemp.setText(text);
    }

    public String getText()
    {
        return displayTemp.getText();
    }



    @Override
    public void init(String name, Object o)
    {
        
    }



    @Override
    public void setAlarmRange(StokerProbe stokerProbe)
    {
        // TODO Auto-generated method stub
        
    }



    @Override
    public void draw()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTemp(float f)
    {
        String s = NumberFormat.getFormat("###").format(f);
        displayTemp.setText(s );
        
    }

    @Override
    public void setTemp(int i)
    {
        String s = NumberFormat.getFormat("###").format(i);
        displayTemp.setText(s );
        
    }

}
