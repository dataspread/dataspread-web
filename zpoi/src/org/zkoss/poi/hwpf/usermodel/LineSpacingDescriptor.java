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

package org.zkoss.poi.hwpf.usermodel;

import org.zkoss.poi.util.LittleEndian;

/**
 * This class is used to determine line spacing for a paragraph.
 *
 * @author Ryan Ackley
 */
public final class LineSpacingDescriptor
  implements Cloneable
{
  short _dyaLine;
  short _fMultiLinespace;

  public LineSpacingDescriptor()
  {
      //see page 181
      _dyaLine = 240;
      _fMultiLinespace = 1;
  }

  public LineSpacingDescriptor(byte[] buf, int offset)
  {
    _dyaLine = LittleEndian.getShort(buf, offset);
    _fMultiLinespace = LittleEndian.getShort(buf, offset + LittleEndian.SHORT_SIZE);
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }

  public void setMultiLinespace(short fMultiLinespace)
  {
    _fMultiLinespace = fMultiLinespace;
  }

  public int toInt()
  {
    byte[] intHolder = new byte[4];
    serialize(intHolder, 0);
    return LittleEndian.getInt(intHolder);
  }

  public void serialize(byte[] buf, int offset)
  {
    LittleEndian.putShort(buf, offset, _dyaLine);
    LittleEndian.putShort(buf, offset + LittleEndian.SHORT_SIZE, _fMultiLinespace);
  }

  public void setDyaLine(short dyaLine)
  {
    _dyaLine = dyaLine;
  }
  public boolean equals(Object o)
  {
    LineSpacingDescriptor lspd = (LineSpacingDescriptor)o;

    return _dyaLine == lspd._dyaLine && _fMultiLinespace == lspd._fMultiLinespace;
  }

    public boolean isEmpty()
    {
        return _dyaLine == 0 && _fMultiLinespace == 0;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[LSPD] EMPTY";

        return "[LSPD] (dyaLine: " + _dyaLine + "; fMultLinespace: "
                + _fMultiLinespace + ")";
    }
}
