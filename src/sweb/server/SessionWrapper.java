package sweb.server;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class SessionWrapper implements HttpSession {
    public SessionWrapper (HttpSession delegate) {
        this.delegate = delegate;
    }
    
    public Object getAttribute (String arg0) {
        return delegate.getAttribute (arg0);
    }

    public Enumeration getAttributeNames () {
        return delegate.getAttributeNames ();
    }

    public long getCreationTime () {
        return delegate.getCreationTime ();
    }

    public String getId () {
        return delegate.getId ();
    }

    public long getLastAccessedTime () {
        return delegate.getLastAccessedTime ();
    }

    public int getMaxInactiveInterval () {
        return delegate.getMaxInactiveInterval ();
    }

    public ServletContext getServletContext () {
        return delegate.getServletContext ();
    }

    public HttpSessionContext getSessionContext () {
        return delegate.getSessionContext ();
    }

    public Object getValue (String arg0) {
        return delegate.getValue (arg0);
    }

    public String[] getValueNames () {
        return delegate.getValueNames ();
    }

    public void invalidate () {
        delegate.invalidate ();
    }

    public boolean isNew () {
        return delegate.isNew ();
    }

    public void putValue (String arg0, Object arg1) {
        delegate.putValue (arg0, arg1);
    }

    public void removeAttribute (String arg0) {
        delegate.removeAttribute (arg0);
    }

    public void removeValue (String arg0) {
        delegate.removeValue (arg0);
    }

    public void setAttribute (String arg0, Object arg1) {
        delegate.setAttribute (arg0, arg1);
    }

    public void setMaxInactiveInterval (int arg0) {
        delegate.setMaxInactiveInterval (arg0);
    }

    private HttpSession delegate;
}
