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
package org.zkoss.poi.poifs.crypt;

import org.zkoss.poi.poifs.filesystem.DirectoryNode;
import org.zkoss.poi.poifs.filesystem.DocumentInputStream;
import org.zkoss.poi.poifs.filesystem.NPOIFSFileSystem;
import org.zkoss.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;

/**
 *  @author Maxim Valyanskiy
 *  @author Gary King
 */
public class EncryptionInfo {
    private final int versionMajor;
    private final int versionMinor;
    private final int encryptionFlags;

    private final EncryptionHeader header;
    private final EncryptionVerifier verifier;

    public EncryptionInfo(POIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    public EncryptionInfo(NPOIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    public EncryptionInfo(DirectoryNode dir) throws IOException {
        DocumentInputStream dis = dir.createDocumentInputStream("EncryptionInfo");
        versionMajor = dis.readShort();
        versionMinor = dis.readShort();

        encryptionFlags = dis.readInt();

        if (versionMajor == 4 && versionMinor == 4 && encryptionFlags == 0x40) {
            StringBuilder builder = new StringBuilder();
            byte[] xmlDescriptor = new byte[dis.available()];
            dis.read(xmlDescriptor);
            for (byte b : xmlDescriptor)
                builder.append((char)b);
            String descriptor = builder.toString();
            header = new EncryptionHeader(descriptor);
            verifier = new EncryptionVerifier(descriptor);
        } else {
            int hSize = dis.readInt();
            header = new EncryptionHeader(dis);
            if (header.getAlgorithm()==EncryptionHeader.ALGORITHM_RC4) {
                verifier = new EncryptionVerifier(dis, 20);
            } else {
                verifier = new EncryptionVerifier(dis, 32);
            }
        }
    }

    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public int getEncryptionFlags() {
        return encryptionFlags;
    }

    public EncryptionHeader getHeader() {
        return header;
    }

    public EncryptionVerifier getVerifier() {
        return verifier;
    }
}
