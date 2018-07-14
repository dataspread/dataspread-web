/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.hdf.model.hdftypes;



/**
 * Lightweight representation of a text piece.
 *
 * @author Ryan Ackley
 */
@Deprecated
public final class TextPiece extends PropertyNode implements Comparable
{
  private boolean _usesUnicode;
  private int _length;

  /**
   * @param start Offset in main document stream.
   * @param length The total length of the text in bytes. Note: 1 character
   *        does not necessarily refer to 1 byte.
   * @param unicode true if this text is unicode.
   */
  public TextPiece(int start, int length, boolean unicode)
  {
      super(start, start + length, null);
      _usesUnicode = unicode;
      _length = length;

  }
  /**
   * @return If this text piece uses unicode
   */
   public boolean usesUnicode()
   {
      return _usesUnicode;
   }
}

