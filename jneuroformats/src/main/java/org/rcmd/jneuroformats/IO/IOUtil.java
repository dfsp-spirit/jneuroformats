
package org.rcmd.jneuroformats.IO;


import java.nio.ByteBuffer;


public class IOUtil {

    public static String readNewlineTerminatedString(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        char c = buffer.getChar();
        while (c != '\n') {
            builder.append(c);
            c = buffer.getChar();
        }
        return builder.toString();
    }


}