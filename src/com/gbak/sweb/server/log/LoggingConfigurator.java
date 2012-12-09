package com.gbak.sweb.server.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.gbak.sweb.server.StokerWebProperties;
import com.gbak.sweb.server.security.LoginProperties;


public class LoggingConfigurator
{

    private static final Logger logger = Logger.getLogger(LoginProperties.class.getName());
    
    public void configure()
    {
        StokerWebProperties.getInstance();
        
        InputStream is = getClass().getClassLoader().getResourceAsStream("log4j.properties");
        if ( is != null )
        {
            //System.out.println("is is not null");
            Properties p = new Properties();
            try
            {
                p.load(is);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            PropertyConfigurator pc = new PropertyConfigurator();
            PropertyConfigurator.configure( p );
        }
    }
}
