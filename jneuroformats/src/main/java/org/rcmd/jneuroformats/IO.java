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

package org.rcmd.jneuroformats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;


public class IO {

    /**
     * Read a newline terminated string from a ByteBuffer.
     * You should be sure that the string is terminated by a newline, otherwise this will read until the end of the buffer and then throw an exception from the `ByteBuffer.get()` call.
     * @param buffer the ByteBuffer to read from
     * @return the string
     */
    protected static String readNewlineTerminatedString(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        char c = (char) getUint8(buffer);
        while (c != '\n') {
            builder.append(c);
            c = (char) getUint8(buffer);
        }
        return builder.toString();
    }

    /**
     * Read a fixed-length ASCII string from a ByteBuffer.
     * @param buffer the ByteBuffer to read from
     * @param length the length of the string to read, in bytes
     * @return the string
     */
    protected static String readFixedLengthString(ByteBuffer buffer, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) getUint8(buffer));
        }
        return builder.toString();
    }

    /**
     * Read a 4-byte integer from a ByteBuffer.
     * @param buffer the ByteBuffer to read from
     * @return the integer
     */
    protected static int getUint8(ByteBuffer buffer) {
        int pos = buffer.position();
        byte b = buffer.get(pos);
        buffer.position(pos + 1);
        return b & 0xFF;
    }

    /**
     * Convert a list of bytes to a byte array.
     * @param integers the list of bytes
     * @return the byte array
     */
    protected static byte[] convertBytes(List<Byte> bytes) {
        byte[] ret = new byte[bytes.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bytes.get(i).byteValue();
        }
        return ret;
    }

    /**
     * Peak into the contents of a file, and return a ByteBuffer with at most the first maxBytes bytes.
     *
     * This is mostly used to determine the byte order of a file, and to determine whether the file is gzipped,
     * or to determine the file type from the header.
     * @param filePath the path to the file
     * @param use_gzip whether the file is gzipped
     * @param byteOrder the byte order of the file, one of ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN
     * @param maxBytes the maximum number of bytes to read. If the file is smaller than this, the whole file is read, and the number of bytes in the returned ByteBuffer will be less (the size of the file).
     * @return a ByteBuffer with the contents of the file, up to the requested number of bytes.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file is not found.
     */
    protected static ByteBuffer peakIntoFile(Path filePath, Boolean use_gzip, ByteOrder byteOrder, int maxBytes) throws IOException, FileNotFoundException {
        InputStream is = new FileInputStream(filePath.toFile());
        if (use_gzip) {
            is = new GZIPInputStream(is);
        }

        ArrayList<Byte> allBytes = new ArrayList<Byte>();
        byte[] buffer = new byte[maxBytes];
        int numRead;
        while ((numRead = is.read(buffer)) != -1) {
            for (int i = 0; i < numRead; i++) {
                allBytes.add(buffer[i]);
            }
        }

        byte[] ab = convertBytes(allBytes);
        ByteBuffer bbuffer = ByteBuffer.wrap(ab);
        bbuffer = bbuffer.order(byteOrder);

        return bbuffer;
    }

    /**
     * Read full file contnets into a ByteBuffer. The file can be in gzip format or not.
     * @param filePath the path to the file
     * @param use_gzip whether the file is gzipped
     * @param byteOrder the byte order of the file, one of ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN
     * @return a ByteBuffer with the contents of the file.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file is not found.
     */
    protected static ByteBuffer readAllFileBytesGzipOrNot(Path filePath, Boolean use_gzip, ByteOrder byteOrder) throws IOException, FileNotFoundException {
        ByteBuffer buffer;
        if (use_gzip) {
            InputStream is = new GZIPInputStream(new FileInputStream(filePath.toFile()));
            ArrayList<Byte> allBytes = new ArrayList<Byte>();
            byte[] pbuffer = new byte[16535];

            int numRead;
            while ((numRead = is.read(pbuffer)) != -1) {
                for (int i = 0; i < numRead; i++) {
                    allBytes.add(pbuffer[i]);
                }
            }

            byte[] ab = convertBytes(allBytes);
            buffer = ByteBuffer.wrap(ab);

        }
        else {
            buffer = ByteBuffer.wrap(Files.readAllBytes(filePath));
        }
        buffer = buffer.order(byteOrder);
        return buffer;
    }

    /**
     * Write a ByteBuffer to a file, gzipping it.
     * @param filePath the path to the file
     * @param buffer the ByteBuffer to write
     * @throws IOException if IO error occurs.
     */
    protected static void writeGzipFile(Path filePath, ByteBuffer buffer) throws IOException {
        WritableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Channels.newOutputStream(channel));
        gzipOutputStream.write(buffer.array());
        gzipOutputStream.close();
    }

}
