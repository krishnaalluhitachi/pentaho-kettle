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

package org.pentaho.di.core.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.di.core.Const;


/**
 * Test class for the basic functionality of StringUtil.
 *
 * @author Sven Boden
 */

public class StringUtilTest extends TestCase {
  /**
   * Test initCap for JIRA PDI-619.
   */
  public void testinitCap() {
    assertEquals( "", StringUtil.initCap( null ) );
    assertEquals( "", StringUtil.initCap( "" ) );
    assertEquals( "", StringUtil.initCap( "   " ) );

    assertEquals( "A word", StringUtil.initCap( "a word" ) );
    assertEquals( "A word", StringUtil.initCap( "A word" ) );

    assertEquals( "Award", StringUtil.initCap( "award" ) );
    assertEquals( "Award", StringUtil.initCap( "Award" ) );

    assertEquals( "AWard", StringUtil.initCap( "aWard" ) );
    assertEquals( "AWard", StringUtil.initCap( "AWard" ) );
  }

  /**
   * Create an example map to be used for variable resolution.
   *
   * @return Map of variablenames/values.
   */
  Map<String, String> createVariables1( String open, String close ) {
    Map<String, String> map = new HashMap<String, String>();

    map.put( "NULL", null );
    map.put( "EMPTY", "" );
    map.put( "checkcase", "case1" );
    map.put( "CheckCase", "case2" );
    map.put( "CHECKCASE", "case3" );
    map.put( "VARIABLE_1", "VARIABLE1" );

    map.put( "recursive1", "A" + open + "recursive2" + close );
    map.put( "recursive2", "recurse" );

    map.put( "recursive3", open + "recursive4" + close + "B" );
    map.put( "recursive4", "recurse" );

    map.put( "recursive5", open + "recursive6" + close + "B" );
    map.put( "recursive6", "Z" + open + "recursive7" + close );
    map.put( "recursive7", "final" );

    // endless recursive
    map.put( "recursive_all", open + "recursive_all1" + close + " tail" );
    map.put( "recursive_all1", open + "recursive_all" + close + " tail1" );

    return map;
  }

  /**
   * Test the basic substitute call.
   */
  public void testSubstituteBasic() {
    Map<String, String> map = createVariables1( "${", "}" );
    assertEquals( "|${BAD_KEY}|", StringUtil.substitute( "|${BAD_KEY}|", map, "${", "}" ) );
    assertEquals( "|${NULL}|", StringUtil.substitute( "|${NULL}|", map, "${", "}" ) );
    assertEquals( "||", StringUtil.substitute( "|${EMPTY}|", map, "${", "}" ) );
    assertEquals( "|case1|", StringUtil.substitute( "|${checkcase}|", map, "${", "}" ) );
    assertEquals( "|case2|", StringUtil.substitute( "|${CheckCase}|", map, "${", "}" ) );
    assertEquals( "|case3|", StringUtil.substitute( "|${CHECKCASE}|", map, "${", "}" ) );
    assertEquals( "|Arecurse|", StringUtil.substitute( "|${recursive1}|", map, "${", "}" ) );
    assertEquals( "|recurseB|", StringUtil.substitute( "|${recursive3}|", map, "${", "}" ) );
    assertEquals( "|ZfinalB|", StringUtil.substitute( "|${recursive5}|", map, "${", "}" ) );

    try {
      StringUtil.substitute( "|${recursive_all}|", map, "${", "}", 0 );
      fail( "recursive check is failing" );
    } catch ( RuntimeException rex ) {
    }

    map = createVariables1( "%%", "%%" );
    assertEquals( "||", StringUtil.substitute( "|%%EMPTY%%|", map, "%%", "%%" ) );
    assertEquals( "|case1|", StringUtil.substitute( "|%%checkcase%%|", map, "%%", "%%" ) );
    assertEquals( "|case2|", StringUtil.substitute( "|%%CheckCase%%|", map, "%%", "%%" ) );
    assertEquals( "|case3|", StringUtil.substitute( "|%%CHECKCASE%%|", map, "%%", "%%" ) );
    assertEquals( "|Arecurse|", StringUtil.substitute( "|%%recursive1%%|", map, "%%", "%%" ) );
    assertEquals( "|recurseB|", StringUtil.substitute( "|%%recursive3%%|", map, "%%", "%%" ) );
    assertEquals( "|ZfinalB|", StringUtil.substitute( "|%%recursive5%%|", map, "%%", "%%" ) );

    try {
      StringUtil.substitute( "|%%recursive_all%%|", map, "%%", "%%" );
      fail( "recursive check is failing" );
    } catch ( RuntimeException rex ) {
    }

    map = createVariables1( "${", "}" );
    assertEquals( "||", StringUtil.environmentSubstitute( "|%%EMPTY%%|", map ) );
    assertEquals( "|case1|", StringUtil.environmentSubstitute( "|%%checkcase%%|", map ) );
    assertEquals( "|case2|", StringUtil.environmentSubstitute( "|%%CheckCase%%|", map ) );
    assertEquals( "|case3|", StringUtil.environmentSubstitute( "|%%CHECKCASE%%|", map ) );
    assertEquals( "|Arecurse|", StringUtil.environmentSubstitute( "|%%recursive1%%|", map ) );
    assertEquals( "|recurseB|", StringUtil.environmentSubstitute( "|%%recursive3%%|", map ) );
    assertEquals( "|ZfinalB|", StringUtil.environmentSubstitute( "|%%recursive5%%|", map ) );

    try {
      StringUtil.environmentSubstitute( "|%%recursive_all%%|", map );
      fail( "recursive check is failing" );
    } catch ( RuntimeException rex ) {
    }
  }

