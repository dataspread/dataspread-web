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

package org.zkoss.poi.hwpf.model;

import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.util.LittleEndian;

@Internal
public final class PieceDescriptor
{

  short descriptor;
   private static BitField fNoParaLast = BitFieldFactory.getInstance(0x01);
   private static BitField fPaphNil = BitFieldFactory.getInstance(0x02);
   private static BitField fCopied = BitFieldFactory.getInstance(0x04);
  int fc;
  PropertyModifier prm;
  boolean unicode;


  public PieceDescriptor(byte[] buf, int offset)
  {
    descriptor = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    fc = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    prm = new PropertyModifier( LittleEndian.getShort(buf, offset));

    // see if this piece uses unicode.
    if ((fc & 0x40000000) == 0)
    {
        unicode = true;
    }
    else
    {
        unicode = false;
        fc &= ~(0x40000000);//gives me FC in doc stream
        fc /= 2;
    }

  }

  public int getFilePosition()
  {
    return fc;
  }

  public void setFilePosition(int pos)
  {
    fc = pos;
  }

  public boolean isUnicode()
  {
    return unicode;
  }

    public PropertyModifier getPrm()
    {
        return prm;
    }

  protected byte[] toByteArray()
  {
    // set up the fc
    int tempFc = fc;
    if (!unicode)
    {
      tempFc *= 2;
      tempFc |= (0x40000000);
    }

    int offset = 0;
    byte[] buf = new byte[8];
    LittleEndian.putShort(buf, offset, descriptor);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, tempFc);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putShort(buf, offset, prm.getValue());

    return buf;

  }

  public static int getSizeInBytes()
  {
    return 8;
  }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + descriptor;
        result = prime * result + ( ( prm == null ) ? 0 : prm.hashCode() );
        result = prime * result + ( unicode ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        PieceDescriptor other = (PieceDescriptor) obj;
        if ( descriptor != other.descriptor )
            return false;
        if ( prm == null )
        {
            if ( other.prm != null )
                return false;
        }
        else if ( !prm.equals( other.prm ) )
            return false;
        if ( unicode != other.unicode )
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "PieceDescriptor (pos: " + getFilePosition() + "; "
                + ( isUnicode() ? "unicode" : "non-unicode" ) + "; prm: "
                + getPrm() + ")";
    }
}
