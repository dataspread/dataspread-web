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

package org.zkoss.poi.openxml4j.opc.signature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zkoss.poi.openxml4j.exceptions.InvalidFormatException;
import org.zkoss.poi.openxml4j.exceptions.OpenXML4JException;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.internal.ContentType;

public final class PackageDigitalSignature extends PackagePart {

	public PackageDigitalSignature() throws InvalidFormatException {
		super(null, null, new ContentType(""));
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	protected InputStream getInputStreamImpl() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OutputStream getOutputStreamImpl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean load(InputStream ios) throws InvalidFormatException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean save(OutputStream zos) throws OpenXML4JException {
		// TODO Auto-generated method stub
		return false;
	}
}
