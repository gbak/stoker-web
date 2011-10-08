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

package sweb.server.controller;

import java.io.IOException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class StokerJSON
{

    public StokerJSON()
    {

    }

    public void getStokerData()
    {
        HttpClient httpclient = new DefaultHttpClient();
        try {

            HttpGet httpget = new HttpGet("http://192.168.15.220/stoker.json");

            System.out.println("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            System.out.println("before execute");
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("after execute");
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");

            JSONObject json = (JSONObject) JSONSerializer.toJSON( responseBody );
            JSONObject stoker = json.getJSONObject("stoker");
            JSONObject sensors = json.getJSONObject("sensors");
            JSONObject blowers = json.getJSONObject("blowers");

            String sensorID = sensors.getString("id");
            System.out.println("SensorID: " + sensorID );



        }
        catch (ClientProtocolException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static void main(String[] args)
    {
        StokerJSON js = new StokerJSON();
        js.getStokerData();

    }
}
