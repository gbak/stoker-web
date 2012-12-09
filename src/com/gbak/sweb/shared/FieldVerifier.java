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

package com.gbak.sweb.shared;

public class FieldVerifier
{


    public static Integer getValidTemp( String str)
    {
        Integer i = null;
        try
        {
           i = new Integer( str );
           if ( ! isTempInRange( i ))
               i = null;
        }
        catch ( NumberFormatException nfe)
        {
            i = null;
        }

        return i;
    }

    public static Float getValidFloatTemp( String str)
    {
        Float f = null;
        try
        {
           f = new Float( str );
           if ( ! isTempInRange( f ))
               f = null;
        }
        catch ( NumberFormatException nfe)
        {
            f = null;
        }

        return f;
    }


    public static boolean isTempInRange( Integer i)
    {
        if ( i.intValue() > 0 && i.intValue() < 800 )
        {
            return true;
        }
        return false;
    }

    public static boolean isTempInRange( Float i)
    {
        if ( i.intValue() > 0 && i.intValue() < 800 )
        {
            return true;
        }
        return false;
    }
}
