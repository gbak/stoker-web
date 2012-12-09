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

public class JasperReportConstants
{

   // public static final String LOG_NAME = "logName";
    
    public enum ReportConstants 
    {
        LOG_NAME { public String toString() { return "logName"; } },
    
        START_DATE { public String toString() { return "startDate"; } },
    
        END_DATE { public String toString() {   return "endDate"; } },
    
        COOK_DURATION { public String toString() { return "cookDuration"; } },
    
        FAN_CYCLES { public String toString() { return "fanCycles"; } },
    
        FAN_TIME { public String toString() { return "fanTime"; } },
        
        TABLE_DATA_SOURCE { public String toString() { return "TableDataSource"; } },
        
        CHART_DATA_SOURCE { public String toString() { return "ChartDataSource"; } }
        
   }
    
}
