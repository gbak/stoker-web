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

package sweb.shared.model.logfile;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class LogDir implements Serializable
{
    private static final long serialVersionUID = 9021381651395313667L;

    String dirName;
    String path;

    ArrayList<LogDir> dirList = new ArrayList<LogDir>();
    ArrayList<String> arFiles = new ArrayList<String>();

    public LogDir()
    {

    }

    public LogDir( String name, String parentDirName )
    {
        /*if ( parentDirName == null || parentDirName.length() == 0 ) {
            path = "dirName";
        }
        else
        {
            path = parentDirName + File.separator + name;
        }*/
        dirName = name;
    }

    public void addFile( String fileName )
    {
        System.out.println("logDir - Adding file: " + fileName );
        arFiles.add( fileName );
    }

    public void addDir( LogDir ld )
    {
        System.out.println("logDir - Adding Dir: " + ld.dirName );
        dirList.add( ld );
    }

    public String getName()
    {
        return dirName;
    }

    public ArrayList<LogDir> getDirList()
    {
        return dirList;
    }

    public ArrayList<String> getFileList()
    {
        return arFiles;
    }

}
