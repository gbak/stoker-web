package sweb.server.controller.events;

import java.util.EventObject;

public class ConfigStateEvent extends EventObject
{

    private static final long serialVersionUID = -5262244265495339323L;
    public enum EventType { NONE, CONFIG_LOADED, CONFIG_NOT_LOADED }
    private EventType m_EventType = EventType.NONE;


    public ConfigStateEvent(Object source, EventType et)
    {
        super(source);
        m_EventType = et;
    }

    public EventType getEventType()
    {
        return m_EventType; 
    }
    
}
