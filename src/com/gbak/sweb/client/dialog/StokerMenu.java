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

package com.gbak.sweb.client.dialog;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;

public class StokerMenu extends MenuBar
{
//   MenuBar MainMenubar = new MenuBar();
   Command profilesCommand = null;
   Command signInCommand = null;

   public StokerMenu()
   {
      super();
      profilesCommand = new Command() {

         public void execute()
         {
            System.out.println("Profiles selected");
         }

      };

      signInCommand = new Command() {

         public void execute()
         {
            System.out.println("Sign-In selected");
         }

      };

      buildMenu();
   }

   private void buildMenu()
   {

    //  addItem("Profiles",false,profilesCommand);
      addItem("Sign-In", false, signInCommand );

      setAutoOpen(false);
      setWidth("100%");
      setAnimationEnabled(true);

   }

}
