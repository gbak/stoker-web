/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.gbak.sweb.shared.model.data;

import java.io.Serializable;
import java.util.Date;

public class SBlowerDataPoint extends SDataPoint implements Serializable
{

    private static final long serialVersionUID = 5758453043237619452L;
    private boolean bBlowerOn;
    private long m_lTotalRuntime; 
    private Date m_blowerOnTime;

    public SBlowerDataPoint( )
    {
        super();
        bBlowerOn = false;

    }

    public SBlowerDataPoint( SBlowerDataPoint sdp )
    {
        super((SDataPoint) sdp);
        bBlowerOn = sdp.isFanOn();

    }
    /*private String getBlowerID(String s)
    {
        return StokerConfiguration.getInstance().getBlowerID(s);
    }*/

    public SBlowerDataPoint(  String d, Date date, boolean bBlowerOn )
    {
        //super.collectedTime = date;
        //super.deviceID = getBlowerID( d );
        super( d, date );
        this.bBlowerOn = bBlowerOn;
    }

    public void setBlowerState( boolean b)
    {
        bBlowerOn = b;
    }

    public boolean isBlower()
    {
        return true;
    }

    public boolean isFanOn()
    {
        return bBlowerOn;
    }

    public boolean compare( SBlowerDataPoint bdp )
    {
       if ( bBlowerOn == bdp.bBlowerOn )
           return true;
       return false;
    }

    @Override
    public boolean compare( SDataPoint sdp )
    {
        if ( sdp instanceof SBlowerDataPoint )
        {
            SBlowerDataPoint bdp = (SBlowerDataPoint)  sdp;
           if ( bBlowerOn == bdp.bBlowerOn )
               return true;
        }
       return false;
    }

    @Override
    public void update( SDataPoint sdp)
    {
        super.update( sdp );
        if ( sdp instanceof SBlowerDataPoint )
        {
            SBlowerDataPoint bdp = (SBlowerDataPoint) sdp;
            bBlowerOn = bdp.bBlowerOn;
           // m_lTotalRuntime = bdp.m_lTotalRuntime;
          //  m_blowerOnTime = bdp.m_blowerOnTime;
        }
    }
    
    public String getDebugString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getDebugString() );
        sb.append("Blower ON: " + bBlowerOn + "\n");
        sb.append("Total Runtime: " + m_lTotalRuntime + "\n");
        sb.append("Blower On TIme: " + m_blowerOnTime + "\n");
        return sb.toString();
    }
    public void setTotalRuntime( long l )
    {
       m_lTotalRuntime = l;    
    }
    
    public long getTotalRuntime()
    {
        return m_lTotalRuntime;
    }
    
    public void setBlowerOnTime( Date d )
    {
        m_blowerOnTime = d;
    }
    
    public Date getBlowerOnTime() 
    { 
        return m_blowerOnTime; 
    }
    
    @Override
    public float getData()
    {
        return (float) (bBlowerOn ? 1 : 0);
    }

}
