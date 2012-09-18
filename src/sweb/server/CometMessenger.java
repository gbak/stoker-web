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

package sweb.server;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class CometMessenger implements ClientMessenger
{

    private ConcurrentMap<String,CometSession> webSessions = new ConcurrentHashMap<String, CometSession>();
    
    private static final Logger logger = Logger.getLogger(CometMessenger.class.getName());
    
    @Inject
    private CometMessenger() { }
    
    @Override
    public void addSession(HttpSession httpSession)
    {
        CometSession cometSession = CometServlet.getCometSession( httpSession );
        
        if ( webSessions.putIfAbsent(httpSession.getId(), cometSession) != null )
        {
          // httpSession.invalidate();
           logger.info("User already on");
           webSessions.remove(httpSession.getId());
           webSessions.put(httpSession.getId(), cometSession);
        }

    }
    
    @Override
    public void removeSession( HttpSession httpSession )
    {
       CometSession cometSession = CometServlet.getCometSession(httpSession);
       try
       {
           cometSession.invalidate();
           webSessions.remove(httpSession.getId());
           httpSession.invalidate();
       }
       catch ( IllegalStateException  ise )
       {
           System.out.println("Error invalidating session; likely invalid already");
       }
    }
    
    private void enqueueCometMessage( Serializable message)
    {
        for ( Map.Entry<String, CometSession> entry: webSessions.entrySet())
        {
            if ( entry.getValue().isValid() )
            {
                logger.trace("sending message to browser ");
               entry.getValue().enqueue( message );
            }
            else
            {
                logger.warn("Removing invalid comet session");
             //   entry.getValue().invalidate();
                webSessions.remove(entry.getKey());
            }

        }
    }
    
    @Override
    public void push(Serializable message )
    {
        enqueueCometMessage(message);
    }
    
    @Override
    public void sessionPush( HttpSession httpSession, Serializable message )
    {
        CometSession cometSession = CometServlet.getCometSession( httpSession );
        if ( cometSession == null )
        {
            return;
        }
        cometSession.enqueue(message);
        
    }
   
}
