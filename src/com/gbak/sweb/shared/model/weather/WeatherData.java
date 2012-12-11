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

package com.gbak.sweb.shared.model.weather;

import java.io.Serializable;

public class WeatherData implements Serializable
{

    private static final long serialVersionUID = -5972105651639343155L;

   private String m_City;
   private String m_State;
   private String m_CurrentTemperature;
   private String m_Humidity;
   private String m_WindSpeed;
   private String m_Text;
   private String m_CurrentImage;
   private String m_Code;
   private String m_ForecastHigh;
   private String m_ForecastLow;
   private String m_ForecastCondition;
   private String m_URL;
   private String m_Logo;

   public WeatherData( String strCity, String strState, String strCurrentTemp,
                       String strHumidity, String strWindSpeed, String strText,
                       String strCurrentImage, String strCode, String strForecastHigh,
                       String strForecastLow, String strForecastCondition )
   {
      setData( strCity,  strState,  strCurrentTemp,
                strHumidity,  strWindSpeed,  strText,
                strCurrentImage,  strCode,  strForecastHigh,
                strForecastLow,  strForecastCondition );
   }

   public WeatherData( )
   {
       setData("","","","","","","","","","","");
   }

   public void setData( String strCity, String strState, String strCurrentTemp,
                       String strHumidity, String strWindSpeed, String strText,
                       String strCurrentImage, String strCode, String strForecastHigh,
                       String strForecastLow, String strForecastCondition)

   {
       m_City = strCity;
       m_State = strState;
       m_CurrentTemperature = strCurrentTemp;
       m_Humidity = strHumidity;
       m_WindSpeed = strWindSpeed;
       m_Text = strText;
       m_CurrentImage = strCurrentImage;
       m_Code = strCode;
       m_ForecastHigh = strForecastHigh;
       m_ForecastLow = strForecastLow;
       m_ForecastCondition = strForecastCondition;
   }

   public void setData( WeatherData wd )
   {
       setData( wd.m_City, wd.m_State, wd.m_CurrentTemperature, wd.m_Humidity,
                wd.m_WindSpeed, wd.m_Text, wd.m_CurrentImage, wd.m_Code,
                wd.m_ForecastHigh, wd.m_ForecastLow, wd.m_ForecastCondition );
   }

   public String getCity()
   {
      return m_City;
   }

   public void setCity(String m_City)
   {
      this.m_City = m_City;
   }

   public String getState()
   {
      return m_State;
   }

   public void setCState(String m_State)
   {
      this.m_State = m_State;
   }

   public String getCurrentTemperature()
   {
      return m_CurrentTemperature;
   }

   public void setCurrentTemperature(String m_CurrentTemperature)
   {
      this.m_CurrentTemperature = m_CurrentTemperature;
   }

   public String getHumidity()
   {
      return m_Humidity;
   }

   public void setHumidity(String m_Humidity)
   {
      this.m_Humidity = m_Humidity;
   }

   public String getWindSpeed()
   {
      return m_WindSpeed;
   }

   public void setWindSpeed(String m_WindSpeed)
   {
      this.m_WindSpeed = m_WindSpeed;
   }

   public String getText()
   {
      return m_Text;
   }

   public void setText(String m_Text)
   {
      this.m_Text = m_Text;
   }

   public String getCurrentImage()
   {
      return m_CurrentImage;
   }

   public void setCurrentImage(String m_CurrentImage)
   {
      this.m_CurrentImage = m_CurrentImage;
   }

   public String getCode()
   {
      return m_Code;
   }

   public void setCode(String m_Code)
   {
      this.m_Code = m_Code;
   }

   public String getForecastHigh()
   {
      return m_ForecastHigh;
   }

   public void setForecastHigh(String m_ForecastHigh)
   {
      this.m_ForecastHigh = m_ForecastHigh;
   }

   public String getForecastLow()
   {
      return m_ForecastLow;
   }

   public void setForecastLow(String m_ForecastLow)
   {
      this.m_ForecastLow = m_ForecastLow;
   }

   public String getForecastCondition()
   {
      return m_ForecastCondition;
   }

   public void setForecastCondition(String m_ForecastCondition)
   {
      this.m_ForecastCondition = m_ForecastCondition;
   }

   public String getLogo()
   {
      return m_Logo;
   }

   public void setLogo(String m_Logo)
   {
      this.m_Logo = m_Logo;
   }

   public String getURL()
   {
      return m_URL;
   }

   public void setURL(String m_URL)
   {
      this.m_URL = m_URL;
   }

}
