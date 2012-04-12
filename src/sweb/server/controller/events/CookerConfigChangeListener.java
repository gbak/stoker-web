package sweb.server.controller.events;

import java.util.EventListener;

public interface CookerConfigChangeListener extends EventListener
{
    public void actionPerformed();
}

