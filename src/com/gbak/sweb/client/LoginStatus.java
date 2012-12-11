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

package com.gbak.sweb.client;

public class LoginStatus
{

    private static boolean bLoggedIn;
    private volatile static LoginStatus m_LoginStatus = null;

    public static LoginStatus getInstance()
    {
        if ( m_LoginStatus == null)
            m_LoginStatus = new LoginStatus();
        return m_LoginStatus;
    }
    private LoginStatus()
    {
        bLoggedIn = false;
    }
    public void setLoginStatus( boolean b)
    {
        bLoggedIn = b;
    }
    public boolean getLoginStatus()
    {
        return bLoggedIn;
    }
}
