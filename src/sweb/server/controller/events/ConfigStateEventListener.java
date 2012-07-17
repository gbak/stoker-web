package sweb.server.controller.events;

import java.util.EventListener;

public interface ConfigStateEventListener extends EventListener
{
    public void actionPerformed(ConfigStateEvent cse);
}
