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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.util.List;

/**
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the structure, and a value that is assigned to all vertices or voxels that are part of the structure. Sometimes the value is not needed, and can be set to 0.
 */
public class FsMgh {

    public static final int MRI_UCHAR = 0;
    public static final int MRI_INT = 1;
    public static final int MRI_FLOAT = 3;
    public static final int MRI_SHORT = 4;

    public FsMghHeader header;
    public FsMghData data;

    public FsMgh() {
        this.header = new FsMghHeader();
        this.data = new FsMghData(this.header);
    }

    public FsMgh(FsMghHeader header, FsMghData data) {
        this.header = header;
        this.data = data;
    }


    public static void validateMriDataType(int mriDataType) throws IOException {
        if(!(mriDataType == MRI_FLOAT || mriDataType == MRI_INT || mriDataType == MRI_SHORT || mriDataType == MRI_UCHAR)) {
            throw new IOException("Invalid MRI data type.");
        }
    }



    /**
     * Read a file in FreeSurfer mgh format and return a FsMgh object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsMgh object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     */
    public static FsMgh fromFsMghFile(Path filePath) throws IOException, FileNotFoundException {

        FsMgh mgh = new FsMgh();

        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read the header
        mgh.header = FsMghHeader.fromByteBuffer(buffer);
        mgh.data = FsMghData.fromByteBuffer(buffer, mgh.header);

        return mgh;

    }

    private static byte[] convertBytes(List<Byte> integers) {
        byte[] ret = new byte[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).byteValue();
        }
        return ret;
    }

    public static FsMgh fromFsMgzFile(Path filePath) throws IOException, FileNotFoundException {
      FileInputStream fis = new FileInputStream(filePath.toFile());
      GZIPInputStream gzis = new GZIPInputStream(fis);
      ArrayList<Byte> allBytes = new ArrayList<Byte>();
      byte[] buffer = new byte[65536];
      int numRead;
      while ((numRead = gzis.read(buffer)) != -1) {
        for(int i = 0; i < numRead; i++) {
            allBytes.add(buffer[i]);
        }
      }

      byte[] ab = convertBytes(allBytes);
      ByteBuffer bbuffer = ByteBuffer.wrap(ab);

      FsMgh mgh = new FsMgh();
      mgh.header = FsMghHeader.fromByteBuffer(bbuffer);
      mgh.data = FsMghData.fromByteBuffer(bbuffer, mgh.header);
      return mgh;
    }

    /**
     * Write this volume to a file in MGH format.
     * @param filePath the path to the file to write to
     * @param format the format to write in, must be "mgh".
     * @throws IOException
     */
    public void writeToFile(Path filePath, String format) throws IOException {
        format = format.toLowerCase();
        if (format.equals("mgh")) {
            System.err.println("FsMgh.writeToFile: Not implemented yet.");
        } else {
            throw new IOException(MessageFormat.format("Unknown FsMgh export format {0}.", format));
        }
    }

}

