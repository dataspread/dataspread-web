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

package org.zkoss.poi.hmef.attribute;

import java.util.Date;

import org.zkoss.poi.hmef.Attachment;
import org.zkoss.poi.hmef.HMEFMessage;
import org.zkoss.poi.hpsf.Util;
import org.zkoss.poi.hsmf.datatypes.MAPIProperty;
import org.zkoss.poi.util.LittleEndian;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;

/**
 * A pure-MAPI attribute holding a Date, which applies 
 *  to a {@link HMEFMessage} or one of its {@link Attachment}s.
 */
public final class MAPIDateAttribute extends MAPIAttribute {
   private static POILogger logger = POILogFactory.getLogger(MAPIDateAttribute.class);
   private Date data;
   
   /**
    * Constructs a single new date attribute from the id, type,
    *  and the contents of the stream
    */
   protected MAPIDateAttribute(MAPIProperty property, int type, byte[] data) {
      super(property, type, data);
      
      // The value is a 64 bit Windows Filetime
      this.data = Util.filetimeToDate(
            LittleEndian.getLong(data, 0)
      );
   }

   public Date getDate() {
      return this.data;
   }
   
   public String toString() {
      return getProperty().toString() + " " + data.toString();
   }
   
   /**
    * Returns the Date of a Attribute, converting as appropriate
    */
   public static Date getAsDate(MAPIAttribute attr) {
      if(attr == null) {
         return null;
      }
      if(attr instanceof MAPIDateAttribute) {
         return ((MAPIDateAttribute)attr).getDate();
      }
      
      logger.log(POILogger.WARN, "Warning, non date property found: " + attr.toString());
      return null;
  }
}
