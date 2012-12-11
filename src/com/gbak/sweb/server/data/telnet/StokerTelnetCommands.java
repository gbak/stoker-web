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

package com.gbak.sweb.server.data.telnet;

public class StokerTelnetCommands
{

    public static String StokerCmdStart = "bbq -t\n";
    public static String StokerCmdStop = "\nbbq -k\n";
    public static String StokerCmdTemps = "\nbbq -temps\n";
    public static String StokerCmdLoginID = "root\r\n";
    public static String StokerCmdLoginPasswd = "tini\r\n";
    public static String StokerCmdLogout = "logout\n";


}
