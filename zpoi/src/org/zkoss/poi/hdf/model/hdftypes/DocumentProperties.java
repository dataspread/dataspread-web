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

import org.zkoss.poi.util.LittleEndian;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */
@Deprecated
public final class DocumentProperties implements HDFType
{

  public boolean _fFacingPages;
  public int _fpc;
  public int _epc;
  public int _rncFtn;
  public int _nFtn;
  public int _rncEdn;
  public int _nEdn;

  public DocumentProperties(byte[] dopArray)
  {
        _fFacingPages = (dopArray[0] & 0x1) > 0;
        _fpc = (dopArray[0] & 0x60) >> 5;

        short num = LittleEndian.getShort(dopArray, 2);
        _rncFtn = (num & 0x3);
        _nFtn = (short)(num & 0xfffc) >> 2;
        num = LittleEndian.getShort(dopArray, 52);
        _rncEdn = num & 0x3;
        _nEdn = (short)(num & 0xfffc) >> 2;
        num = LittleEndian.getShort(dopArray, 54);
        _epc = num & 0x3;
  }
}
