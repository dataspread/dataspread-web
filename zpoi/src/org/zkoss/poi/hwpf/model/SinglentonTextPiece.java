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

import java.io.IOException;

import org.zkoss.poi.util.Internal;

@Internal
public class SinglentonTextPiece extends TextPiece
{

    public SinglentonTextPiece( StringBuilder buffer ) throws IOException
    {
        super( 0, buffer.length(), buffer.toString().getBytes( "UTF-16LE" ),
                new PieceDescriptor( new byte[8], 0 ) );
    }

    @Override
    public int bytesLength()
    {
        return getStringBuilder().length() * 2;
    }

    @Override
    public int characterLength()
    {
        return getStringBuilder().length();
    }

    @Override
    public int getCP()
    {
        return 0;
    }

    @Override
    public int getEnd()
    {
        return characterLength();
    }

    @Override
    public int getStart()
    {
        return 0;
    }

    public String toString()
    {
        return "SinglentonTextPiece (" + characterLength() + " chars)";
    }
}
