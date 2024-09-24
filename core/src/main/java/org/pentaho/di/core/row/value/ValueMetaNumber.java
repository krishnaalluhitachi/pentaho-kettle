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

package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;

public class ValueMetaNumber extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaNumber() {
    this( null );
  }

  public ValueMetaNumber( String name ) {
    super( name, ValueMetaInterface.TYPE_NUMBER );
  }

  public ValueMetaNumber( String name, int length, int precision ) {
    super( name, ValueMetaInterface.TYPE_NUMBER, length, precision );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getNumber( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return Double.class;
  }

  @Override
  public String getFormatMask() {
    return getNumberFormatMask();
  }
}