  /**
   * Test isEmpty() call.
   */
  public void testIsEmpty() {
    assertTrue( StringUtil.isEmpty( (String) null ) );
    assertTrue( StringUtil.isEmpty( "" ) );

    assertFalse( StringUtil.isEmpty( "A" ) );
    assertFalse( StringUtil.isEmpty( " A " ) );
  }

  /**
   * Test getIndent() call.
   */
  public void testGetIndent() {
    assertEquals( "", StringUtil.getIndent( 0 ) );
    assertEquals( " ", StringUtil.getIndent( 1 ) );
    assertEquals( "  ", StringUtil.getIndent( 2 ) );
    assertEquals( "   ", StringUtil.getIndent( 3 ) );
  }

  public void testIsVariable() throws Exception {
    assertTrue( StringUtil.isVariable( "${somename}" ) );
    assertTrue( StringUtil.isVariable( "%%somename%%" ) );
    assertTrue( StringUtil.isVariable( "$[somename]" ) );
    assertFalse( StringUtil.isVariable( "somename" ) );
    assertFalse( StringUtil.isVariable( null ) );
  }

  public void testSafeToLowerCase() {
    assertNull( StringUtil.safeToLowerCase( null ) );
    assertEquals( "", StringUtil.safeToLowerCase( "" ) );
    assertEquals( " ", StringUtil.safeToLowerCase( " " ) );
    assertEquals( "abc123", StringUtil.safeToLowerCase( "abc123" ) );
    assertEquals( "abc123", StringUtil.safeToLowerCase( "Abc123" ) );
    assertEquals( "abc123", StringUtil.safeToLowerCase( "ABC123" ) );
    assertNull( StringUtil.safeToLowerCase( new ToString() ) );
    assertNull( StringUtil.safeToLowerCase( ( new ToString() ).toString() ) );
    assertEquals( "abc123", StringUtil.safeToLowerCase( new ToString( "ABC123" ) ) );
    assertEquals( "abc123", StringUtil.safeToLowerCase( ( new ToString( "ABC123" ) ).toString() ) );
  }

  @Test
  public void testHasVariable() {
    assertTrue( StringUtil.hasVariable( "abc${foo}" ) );
    assertTrue( StringUtil.hasVariable( "abc%%foo%%efg" ) );
    assertTrue( StringUtil.hasVariable( "$[foo]abc" ) );
    assertFalse( "Open and close ordered improperly", StringUtil.hasVariable( "a}bc${foo" ) );
    assertFalse( "Variable is not closed", StringUtil.hasVariable( "abc${foo" ) );
    assertFalse( "Variable is not opened", StringUtil.hasVariable( "abcfoo}" ) );
    assertFalse( "no variable present to substitute", StringUtil.hasVariable( "abc${}foo" ) );
  }

  class ToString {
    private String string;

    ToString() {
    }

    ToString( final String string ) {
      this.string = string;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  @Test
  public void testTrimStart_Single() {
    assertEquals( "file/path/", StringUtil.trimStart( "/file/path/", '/' ) );
  }

  @Test
  public void testTrimStart_Many() {
    assertEquals( "file/path/", StringUtil.trimStart( "////file/path/", '/' ) );
  }

  @Test
  public void testTrimStart_None() {
    assertEquals( "file/path/", StringUtil.trimStart( "file/path/", '/' ) );
  }

  @Test
  public void testTrimEnd_Single() {
    assertEquals( "/file/path", StringUtil.trimEnd( "/file/path/", '/' ) );
  }

  @Test
  public void testTrimEnd_Many() {
    assertEquals( "/file/path", StringUtil.trimEnd( "/file/path///", '/' ) );
  }

  @Test
  public void testTrimEnd_None() {
    assertEquals( "/file/path", StringUtil.trimEnd( "/file/path", '/' ) );
  }

  @Test
  public void testSubstituteHex_ignoreEmptyArrayDefinition() throws Exception {
    String text = "holdings.$[].as_of_dt";
    assertEquals( text, StringUtil.substituteHex( text ) );
  }

  @Test
  public void environmentSubstituteHexTest() {
    String result = StringUtil.environmentSubstitute( "$[31,32,33,34,35,36]", createVariables1( "${", "}" ) );
    assertEquals( "123456", result );
  }

  @Test
  public void environmentSubstituteHexEscapeTest() {
    String result = StringUtil.environmentSubstitute( "$[31,32,33,34,35,36]", createVariables1( "${", "}" ), true );
    assertEquals( "$[31,32,33,34,35,36]", result );
  }

  @Test
  public void testToUri() {
    if ( Const.getOS().startsWith("Windows") ) {
      assertEquals( "file:/c:/tmp/file", StringUtil.toUri( "c:\\tmp\\file" ).toString() );
      assertEquals( "file:/D:/tmp/file", StringUtil.toUri( "D:\\tmp\\file" ).toString() );
      assertEquals( "file:/C:/tmp/file", StringUtil.toUri( "/tmp/file").toString() );
    } else {
      assertEquals( "file:/tmp/file", StringUtil.toUri( "/tmp/file").toString() );
    }
    assertEquals( "hc://cluster/user/file", StringUtil.toUri( "hc://cluster/user/file" ).toString() );
    assertEquals( "pvfs://pvfsconn/user/file", StringUtil.toUri( "pvfs://pvfsconn/user/file" ).toString() );
  }

}
