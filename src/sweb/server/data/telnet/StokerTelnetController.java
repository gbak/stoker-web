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

package sweb.server.data.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import sweb.server.StokerWebConstants;
import sweb.server.StokerWebProperties;
import sweb.server.config.StokerWebConfiguration;
import sweb.server.data.DataController;
import sweb.server.events.StateChangeEvent;
import sweb.server.events.StateChangeEvent.EventType;
import sweb.server.parser.stoker.InvalidDataPointException;
import sweb.server.parser.stoker.SDataPointHelper;
import sweb.shared.model.data.SDataPoint;

/*  This class does the following:
 *     Creates connection to Stoker
 *     Sends the stop command
 *     Sends start command to enable temp output
 *     Captures incomming temp messages and sends them to the data store
 */

public class StokerTelnetController implements DataController
{
  //  private volatile static StokerTelnetController stc = null;

    enum StokerResponseState { NONE, TEMPS };
    enum StokerCmdState  { ENTRY, CMD_IN_PROGRESS, STARTED, STOPPED, UNKNOWN };
    enum TelnetState { ENTRY, DISCONNECTED, CONNECTED, ERROR };
    enum LoginState { YES, NO };

    volatile StokerResponseState m_StokerResponseState = StokerResponseState.NONE;
    volatile StokerCmdState m_StokerState = StokerCmdState.ENTRY;
    volatile TelnetState    m_TelnetState = TelnetState.DISCONNECTED;
    volatile LoginState     m_LoginState  = LoginState.NO;

    TelnetClient m_Telnet = null;
    OutputStream m_StreamToStoker = null;
    InputStream  m_StreamFromStoker = null;

    Date m_LastMessageTime = null; // Last time we received a valid data point from the Stoker
    Thread m_ReaderThread = null;  // thread that always runs to read the telnet input
    Timer startTimer = new Timer();
    Timer telnetMonitorTimer = new Timer();

    AtomicBoolean abStartHelper = new AtomicBoolean(false);
    
    private EventBus eventBus;
    private StokerWebConfiguration swConfiguration;

    private static final Logger logger = Logger.getLogger(StokerTelnetController.class.getName());
    
    @Inject
    public StokerTelnetController(EventBus eventBus,
                                  StokerWebConfiguration config)
    {
        this.eventBus = eventBus;
        this.swConfiguration = config;
        
        m_LoginState = LoginState.NO;
        m_TelnetState = TelnetState.ENTRY;

    }
    
    public boolean waitForReady(long lWaitTimeMills )
    {
        final AtomicBoolean go = new AtomicBoolean( );
        final Timer waitTimer = new Timer();

        go.set(false);

        TimerTask waitTT = new TimerTask() {
            @Override
            public void run()
            {
                go.set(true);
            }

        };
        waitTimer.schedule(waitTT,lWaitTimeMills);

        while ( ! go.get() )
        {
            if ( isReady() )
            {
                waitTimer.cancel();  // cancel here just in case it fires, our return state would be wrong
                break;
            }
            sleep( 100 );
        }

        waitTimer.cancel();

        return go.get();
    }

    public boolean isReady()
    {
        // Stoker needs to be in the Started state (started by this controller ) in
        // order to get temps out.  So, now we just check to see if we are getting
        // temps back.

        //return ( stokerState == StokerCmdState.STARTED && telnetState == TelnetState.CONNECTED );
        return ( isProcessing() );
    }

    public boolean isProcessing()
    {
       return ( m_StokerResponseState  == StokerResponseState.TEMPS &&
                m_TelnetState == TelnetState.CONNECTED );
    }

   

