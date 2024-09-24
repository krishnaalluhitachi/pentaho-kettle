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

package org.pentaho.di.core;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.version.BuildVersion;

/**
 * This class caches database queries so that the same query doesn't get called twice. Queries are often launched to the
 * databases to get information on tables etc.
 *
 * @author Matt
 * @since 15-01-04
 *
 */
public class DBCache {
  @VisibleForTesting
  static DBCache dbCache;

  private Map<DBCacheEntry, RowMetaInterface> cache;
  private boolean useCache;

  private LogChannelInterface log;

  @VisibleForTesting
  static Supplier<String> fileNameSupplier = DBCache::getFilename;

  public void setActive() {
    setActive( true );
  }

  public void setInactive() {
    setActive( false );
  }

  public void setActive( boolean act ) {
    useCache = act;
  }

  public boolean isActive() {
    return useCache;
  }

  public void put( DBCacheEntry entry, RowMetaInterface fields ) {
    if ( !useCache ) {
      return;
    }

    RowMetaInterface copy = fields.clone();
    cache.put( entry, copy );
  }

  /**
   * Get the fields as a row generated by a database cache entry
   *
   * @param entry
   *          the entry to look for
   * @return the fields as a row generated by a database cache entry
   */
  public RowMetaInterface get( DBCacheEntry entry ) {
    if ( !useCache ) {
      return null;
    }

    RowMetaInterface fields = cache.get( entry );
    if ( fields != null ) {
      fields = fields.clone(); // Copy it again!
    }

    return fields;
  }

  public int size() {
    return cache.size();
  }

  /**
   * Clear out all entries of database with a certain name
   *
   * @param dbname
   *          The name of the database for which we want to clear the cache or null if we want to clear it all.
   */
  public void clear( String dbname ) {
    if ( dbname == null ) {
      cache = new ConcurrentHashMap<>();
      setActive();
    } else {
      for ( DBCacheEntry entry : cache.keySet() ) {
        if ( entry.sameDB( dbname ) ) {
          cache.remove( entry );
        }
      }
    }
  }

  public static String getFilename() {
    return Const.getKettleDirectory()
      + Const.FILE_SEPARATOR + "db.cache-" + BuildVersion.getInstance().getVersion();
  }

  private DBCache() throws KettleFileException {
    try {
      clear( null );

      // Serialization support for the DB cache
      //
      log = new LogChannel( "DBCache" );

      String filename = fileNameSupplier.get();
      File file = new File( filename );
      if ( file.canRead() ) {
        log.logDetailed( "Loading database cache from file: [" + filename + "]" );

        try ( DataInputStream dis = new DataInputStream( new FileInputStream( file ) ) ) {
          loadFileToCache( dis );
        }
      } else {
        log.logDetailed( "The database cache doesn't exist yet." );
      }
    } catch ( Exception e ) {
      throw new KettleFileException( "Couldn't read the database cache", e );
    }
  }

  @SuppressWarnings( { "squid:S2189", "squid:S1451" } )
  private void loadFileToCache( DataInputStream dis ) throws KettleFileException, SocketTimeoutException {
    int counter = 0;
    try {
      //noinspection InfiniteLoopStatement Only way to detect EOF on DataInputStream is with exception
      while ( true ) {
        DBCacheEntry entry = new DBCacheEntry( dis );
        RowMetaInterface row = new RowMeta( dis );
        cache.put( entry, row );
        counter++;
      }
    } catch ( KettleEOFException eof ) {
      log.logDetailed( "We read " + counter + " cached rows from the database cache!" );
    }
  }

  public void saveCache() throws KettleFileException {
    try {
      // Serialization support for the DB cache
      //
      String filename = fileNameSupplier.get();
      File file = new File( filename );
      if ( !file.exists() || file.canWrite() ) {

        try ( DataOutputStream dos =
                new DataOutputStream( new BufferedOutputStream( new FileOutputStream( file ), 10000 ) ) ) {

          int counter = 0;

          for ( DBCacheEntry entry : cache.keySet() ) {
            entry.write( dos );

            // Save the corresponding row as well.
            RowMetaInterface rowMeta = get( entry );
            if ( rowMeta != null ) {
              rowMeta.writeMeta( dos );
              counter++;
            } else {
              throw new KettleFileException( "The database cache contains an empty row. We can't save this!" );
            }
          }
          log.logDetailed( "We wrote " + counter + " cached rows to the database cache!" );
        }
      } else {
        throw new KettleFileException( "We can't write to the cache file: " + filename );
      }
    } catch ( Exception e ) {
      throw new KettleFileException( "Couldn't write to the database cache", e );
    }
  }

  /**
   * Create the database cache instance by loading it from disk
   *
   * @return the database cache instance.
   */
  @SuppressWarnings( "squid:S00112" )
  public static DBCache getInstance() {
    if ( dbCache != null ) {
      return dbCache;
    }
    try {
      dbCache = new DBCache();
    } catch ( KettleFileException kfe ) {
      throw new RuntimeException( "Unable to create the database cache: " + kfe.getMessage() );
    }
    return dbCache;
  }

}
