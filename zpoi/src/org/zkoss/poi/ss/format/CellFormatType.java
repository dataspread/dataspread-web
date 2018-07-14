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

import java.util.Locale;

/**
 * The different kinds of formats that the formatter understands.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public enum CellFormatType { //ZSS-68

    /** The general (default) format; also used for <tt>"General"</tt>. */
    GENERAL {
        CellFormatter formatter(String pattern, Locale locale) {
            return new CellGeneralFormatter(locale);
        }
        boolean isSpecial(char ch) {
            return false;
        }
    },
    /** A numeric format. */
    NUMBER {
        boolean isSpecial(char ch) {
            return false;
        }
        CellFormatter formatter(String pattern, Locale locale) {
            return new CellNumberFormatter(pattern, locale);
        }
    },
    /** A date format. */
    DATE {
        boolean isSpecial(char ch) {
            return ch == '\'' || (ch <= '\u007f' && Character.isLetter(ch));
        }
        CellFormatter formatter(String pattern, Locale locale) {
            return new CellDateFormatter(pattern, locale);
        }
    },
    /** An elapsed time format. */
    ELAPSED {
        boolean isSpecial(char ch) {
            return false;
        }
        CellFormatter formatter(String pattern, Locale locale) {
            return new CellElapsedFormatter(pattern, locale);
        }
    },
    /** A text format. */
    TEXT {
        boolean isSpecial(char ch) {
            return false;
        }
        CellFormatter formatter(String pattern, Locale locale) {
            return new CellTextFormatter(pattern, locale);
        }
    };

    /**
     * Returns <tt>true</tt> if the format is special and needs to be quoted.
     *
     * @param ch The character to test.
     *
     * @return <tt>true</tt> if the format is special and needs to be quoted.
     */
    abstract boolean isSpecial(char ch);

    /**
     * Returns a new formatter of the appropriate type, for the given pattern.
     * The pattern must be appropriate for the type.
     *
     * @param pattern The pattern to use.
     *
     * @return A new formatter of the appropriate type, for the given pattern.
     */
    abstract CellFormatter formatter(String pattern, Locale locale);
}
