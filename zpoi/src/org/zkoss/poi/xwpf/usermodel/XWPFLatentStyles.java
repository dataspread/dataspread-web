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
package org.zkoss.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLatentStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLsdException;

public class XWPFLatentStyles {
	private CTLatentStyles latentStyles;
	protected XWPFStyles styles; //LatentStyle shall know styles
	
	protected XWPFLatentStyles(){
	}
	
	protected XWPFLatentStyles(CTLatentStyles latentStyles){
		this(latentStyles,null);
	}
	
	protected XWPFLatentStyles(CTLatentStyles latentStyles, XWPFStyles styles) {
		this.latentStyles=latentStyles;
		this.styles=styles;
	}
	
	/**
	 * checks wheter specific LatentStyleID is a latentStyle
	*/
	protected boolean isLatentStyle(String latentStyleID){	
		for ( CTLsdException lsd: latentStyles.getLsdExceptionList()) {
			if(lsd.getName().equals(latentStyleID));
				return true;
		}
		return false;		
	}
}
