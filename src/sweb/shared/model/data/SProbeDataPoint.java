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

package sweb.shared.model.data;

import java.io.Serializable;
import java.util.Date;

public class SProbeDataPoint extends SDataPoint implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -644393265758209355L;

    private float fTempC;
    private float fTempF;


    public SProbeDataPoint()
    {
        super();
        fTempC = 0;
        fTempF = 0;

    }

    public SProbeDataPoint(  String d, Date date, float F, float C)
    {
        super( d, date );
        fTempC = C;
        fTempF = F;
    }

    public boolean compare( SDataPoint sdp )
    {
        if ( sdp instanceof SProbeDataPoint )
        {
            SProbeDataPoint pdp = (SProbeDataPoint) sdp;
            if ( fTempF == pdp.fTempF )
                return true;
        }
        return false;
    }

    public boolean compare( SProbeDataPoint pdp )
    {
        if ( fTempF == pdp.fTempF )
            return true;

        return false;
    }

    public float getTempF()
    {
       return fTempF;
    }
    public float getTempC()
    {
        return fTempC;
    }

    public void update( SDataPoint sdp )
    {
       super.update(sdp);
       if ( sdp instanceof SProbeDataPoint )
       {
          SProbeDataPoint pdp = (SProbeDataPoint) sdp;
          fTempC = pdp.fTempC;
          fTempF = pdp.fTempF;
       }
    }
    
    @Override
    public float getData()
    {
        return fTempF;
    }

    

}
