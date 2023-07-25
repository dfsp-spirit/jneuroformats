/*
 *  Copyright 2023 Tim Schäfer
 *
 *    Licensed under the MIT License (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        https://github.com/dfsp-spirit/jneuroformats/blob/main/LICENSE or at https://opensource.org/licenses/MIT
 *
 *   Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
        for (int i = 0; i < length; i++) {
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