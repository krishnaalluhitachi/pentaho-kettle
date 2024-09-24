/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.zipfile;

import org.apache.commons.vfs2.FileObject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import java.io.File;
import java.io.IOException;

public class JobEntryZipFileMoveFilesTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    JobEntryZipFile jobEntryZipFile = new JobEntryZipFile();

    @Test
    public void testFoldersToMove() throws IOException {
        File sourceFileorFolderPath =  temporaryFolder.newFolder( "Source" );
        File destFolder = temporaryFolder.newFolder( "Dest" );
        FileObject sourceFileOrFolder = null;
        FileObject[] fileObjects = new FileObject[2];
        boolean result;
        int i=0;
        //Try to duplicate the functionality of moving files
        //without zipping and with Junit TemporaryFolder feature
        try {
            sourceFileOrFolder = KettleVFS.getFileObject( sourceFileorFolderPath.toString() );
            //Creating source files/folders
            while ( true ) {
                File sourceFile = temporaryFolder.newFile( "/Source/"+"source" + i+".txt" );
                fileObjects[i] = KettleVFS.getFileObject( sourceFile.toString() );
                i++;
                if ( i == 2 )
                    break;
            }
            for ( FileObject fileObject : fileObjects ) {
                result = jobEntryZipFile.moveFilesToDestinationFolder( sourceFileOrFolder, fileObject,
                        destFolder.toString(), sourceFileOrFolder.isFolder(), fileObject );
                Assert.assertTrue( result );
                fileObject.close();
            }
            sourceFileOrFolder.close();
            sourceFileorFolderPath.delete();
            destFolder.deleteOnExit();
        }
        catch ( KettleFileException e ) {
            e.printStackTrace();
        }
    }
}