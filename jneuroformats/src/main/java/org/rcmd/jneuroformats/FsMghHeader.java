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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the structure, and a value that is assigned to all vertices or voxels that are part of the structure. Sometimes the value is not needed, and can be set to 0.
 */
public class FsMghHeader {

    public int dim1size = 0;
    public int dim2size = 0;
    public int dim3size = 0;
    public int dim4size = 0;

    public int mriDatatype = FsMgh.MRI_FLOAT;
    public int dof = 0;
    public short rasGoodFlag = 0; // stored as signed int16 in file. 1 means that the RAS matrix and info (size x/y/z, Mdc, Pxyz_c) is good, everything else means it is not.

    public float sizeX = 0.0f;
    public float sizeY = 0.0f;
    public float sizeZ = 0.0f;

    /**
     * The RAS matrix. 3x3 matrix, stored as 9 float values in the file. The matrix is stored in row-major order, i.e. the first 3 values are the first row, the next 3 values are the second row, and the last 3 values are the third row.
     */
    public List<Float> Mdc = new ArrayList<>();

    /**
     * The RAS origin. Sometimes referred to as 'Point XYZ center' or 'Pxyz_c'. 3 float values, stored in the file as 3 float values.
     */
    public List<Float> Pxyz_c = new ArrayList<>();

    public FsMghHeader() {
        Mdc = new ArrayList<>();
        Pxyz_c = new ArrayList<>();
    }

    /**
     * Get the number of values in the data part of the file.
     * @return the number of values in the data part of the file.
     */
    public int getNumValues() {
        return this.dim1size * this.dim2size * this.dim3size * this.dim4size;
    }

    /**
     * Read an FsMghHeader instance from a Buffer in FreeSurfer MGH format. Reads the header and advances the buffer to the data part of the file.
     * @param buf the buffer to read from.
     * @return an FsMghHeader instance.
     * @throws IOException if IO error occurs, or if the file is not in valid MGH format.
     */
    public static FsMghHeader fromByteBuffer(ByteBuffer buf) throws IOException {
        FsMghHeader header = new FsMghHeader();

        int mghVersion = buf.getInt();
        if (mghVersion != 1) {
            throw new IOException(MessageFormat.format("Invalid MGH format version in MGH file: expected 1, got {0}. File invalid.", mghVersion));
        }

        header.dim1size = buf.getInt();
        header.dim2size = buf.getInt();
        header.dim3size = buf.getInt();
        header.dim4size = buf.getInt();

        header.mriDatatype = buf.getInt();
        header.dof = buf.getInt();

        header.rasGoodFlag = buf.getShort();

        int unusedHeaderSpaceSizeLeft = 254; // in bytes

        Short validRasGoodFlagValue = 1;

        if (header.rasGoodFlag == validRasGoodFlagValue) {
            header.sizeX = buf.getFloat();
            header.sizeY = buf.getFloat();
            header.sizeZ = buf.getFloat();

            for (int i = 0; i < 9; i++) {
                header.Mdc.add(buf.getFloat());
            }
            for (int i = 0; i < 3; i++) {
                header.Pxyz_c.add(buf.getFloat());
            }
            unusedHeaderSpaceSizeLeft -= 60;
        }

        // Skip the rest of the unused header space and advance buffer to data part of file.
        // We do not seek (via buf.position()) because we want to be able to use this function also for gzip streams later.
        @SuppressWarnings("unused")
        byte unusedByte;
        while (unusedHeaderSpaceSizeLeft > 0) {
            unusedByte = buf.get();
            unusedHeaderSpaceSizeLeft--;
        }

        return header;
    }

    /**
     * Get the size of the header part of the file, in bytes.
     * @return the size of the header part of the file, in bytes. This is always the same for the MGH format, which uses a fixed size header.
     */
    public int getHeaderSizeInBytes() {
        return 284;
    }

    /**
     * Get the size of the data part of the file, in bytes. Data size is computed based on header information: data type and number of values.
     * @return the size of the data part of the file, in bytes.
     */
    public int getDataSizeInBytes() {
        return this.getNumValues() * this.getNumBytesPerValue();
    }

    private int getNumBytesPerValue() {
        int numBytesPerValue = 4;
        if (this.mriDatatype == FsMgh.MRI_FLOAT) {
            numBytesPerValue = 4;
        }
        else if (this.mriDatatype == FsMgh.MRI_INT) {
            numBytesPerValue = 4;
        }
        else if (this.mriDatatype == FsMgh.MRI_SHORT) {
            numBytesPerValue = 2;
        }
        else if (this.mriDatatype == FsMgh.MRI_UCHAR) {
            numBytesPerValue = 1;
        }
        return numBytesPerValue;
    }

    /**
     * Write the FsMghHeader to a ByteBuffer.
     * @param buf an existing ByteBuffer to write to. If null, a new ByteBuffer will be created.
     * @return the ByteBuffer, with the FsMghHeader written to it.
     * @throws IOException if IO error occurs.
     */
    protected ByteBuffer writeFsMghHeaderToByteBuffer(ByteBuffer buf) throws IOException {
        if (buf == null) {
            buf = ByteBuffer.allocate(this.getHeaderSizeInBytes() + this.getDataSizeInBytes()+ 1000);
        }

        int mghVersionNumber = 1;
        buf.putInt(mghVersionNumber);

        buf.putInt(this.dim1size);
        buf.putInt(this.dim2size);
        buf.putInt(this.dim3size);
        buf.putInt(this.dim4size);

        buf.putInt(this.mriDatatype);
        buf.putInt(this.dof);
        buf.putShort(rasGoodFlag);

        Short validRasGoodFlagValue = 1;

        if (this.rasGoodFlag == validRasGoodFlagValue) {
            buf.putFloat(this.sizeX);
            buf.putFloat(this.sizeY);
            buf.putFloat(this.sizeZ);

            for (int i = 0; i < 9; i++) {
                buf.putFloat(this.Mdc.get(i));
            }

            for (int i = 0; i < 3; i++) {
                buf.putFloat(this.Pxyz_c.get(i));
            }

        }
        else {
            for (int i = 0; i < 60; i++) {
                buf.put((byte) 0);
            }
        }

        // fill rest of the reserved header space with zeros
        for (int i = 0; i < 194; i++) {
            buf.put((byte) 0);
        }

        return buf;
    }

    /**
     * Read an FsMghHeader instance from a file.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsMghHeader instance.
     * @throws IOException if IO error occurs, or if the file is not in valid MGH format.
     * @throws FileNotFoundException if the file does not exist.
     */
    public static FsMghHeader fromFsMghFile(Path filePath) throws IOException, FileNotFoundException {
        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return FsMghHeader.fromByteBuffer(buffer);
    }

}
