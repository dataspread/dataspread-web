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

package org.zkoss.poi.hdf.extractor;

import java.util.*;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */
@Deprecated
public final class TableRow
{
  TAP _descriptor;
  ArrayList _cells;

  public TableRow(ArrayList cells, TAP descriptor)
  {
    _cells = cells;
    _descriptor = descriptor;
  }
  public TAP getTAP()
  {
    return _descriptor;
  }
  public ArrayList getCells()
  {
    return _cells;
  }
}
