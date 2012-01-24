package sweb.client.gauge;

import com.google.gwt.user.client.ui.IsRenderable;

import sweb.shared.model.StokerProbe;


public interface InstantTempDisplay extends IsRenderable
{

    public void init(String name, Object o);
    
    public void setAlarmRange(StokerProbe stokerProbe);
    
    public void draw();
    
    public void setTemp( float f );
    
    public void setTemp( int i );
    
}
