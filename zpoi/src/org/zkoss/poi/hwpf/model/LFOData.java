/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.zkoss.poi.hwpf.model;

import java.io.IOException;

import org.zkoss.poi.hwpf.model.io.HWPFOutputStream;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.util.LittleEndian;

/**
 * The LFOData structure contains the Main Document CP of the corresponding LFO,
 * as well as an array of LVL override data.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
public class LFOData
{
    private int _cp;

    private ListFormatOverrideLevel[] _rgLfoLvl;

    public LFOData()
    {
        _cp = 0;
        _rgLfoLvl = new ListFormatOverrideLevel[0];
    }

    LFOData( byte[] buf, int startOffset, int cLfolvl )
    {
        int offset = startOffset;

        _cp = LittleEndian.getInt( buf, offset );
        offset += LittleEndian.INT_SIZE;

        _rgLfoLvl = new ListFormatOverrideLevel[cLfolvl];
        for ( int x = 0; x < cLfolvl; x++ )
        {
            _rgLfoLvl[x] = new ListFormatOverrideLevel( buf, offset );
            offset += _rgLfoLvl[x].getSizeInBytes();
        }
    }

    public int getCp()
    {
        return _cp;
    }

    public ListFormatOverrideLevel[] getRgLfoLvl()
    {
        return _rgLfoLvl;
    }

    public int getSizeInBytes()
    {
        int result = 0;
        result += LittleEndian.INT_SIZE;

        for ( ListFormatOverrideLevel lfolvl : _rgLfoLvl )
            result += lfolvl.getSizeInBytes();

        return result;
    }

    void writeTo( HWPFOutputStream tableStream ) throws IOException
    {
        LittleEndian.putInt( _cp, tableStream );
        for ( ListFormatOverrideLevel lfolvl : _rgLfoLvl )
        {
            tableStream.write( lfolvl.toByteArray() );
        }
    }

}
