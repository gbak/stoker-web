package sweb.server;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

public interface ClientMessenger
{
    public void addSession(HttpSession httpSession);
   
    public void removeSession( HttpSession httpSession );
  
    public void push(Serializable message );
 
    public void sessionPush( HttpSession httpSession, Serializable message );
  
}
