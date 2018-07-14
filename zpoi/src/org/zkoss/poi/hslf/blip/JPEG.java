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

package org.zkoss.poi.hslf.blip;

import org.zkoss.poi.hslf.model.Picture;

/**
 * Represents a JPEG picture data in a PPT file
 *
 * @author Yegor Kozlov
 */
public final class JPEG extends Bitmap {

    /**
     * @return type of  this picture
     * @see  org.zkoss.poi.hslf.model.Picture#JPEG
     */
    public int getType(){
        return Picture.JPEG;
    }

    /**
     * JPEG signature is <code>0x46A0</code>
     *
     * @return JPEG signature (<code>0x46A0</code>)
     */
    public int getSignature(){
        return 0x46A0;
    }
}
