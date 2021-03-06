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

package com.gbak.sweb.server.log.file;

import java.io.File;

import org.apache.log4j.Logger;

import com.gbak.sweb.server.StokerWebConstants;
import com.gbak.sweb.server.StokerWebProperties;
import com.gbak.sweb.shared.model.logfile.LogDir;



public class ListLogFiles
{
   
    private static final String baseDir = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKERWEB_DIR) + 
                                          File.separator + 
                   normalizePath(StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_LOGS_DIR));

    private static final Logger logger = Logger.getLogger(StokerFile.class.getName());
    
    public static LogDir getAllLogFiles()
    {
        return getFiles(baseDir);
    }

    private static String normalizePath(String path)
    {
        // Replaces all \\... with only one \
        path = path.replaceAll("\\\\+","\\\\");
        // Replaces all //... with only one /
        path = path.replaceAll("/+","/");
        path = path.replace('\\', File.separatorChar);
        path = path.replace('/', File.separatorChar);

        return new File(path).getPath();
    }
    public static LogDir getFiles( String path )
    {

        File root = new File( path );
        File[] list = root.listFiles();
        LogDir ld = new LogDir( path, "" );

        for ( File f : list ) {
            logger.debug("LogDir:getFiles() processing [" + f.getName() + "]");
            if ( f.isDirectory() )
            {
                logger.debug( "LogDir:getFiles() Dir:" + f.getAbsoluteFile() );
                logger.debug( "LogDir:getFiles() f.getAbsolutePath() [" + f.getAbsolutePath() + "]");
                logger.debug( "LogDir:getFiles() baseDir [" + baseDir + "]");
                logger.debug( "LogDir:getFiles() f.getAbsolutePath().indexOf(baseDir) [" + f.getAbsolutePath().indexOf(baseDir) +"]");
                String s = f.getAbsolutePath().substring(f.getAbsolutePath().indexOf(baseDir));
                logger.debug("LogDir:getFiles()  Proceeding to dir: [" + s + "]" );
                ld.addDir( getFiles( s ));

            }
            else
            {
                logger.debug("LogDir:getFiles()  Adding file: [" + f.getName() + "]" );
                ld.addFile( f.getName() );

            }
        }
        return ld;
    }

    public static String getFullPathForFile( String fileName )
    {
        return getFullPathForFile( fileName, null );
    }

    private static String getFullPathForFile( String fileName, String path )
    {
        if ( path == null )
        {
            path = baseDir;
        }

        File root = new File( path );
        File[] list = root.listFiles();

        for ( File f: list )
        {
            if ( f.isDirectory() )
            {
                String s = getFullPathForFile( fileName, f.getAbsolutePath() );
                if ( s != null )
                {
                    return s;
                }
            }
            else
            {
                if ( f.getName().compareTo(fileName) == 0)
                {
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }

}
