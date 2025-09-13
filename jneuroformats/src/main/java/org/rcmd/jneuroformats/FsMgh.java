/*
 *  Copyright 2021 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.rcmd.jneuroformats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Models a FreeSurfer volume.
 *
 * The volume is defined by a header (FsMghHeader) and the actual data (FsMghData). The data is stored in a 4-dimensional array.
 */
public class FsMgh {

    /** MRI data type constants. MRI_UCHAR is a unsigned char (8 bits). */
    public static final int MRI_UCHAR = 0;

    /** MRI data type constants. MRI_INT is a signed int (32 bits). */
    public static final int MRI_INT = 1;

    /** MRI data type constants. MRI_FLOAT is a float (32 bits). */
    public static final int MRI_FLOAT = 3;

    /** MRI data type constants. MRI_SHORT is a signed short (16 bits). */
    public static final int MRI_SHORT = 4;

    /** The FsMghHeader instance stores the header information for the volume. */
    public FsMghHeader header;

    /** The FsMghData instance stores the actual volume data. */
    public FsMghData data;

    /**
     * Default constructor for FsMgh. Initializes an empty header and data object.
     */
    public FsMgh() {
        this.header = new FsMghHeader();
        this.data = new FsMghData(this.header);
    }

    /**
     * Constructor for FsMgh with header and data.
     * @param header the FsMghHeader object.
     * @param data the FsMghData object.
     */
    public FsMgh(FsMghHeader header, FsMghData data) {
        this.header = header;
        this.data = data;
    }

    /**
     * Validate the MRI data type.
     * @param mriDataType the MRI data type to validate.
     * @throws IOException if the MRI data type is invalid.
     */
    protected static void validateMriDataType(int mriDataType) throws IOException {
        if (!(mriDataType == MRI_FLOAT || mriDataType == MRI_INT || mriDataType == MRI_SHORT || mriDataType == MRI_UCHAR)) {
            throw new IOException("Invalid MRI data type.");
        }
    }

    /**
     * Enum for the different volume file types.
     */
    public enum VolumeFileType {
        /** FreeSurfer MGH file format. */
        MGH,
        /** FreeSurfer MGZ file format, a zipped version of MGH. */
        MGZ
    }

    /**
     * Determine the volume file format from the file extension.
     * @param filePath the path to the file.
     * @return the volume file type.
     * @throws IOException if the file extension is not recognized.
     */
    protected static VolumeFileType volumeFileFormatFromFileExtension(Path filePath) throws IOException {
        String fileNameLower = filePath.getFileName().toString().toLowerCase();
        if (fileNameLower.endsWith(".mgh")) {
            return VolumeFileType.MGH;
        }
        else if (fileNameLower.endsWith(".mgz")) {
            return VolumeFileType.MGZ;
        }
        else {
            throw new IOException(
                    MessageFormat.format("Cannot determine volume file format for file {0} from name: unknown file extension.", filePath.getFileName().toString()));
        }
    }

    /**
     * Determine the volume file format from the file extension or from the specified format.
     * @param filePath the path to the file.
     * @param format the format to use if specified.
     * @return the volume file type.
     * @throws IOException if the file extension is not recognized.
     */
    protected static VolumeFileType getVolumeFileFormat(Path filePath, String format) throws IOException {
        String formatLower = format.toLowerCase();
        if (formatLower.equals("auto")) {
            return volumeFileFormatFromFileExtension(filePath);
        }
        else {

            if (format.equals("mgh")) {
                return VolumeFileType.MGH;
            }
            else if (format.equals("mgz")) {
                return VolumeFileType.MGZ;
            }
            else {
                throw new IOException(MessageFormat.format("Unknown volume file format {0}.", format));
            }
        }
    }

    /**
     * Read a file in FreeSurfer MGH format or MGZ format and return a FsMgh object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.mgz")`. The file format will be determined from the file extension.
     * @return an FsMgh object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #readFormat(Path, String) if you want to read a file and specify the format.
     */
    public static FsMgh read(Path filePath) throws IOException, FileNotFoundException {
        return FsMgh.readFormat(filePath, "auto");
    }

