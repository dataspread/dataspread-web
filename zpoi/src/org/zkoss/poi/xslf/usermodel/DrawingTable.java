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

package org.zkoss.poi.xslf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;

import java.util.List;

public class DrawingTable {
    private final CTTable table;

    public DrawingTable(CTTable table) {
        this.table = table;
    }

    public DrawingTableRow[] getRows() {
        List<CTTableRow> ctTableRows = table.getTrList();
        DrawingTableRow[] o = new DrawingTableRow[ctTableRows.size()];

        for (int i=0; i<o.length; i++) {
            o[i] = new DrawingTableRow(ctTableRows.get(i));
        }

        return o;
    }
}