    private void createConnection() throws IOException
    {

        logger.info("Creating Telnet connection.");

        m_Telnet = new TelnetClient();
        String strStokerIP = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKER_IP_ADDRESS);
        String strStokerPort = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKER_PORT);
        int iStokerPort =  new Integer( strStokerPort ).intValue();
        m_Telnet.connect( strStokerIP, iStokerPort ); 
                          

        m_TelnetState = TelnetState.CONNECTED;

        if ( m_ReaderThread != null)
        {
            logger.warn("Interrupting thread");

            m_ReaderThread.interrupt();
        }

        m_ReaderThread = new Thread() {
            @Override
            public void run()
            {
                streamReader();
            }
        };
        m_ReaderThread.start();


        m_StreamToStoker = m_Telnet.getOutputStream();

        int x = 0;
        while (m_LoginState == LoginState.NO )
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if (x++ > 60)
            {
                if ( m_LoginState == LoginState.NO )
                {
                    logger.warn("Unable to connect or login, giving up");
                    stopInternal();
                    break;
                }
            }
        }

    }

    public boolean reconnect()
    {

            stopInternal();
            start();

        return true;
    }

    private void sendSequence(String sendString) throws IOException
    {
        m_StokerState = StokerCmdState.CMD_IN_PROGRESS;
        m_StreamToStoker.write(sendString.getBytes());
        m_StreamToStoker.flush();
        logger.debug("Sent: [" + new String(sendString.getBytes()) + "]");
    }

    private void sendLoginSequence() throws IOException
    {
        sendSequence(StokerTelnetCommands.StokerCmdLoginID);

    }

    private void sendPasswordSequence() throws IOException
    {
        sendSequence(StokerTelnetCommands.StokerCmdLoginPasswd);

    }

    private void waitForCompletion( StokerCmdState s )
    {
        int x = 0;
        while (m_StokerState != s && x++ < 15)
        {
            logger.debug("Command in process, waiting...");
            sleep(2000);
        }

        System.out.println("Out of waitForCompletion loop, x: " + x );
        if (x > 15)
        {
            logger.info("Command did not return in 30 seconds, reconnecting...");
            reconnect();
        }

    }

    private void sendTempsCommand()
    {
        logger.debug("sendTempsCommand()");
        try
        {
            sendSequence(StokerTelnetCommands.StokerCmdTemps);
        }
        catch (IOException e)
        {
            // Error writing to telnet port
            reconnect();
        }

    }
    private void sendStartCommand()
    {
        logger.debug("sendStartCommand()");
        try
        {
            sendSequence(StokerTelnetCommands.StokerCmdStart);
            waitForCompletion(StokerCmdState.STARTED);

        }
        catch (IOException e)
        {
            // Error writing to telnet port
            reconnect();
        }

    }

    private void sendStopCommand()
    {
        logger.info("sendStopCommand()");
        try
        {
            sendSequence(StokerTelnetCommands.StokerCmdStop);
            waitForCompletion(StokerCmdState.STOPPED);
        }
        catch (IOException e)
        {
            // Error writing to telnet port
            logger.info("Exception caught running stop command, attempting reconnect.");
            reconnect();
        }

    }

    private void closeConnection() throws IOException
    {
        eventBus.post(new StateChangeEvent( this, EventType.LOST_CONNECTION ));

        m_TelnetState = TelnetState.DISCONNECTED;
        m_LoginState = LoginState.NO;
        m_StokerState = StokerCmdState.UNKNOWN;
        if ( m_ReaderThread != null)
           m_ReaderThread.interrupt();
        if ( m_Telnet.isConnected())  // this is to get around a null Pointer exception in the disconnect if the stoker is lost
           m_Telnet.disconnect();
    }

    private void sleep(long l)
    {
        try
        {
            Thread.sleep( l);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }
    private void streamReader()
    {
        m_StreamFromStoker = m_Telnet.getInputStream();

        try
        {
           // BufferedReader reader = new BufferedReader(new InputStreamReader(
           //         streamFromStoker));
            StringBuilder sb = new StringBuilder();
            String line = null;
            int intRead;
            int lastRead = 0;
            int doubleLastRead = 0;
            byte[] buff = new byte[1024];
            int ret_read = 0;

            while ( m_TelnetState == TelnetState.CONNECTED )
            {
                if ( m_StreamFromStoker.available() <= 0 )
                {
                    sleep(100);
                    continue;
                }

                // telnet.sendAYT(2000);

                ret_read = m_StreamFromStoker.read(buff);

                for ( int  i = 0; i < ret_read; i++ )
                {
                    intRead = buff[i];
                    if (intRead >= 32 && intRead < 127)
                    {
                        char c = (char) intRead;
                        sb.append(c);
                    }

                    if (intRead == ' ' && lastRead == ':'
                            && sb.toString().contains(StokerWebConstants.STOKER_PROMPT_LOGIN))  // login:
                    {
                        logger.info("found login string");
                        sendLoginSequence();
                        sb = new StringBuilder();
                    }

                    if (intRead == ' ' && lastRead == ':'
                            && sb.toString().contains(StokerWebConstants.STOKER_PROMPT_PASSWORD))  // password
                    {
                        logger.info("found password string");
                        sendPasswordSequence();
                        sb = new StringBuilder();
                    }

                    if (intRead == ' ' && lastRead == '>' && doubleLastRead == 47
                            && sb.toString().contains("tini") && sb.toString().contains(" />"))
                    {
                        logger.info("found tini prompt");
                        m_LoginState = LoginState.YES;

                    }

                    if ( intRead == 't' && lastRead == 'r'
                            && sb.toString().contains(StokerWebConstants.STOKER_CONDITION_START))  // stoker: start
                    {
                        logger.info("Stoker Start response detected");
                        m_StokerState = StokerCmdState.STARTED;

                    }

                    if ( intRead == 'p' && lastRead == 'o'
                            && sb.toString().contains(StokerWebConstants.STOKER_CONDITION_STOP))  // stkcmd: stop
                    {
                        logger.info("Stoker stopped response detected");
                        m_StokerState = StokerCmdState.STOPPED;

                    }


                    if ( intRead == 'd' && lastRead == 'e' )
                          //  && sb.toString().contains("stkcmd: not started"))
                    {
                        logger.debug("String: " + sb.toString());
                        logger.info("Stoker not started response detected");
                        m_StokerState = StokerCmdState.STOPPED;

                    }


                    if (intRead == 10 ) // && m_StokerState == StokerCmdState.STARTED)
                    {
                        if (sb.length() > 0)
                        {
                            sb.append('\n');

                            try
                            {
                                // Not sure if I want this here.
                               logger.trace("t");
                                addDataPoint( sb.toString() );

                              // System.out.print("p");

                               m_StokerResponseState = StokerResponseState.TEMPS;
                               m_LastMessageTime = Calendar.getInstance().getTime();
                            }
                           catch ( InvalidDataPointException idp)
                            {
                                logger.warn("Invalid Data Point: [" + sb.toString() + "]");
                            }

                                sb = new StringBuilder();
                            }
                        }

                    doubleLastRead = lastRead;
                    lastRead = intRead;

                }  // end for read

            }  // end while CONNECTED

        }
        catch (Exception e)
        {
            logger.error("Exception while reading socket: " + e.getMessage());

            StackTraceElement [] sta = e.getStackTrace();
            
            for ( int i = 0; i < sta.length; i++ )
            {
                logger.error(sta[i]);
            }
            reconnect();
        }
        logger.error("Reader exiting");

    }

    protected void addDataPoint( String s ) throws InvalidDataPointException
    {
        ArrayList<SDataPoint> arDP = SDataPointHelper.createDataPoint( s, swConfiguration );
        
        for ( SDataPoint dp: arDP )
        {
           eventBus.post(dp);
        }
    }

    

    private void waitForTemps()
    {
        logger.debug("StokerTelnetController::waitForTemps()");
       // TODO: replace this code with a better solution
        boolean leave = false;
        int x = 0;
        while ( !leave )
        {
            if ( x++ > 20 )
                break;


            if ( m_StokerResponseState == StokerResponseState.TEMPS)
            {
                leave = true;

                logger.info("waitForTemps(): Found TEMPS");
                break;
            }
            sleep(1000);
        }
        logger.debug("StokerTelnetController::waitForTemps() done");
    }

    /*
     * (non-Javadoc)
     * @see sweb.server.stoker.controller.data.DataController#stop()
     *
     * Public facing stop() need to cancel the start timer so it will
     * not kick off again.
     */
    public void stop()
    {
       startTimer.cancel();
       stopInternal();
    }

    /*
     * Internal stop shouldn't cancel the startTimer since we call this
     * method during the reconnect process.
     */
    private void stopInternal()
    {
        logger.debug("stopInternal()");
        try
        {
        //   sendStopCommand();  // when disconnecting, it is probably best to leave the stoker running.
           closeConnection();
        }
        catch ( IOException ioe )
        {
            ioe.printStackTrace();
        }
    }

    /*
     * Threaded class to monitor the stoker output and restart
     * the telnet interface if the output should go stop.
     */
    private void startMonitor()
    {
/*
        final Timer t = new Timer();
        //final Date monitorDate = null;

        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run()
            {
                System.out.println("StokerTelnetController:startMontor() TimerTask:run() ");
                Calendar calCurrent = Calendar.getInstance();
                Calendar calLastMessage = Calendar.getInstance();
                calLastMessage.setTime(m_LastMessageTime);
                calLastMessage.add( Calendar.MINUTE, 1);
                if ( calLastMessage.before( calCurrent ) )
                {
                    // no messages from Stoker in 2 minutes
                    // try to restart telnet interface
                    t.cancel();
                    System.out.println("No activity from stoker in the last one minutes, attempting reconnect");
                    reconnect();
                }

            }

        }, 60000, 10000);
      */

        TelnetMontitorTimerTask _runTimer = new TelnetMontitorTimerTask();
        telnetMonitorTimer.schedule( _runTimer, 10000 );
    }

    private class TelnetMontitorTimerTask extends TimerTask
    {
       public void run()
       {
          Calendar calCurrent = Calendar.getInstance();
          Calendar calLastMessage = Calendar.getInstance();
          calLastMessage.setTime(m_LastMessageTime);
          calLastMessage.add( Calendar.MINUTE, 1);

          if ( calLastMessage.before( calCurrent ) )
          {
              // no messages from Stoker in 2 minutes
              // try to restart telnet interface

              logger.warn("No activity from stoker in the last minute, attempting reconnect");
              reconnect();
              return;
          }

          calLastMessage.add(Calendar.SECOND, 5);

          telnetMonitorTimer.schedule( new TelnetMontitorTimerTask(), calLastMessage.getTime() );
       }
    }

    public void startHelper()
    {
        if (! abStartHelper.get() )
        {
            if ( abStartHelper.getAndSet(true) == false )
            {
                logger.debug("StokerTelnet .start()");

                m_StokerState = StokerCmdState.ENTRY;
                m_StokerResponseState = StokerResponseState.NONE;

       //     while (telnetState == TelnetState.CONNECTED && stokerState != State.STARTED )
        //    {
                try
                {
                    createConnection();

                    if ( m_TelnetState != TelnetState.CONNECTED )
                    {
                       return;
                    }
                    waitForTemps();
                    if ( m_StokerResponseState != StokerResponseState.TEMPS )
                        sendTempsCommand();

                    waitForTemps();
                    if ( m_StokerResponseState != StokerResponseState.TEMPS )
                    {

                       sendStopCommand();
                       sendStartCommand();
                    }
                    eventBus.post(new StateChangeEvent( this, EventType.CONNECTION_ESTABLISHED ));

                }
                catch (ConnectException ce)
                {
                    m_TelnetState = TelnetState.DISCONNECTED;
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }

                if ( m_TelnetState != TelnetState.CONNECTED &&
                        m_StokerResponseState != StokerResponseState.TEMPS )
                {
                    // Something is not right.  tear down the connection and leave
                    logger.error("StokerTelnetController:start()");
                    logger.error("  Status not correct after starting, calling stop()");
                    stopInternal();

                }
                else
                {
                 // Things seem to be in good order, so we will start
                    // the keep alive monitor
                    startMonitor();

                }


            }
            abStartHelper.set(false);
        }

    }
    public void start()
    {
        startTimer = new Timer();
        TimerTask tt = new TimerTask() {

            @Override
            public void run()
            {
                logger.debug("StokerTelnetController:start() run()");
                if ( m_TelnetState != TelnetState.CONNECTED )
                {
                    logger.debug("StokerTelnetController:start() run() - calling startHelper()");
                   startHelper();
                   return;
                }

            }

        };

        startTimer.scheduleAtFixedRate(tt,0, 180000);  // TODO: make this configurable

    }


   /* public static void main(String[] args)
    {
        StokerTelnetController sw = new StokerTelnetController();

        // sw.createConnection();
        // sw.sendStopCommand();
        // sw.sendStartCommand();
        sw.start();
        int x = 0;
        while ( true )
        {
            
            while ( sw.isMessageAvailable() )
            {
               System.out.println("Next message is: " + sw.getNextMessage());
            }
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            

        }
      //  sw.sendStopCommand();
      ///  sw.closeConnection();

       // sw.stop();

    }

*/

}
