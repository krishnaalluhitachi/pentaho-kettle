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

package org.pentaho.di.core.exception;

/**
 * This exception is thrown in case there is an error in the Kettle plugin loader
 *
 * @author matt
 *
 */
public class KettlePluginException extends KettleException {

  private static final long serialVersionUID = -7251001771637436705L;

  public KettlePluginException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public KettlePluginException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * @param message
   */
  public KettlePluginException( String message ) {
    super( message );
  }

  /**
   * @param cause
   */
  public KettlePluginException( Throwable cause ) {
    super( cause );
  }

}
