/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
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

package com.gbak.sweb.server.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.gbak.sweb.server.report.JasperReportConstants.ReportConstants;


public class ReportDataSource
{
    private HashMap<String,Object> reportData = new HashMap<String,Object>();
    
    public void addReportValue( ReportConstants rc, Object value )
    {
        reportData.put(rc.toString(), value);
    }
    
    public Collection<Map<String,?>> getReportData()
    {
        Collection<Map<String,?>> array1 = new ArrayList<Map<String,?>>();
        array1.add( reportData );
        return array1;
    }
}
