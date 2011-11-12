package sweb.server;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;
import sweb.server.controller.Controller;

public class ClientMessagePusher
{

    private volatile static ClientMessagePusher m_ClientMessagePusher = null;
    private ConcurrentMap<String,CometSession> webSessions = new ConcurrentHashMap<String, CometSession>();
    
    public static ClientMessagePusher getInstance()
    {
        if ( m_ClientMessagePusher == null)
        {
            synchronized ( Controller.class)
            {
                if ( m_ClientMessagePusher == null )
                {
                    m_ClientMessagePusher = new ClientMessagePusher();
                }
            }
        }
        return m_ClientMessagePusher;
    }
    
    public void addSession(HttpSession httpSession)
    {
        CometSession cometSession = CometServlet.getCometSession( httpSession );
        
        if ( webSessions.putIfAbsent(httpSession.getId(), cometSession) != null )
        {
           //httpSession.invalidate();
           System.out.println("User already on");
           webSessions.remove(httpSession.getId());
           webSessions.put(httpSession.getId(), cometSession);
        }

    }
    
    private void enqueueCometMessage( Serializable message)
    {
        for ( Map.Entry<String, CometSession> entry: webSessions.entrySet())
        {
            if ( entry.getValue().isValid() )
            {
               entry.getValue().enqueue( message );
            }
            else
            {
                System.out.println("Removing invalid comet session");
                entry.getValue().invalidate();
                webSessions.remove(entry.getKey());
            }

        }
    }
    
    public void push(Serializable message )
    {
        enqueueCometMessage(message);
    }
    
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
