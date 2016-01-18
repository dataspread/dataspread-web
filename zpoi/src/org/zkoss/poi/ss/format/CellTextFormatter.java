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
package org.zkoss.poi.ss.format;

import org.zkoss.poi.ss.format.CellFormatPart.PartHandler;
import org.zkoss.util.Pair;

import java.util.Locale;
import java.util.regex.Matcher;

/**
 * This class implements printing out text.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class CellTextFormatter extends CellFormatter {
    private final int[] textPos;
    private final String desc;

    //20111229, henrichen@zkoss.org: ZSS-68
    /*package*/ enum FormatterType {
    	SIMPLE_TEXT;
    }
    //20111229, henrichen@zkoss.org: ZSS-68
    static CellFormatter getFormatter(FormatterType ft, Locale locale) {
    	final Pair key = new Pair(ft, locale);
    	CellFormatter formatter = (CellFormatter) _formatters.get(key);
    	if (formatter != null) {  //in cache, use it
    		return formatter;
    	}
    	switch(ft) {
    	case SIMPLE_TEXT:
        	formatter = new CellTextFormatter("@", locale);
    		break;
    	}
    	_formatters.put(key, formatter); //cache in
    	return formatter;
    }
    
    //static final CellFormatter SIMPLE_TEXT = new CellTextFormatter("@");

    public CellTextFormatter(String format, Locale locale) { //20111229, henrichen@zkoss.org: ZSS-68
        super(format, locale);

        final int[] numPlaces = new int[1];

        desc = CellFormatPart.parseFormat(format, CellFormatType.TEXT,
                new PartHandler() {
                    public String handlePart(Matcher m, String part,
                            CellFormatType type, StringBuffer desc) {
                        if (part.equals("@")) {
                            numPlaces[0]++;
                            return "\u0000";
                        }
                        return null;
                    }
                }).toString();

        // Remember the "@" positions in last-to-first order (to make insertion easier)
        textPos = new int[numPlaces[0]];
        int pos = desc.length() - 1;
        for (int i = 0; i < textPos.length; i++) {
            textPos[i] = desc.lastIndexOf("\u0000", pos);
            pos = textPos[i] - 1;
        }
    }

    /** {@inheritDoc} */
    public void formatValue(StringBuffer toAppendTo, Object obj) {
        int start = toAppendTo.length();
        String text = obj.toString();
        if (obj instanceof Boolean) {
            text = text.toUpperCase();
        }
        toAppendTo.append(desc);
        for (int i = 0; i < textPos.length; i++) {
            int pos = start + textPos[i];
            toAppendTo.replace(pos, pos + 1, text);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For text, this is just printing the text.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        getFormatter(FormatterType.SIMPLE_TEXT, locale).formatValue(toAppendTo, value); //ZSS-68
    }
}
