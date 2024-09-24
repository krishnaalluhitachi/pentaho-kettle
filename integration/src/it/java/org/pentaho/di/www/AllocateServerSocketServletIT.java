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

package org.pentaho.di.www;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class AllocateServerSocketServletIT {
  private TransformationMap mockTransformationMap;

  private AllocateServerSocketServlet allocateServerSocketServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    allocateServerSocketServlet = new AllocateServerSocketServlet( mockTransformationMap );
  }

  @Test
  public void testAllocateServerSocketServletEncodesParametersForHmtlResponse() throws ServletException,
    IOException {
    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = mock( HttpServletResponse.class );
    SocketPortAllocation mockSocketPortAllocation = mock( SocketPortAllocation.class );
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ServletOutputStream servletOutputStream = new ServletOutputStream() {

      @Override
      public void write( int b ) throws IOException {
        byteArrayOutputStream.write( b );
      }
    };

    when( mockRequest.getContextPath() ).thenReturn( AllocateServerSocketServlet.CONTEXT_PATH );
    when( mockRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
    when( mockResponse.getOutputStream() ).thenReturn( servletOutputStream );
    when(
      mockTransformationMap.allocateServerSocketPort(
        anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
        anyString(), anyString() ) ).thenReturn( mockSocketPortAllocation );

    allocateServerSocketServlet.doGet( mockRequest, mockResponse );

    String response = byteArrayOutputStream.toString();
    // Pull out dynamic part of body, remove hardcoded html
    String dynamicBody =
      ServletTestUtils
        .getInsideOfTag( "BODY", response ).replaceAll( "<p>", "" ).replaceAll( "<br>", "" ).replaceAll(
          "<H1>.+</H1>", "" ).replaceAll( "--> port", "" );
    assertFalse( ServletTestUtils.hasBadText( dynamicBody ) );
  }
}
