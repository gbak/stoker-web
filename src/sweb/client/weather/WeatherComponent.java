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

package sweb.client.weather;

import sweb.shared.model.weather.WeatherData;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class WeatherComponent extends Composite
{

   DecoratorPanel dp = new DecoratorPanel();
   Label m_currentTempLabel = new Label();
   Label m_HighLow = new Label();
   Label m_WindSpeed = new Label();
   Label m_Text = new Label();
   Label m_Humidity = new Label();
   Label m_CityState = new Label();
   Image m_CurrentImage = new Image();
   Image m_YahooImage = new Image();
   String m_YahooURL = new String();


   public WeatherComponent( WeatherData w )
   {
      HorizontalPanel hpSouth = new HorizontalPanel();

      hpSouth.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

      Image imageYahooLogo = m_YahooImage;

      imageYahooLogo.setHeight("15px");
      m_CurrentImage.setHeight("30px");

      m_CurrentImage.setStyleName("weatherLabel");
      m_Text.setStyleName("weatherValue");
      m_currentTempLabel .setStyleName("weatherValue");

      hpSouth.add(m_CurrentImage);
      hpSouth.add( m_Text);

      hpSouth.add( createPair("Current Temp: ", m_currentTempLabel));
      hpSouth.add( createPair("Today's High/Low:", m_HighLow));
      hpSouth.add( createPair("Wind speed:", m_WindSpeed));
      hpSouth.add( createPair("Humidity:", m_Humidity));

      m_CityState.setStyleName("weatherValue");
      hpSouth.add( m_CityState );
      hpSouth.add( imageYahooLogo);

      hpSouth.setWidth("100%");
      dp.setWidth("100%");
      dp.add( hpSouth );

      initWidget( dp );
   }

   private HorizontalPanel createPair(String header,Label value)
   {
       HorizontalPanel hp = new HorizontalPanel();


       Label head = new Label(header);
       head.setStyleName("weatherLabel");
       value.setStyleName("weatherValue");
       value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
       hp.add( head );
       hp.add( value);

       return hp;

   }

   public void update(WeatherData wd)
   {
       m_currentTempLabel.setText(wd.getCurrentTemperature());
       m_HighLow.setText(wd.getForecastHigh() + "/" + wd.getForecastLow());
       m_WindSpeed.setText(wd.getWindSpeed());
       m_Text.setText(wd.getText());
       m_CurrentImage.setUrl(wd.getCurrentImage());
       m_Humidity.setText(wd.getHumidity() + " %");
       m_CityState.setText(wd.getCity() + "," + wd.getState());
       m_YahooImage.setUrl(wd.getLogo());

   }

   /* public WeatherComponent( WeatherData w )
   {
      // wd = w;
      HorizontalPanel hpSouth = new HorizontalPanel();

      final FlexTable flexTable = new FlexTable();
      FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();

      //final WeatherData wd = w;
      Image imageYahooLogo = m_YahooImage;
      imageYahooLogo.setSize("126px", "20px");
      imageYahooLogo.addClickHandler( new ClickHandler() {
         public void onClick(ClickEvent event)
         {
            com.google.gwt.user.client.Window.open(wd.getURL(), "Yahoo! Weather", "");
         }
      });
      //flexTable.setWidth("32em");
      flexTable.setCellSpacing(5);
      flexTable.setCellPadding(3);
      flexTable.setWidth("100%");

    //  flexTable.setBorderWidth(1);
      cellFormatter.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
      //flexTable.setWidget(1,0,imageYahooLogo );
     // cellFormatter.setColSpan(0, 0, 2);
      //cellFormatter.setRowSpan(0,0,1);

      flexTable.setWidget( 0,0, m_CurrentImage );
      cellFormatter.setRowSpan(0,0,2);

      flexTable.setWidget( 1,0, m_Text);

      flexTable.setWidget( 0,2, new Label("Current Temp: "));
      flexTable.setWidget( 0,3, m_currentTempLabel );
      cellFormatter.setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);


      flexTable.setWidget( 1,1, new Label("Today's High/Low:"));
      flexTable.setWidget( 1,2, m_HighLow );
      cellFormatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);

      flexTable.setWidget( 0,4, new Label("Wind speed: "));
      flexTable.setWidget( 0,5, m_WindSpeed );
      cellFormatter.setHorizontalAlignment(0, 4, HasHorizontalAlignment.ALIGN_RIGHT);

      flexTable.setWidget( 1,3, new Label("Humidity:"));
      flexTable.setWidget( 1,4, m_Humidity );
      cellFormatter.setHorizontalAlignment(1, 3, HasHorizontalAlignment.ALIGN_RIGHT);

      flexTable.setWidget( 1,5, m_CityState );

      flexTable.setWidget(1,6,imageYahooLogo );

      //hpSouth.add( imageYahooLogo );
      //hpSouth.add( new Image(wd.getCurrentImage()));
      //hpSouth.add( new Label("Current Temp: "));
      //hpSouth.add( new Label(wd.getCurrentTemperature()));
      //hpSouth.add( new Label("Wind Speed: "));
      //hpSouth.add( new Label(wd.getWindSpeed()));
      //hpSouth.add( new Label("Today's High/Low:"));
      //hpSouth.add( new Label(wd.getForecastHigh() + "/" + wd.getForecastLow()) );

      dp.add( flexTable );

      initWidget( dp );
   }*/
}
