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
package org.zkoss.poi.ss.usermodel.charts;

/**
 * Represents text model of the charts.
 * 
 * @author henrichen@zkoss.org
 *
 */
public interface ChartTextSource {
    /**
     * Returns {@code true} if charts text source is valid cell range.
     *
     * @return {@code true} if charts text source is valid cell range
     */
    boolean isReference();

    /**
     * Returns the text String(cached string if a reference)
     * @return
     */
    String getTextString();
    
    /**
     * Returns formula representation of the text source. It is only applicable
     * for text source that is valid cell range.
     *
     * @return formula representation of the text source
     * @throws {@code UnsupportedOperationException} if the text source is not a
     *                reference.
     */
    String getFormulaString();
    
    //20111007, henrichen@zkoss.org: when sheet name change, the reference has to be adjusted accordingly
    void renameSheet(String oldname, String newname);
}
