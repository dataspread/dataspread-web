/* RangeRunner.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api;

/**
 * The runner to help you run multiple {@link Range} APIs in synchronization. 
 * @author dennis
 * @see Range#sync(RangeRunner)
 * @see Range#setSyncLevel(org.zkoss.zss.api.Range.SyncLevel)
 * @since 3.0.0
 */
public interface RangeRunner {
	/**
	 * main execution method
	 * @param range the range to process
	 */
	void run(Range range);
}