    /**
     * Read a file in FreeSurfer MGH format or MGZ format and return a FsMgh object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.mgz")`. The file format will be determined from the file extension if parameter `format` is set to `auto`.
     * @param format the file format to read, either "mgh", "mgz", or "auto" to auto-detect from the file name.
     * @return an FsMgh object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #read(Path) if you want to read a file without specifying the format.
     */
    public static FsMgh readFormat(Path filePath, String format) throws IOException, FileNotFoundException {
        VolumeFileType vol = getVolumeFileFormat(filePath, format);

        if (vol.equals(VolumeFileType.MGH)) {
            return fromFsMghFile(filePath);
        }
        else if (vol.equals(VolumeFileType.MGZ)) {
            return fromFsMgzFile(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unknown volume file format {0}.", vol.toString()));
        }
    }

    /**
     * Read a file in FreeSurfer mgh format and return a FsMgh object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsMgh object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     */
    protected static FsMgh fromFsMghFile(Path filePath) throws IOException, FileNotFoundException {

        FsMgh mgh = new FsMgh();

        FileInputStream fis = new FileInputStream(filePath.toFile());
        ArrayList<Byte> allBytes = new ArrayList<Byte>();
        byte[] readBuffer = new byte[65536];
        int numRead;
        while ((numRead = fis.read(readBuffer)) != -1) {
            for (int i = 0; i < numRead; i++) {
                allBytes.add(readBuffer[i]);
            }
        }

        byte[] ab = IO.convertBytes(allBytes);
        ByteBuffer bbuffer = ByteBuffer.wrap(ab);

        // Read the header
        mgh.header = FsMghHeader.fromByteBuffer(bbuffer);
        mgh.data = FsMghData.fromByteBuffer(bbuffer, mgh.header);

        return mgh;

    }

    /**
     * Read a file in FreeSurfer MGH format and return a FsMgh object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsMgh object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     */
    protected static FsMgh fromFsMgzFile(Path filePath) throws IOException, FileNotFoundException {
        FileInputStream fis = new FileInputStream(filePath.toFile());
        GZIPInputStream gzis = new GZIPInputStream(fis);
        ArrayList<Byte> allBytes = new ArrayList<Byte>();
        byte[] buffer = new byte[65536];
        int numRead;
        while ((numRead = gzis.read(buffer)) != -1) {
            for (int i = 0; i < numRead; i++) {
                allBytes.add(buffer[i]);
            }
        }

        byte[] ab = IO.convertBytes(allBytes);
        ByteBuffer bbuffer = ByteBuffer.wrap(ab);

        FsMgh mgh = new FsMgh();
        mgh.header = FsMghHeader.fromByteBuffer(bbuffer);
        mgh.data = FsMghData.fromByteBuffer(bbuffer, mgh.header);
        gzis.close();
        fis.close();
        return mgh;
    }

    /**
     * Write this mesh to a file in FreeSurfer MGH format.
     * @param filePath the path to the file to write to
     * @throws IOException if IO error occurs.
     */
    protected void writeToMghFile(Path filePath) throws IOException {
        ByteBuffer buf = this.writeFsMghToByteBuffer();
        IO.writeFile(filePath, buf);
    }

    /**
     * Write this mesh to a file in FreeSurfer MGZ format.
     * @param filePath the path to the file to write to
     * @throws IOException if IO error occurs.
     */
    protected void writeToMgzFile(Path filePath) throws IOException {
        ByteBuffer buf = this.writeFsMghToByteBuffer();
        IO.writeGzipFile(filePath, buf);
    }

    /**
     * Write this mesh to a ByteBuffer in FreeSurfer surface format.
     * This method is used internally by writeSurface(Path filePath).
     * @return a ByteBuffer containing the MGH data.
     * @throws IOException if IO error occurs.
     */
    protected ByteBuffer writeFsMghToByteBuffer() throws IOException {

        ByteBuffer buf = ByteBuffer.allocate(this.header.getHeaderSizeInBytes() + this.header.getDataSizeInBytes() + 1000);
        buf = this.header.writeFsMghHeaderToByteBuffer(buf);
        buf = this.data.writeFsMghDataToByteBuffer(buf, this.header);

        return buf;
    }

    /**
     * Write this volume to a file in MGH format.
     * @param filePath the path to the file to write to
     * @param format the format to write in, must be "mgh" or "mgz".
     * @throws IOException if IO error occurs.
     */
    public void write(Path filePath, String format) throws IOException {
        format = format.toLowerCase();
        if (format.equals("mgh")) {
            this.writeToMghFile(filePath);
        }
        else if (format.equals("mgz")) {
            this.writeToMgzFile(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unknown FsMgh export format {0}.", format));
        }
    }

}
