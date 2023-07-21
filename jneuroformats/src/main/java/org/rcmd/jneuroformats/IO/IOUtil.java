/*
 *  This file is part of jneuroformats and subject to its license.
 *  See the LICENSE file that accompanies this distribution, or visit
 *  the project homepage at https://github.com/dfsp-spirit/jneuroformats.
 */

package org.rcmd.jneuroformats.IO;

import java.nio.ByteBuffer;

public class IOUtil {

    public static String readNewlineTerminatedString(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        char c = (char) getUint8(buffer);
        while (c != '\n') {
            builder.append(c);
            c = (char) getUint8(buffer);
        }
        return builder.toString();
    }

    public static String readFixedLengthString(ByteBuffer buffer, int length) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            builder.append((char) getUint8(buffer));
        }
        return builder.toString();
    }

    public static int getUint8(ByteBuffer buffer) {
        int pos = buffer.position();
        byte b = buffer.get(pos);
        buffer.position(pos + 1);
        return b & 0xFF;
    }

}