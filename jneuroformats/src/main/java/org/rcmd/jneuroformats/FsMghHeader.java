
package org.rcmd.jneuroformats;


import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.ByteBuffer;

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

    public int mri_datatype = FsMgh.MRI_FLOAT;
    public int dof = 0;
    public short rasGoodFlag = 0; // stored as signed int16 in file. 1 means that the RAS matrix is good, everything else means it is not.

    public float sizeX = 0.0f;
    public float sizeY = 0.0f;
    public float sizeZ = 0.0f;

    public List<Float> Mdc = new ArrayList<>();
    public List<Float> Pxyz_c = new ArrayList<>();

    public FsMghHeader() {
        Mdc = new ArrayList<>();
        Pxyz_c = new ArrayList<>();
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
        if(mghVersion != 1) {
            throw new IOException(MessageFormat.format("Invalid MGH format version in MGH file: expected 1, got {0}. File invalid.", mghVersion));
        }

        header.dim1size = buf.getInt();
        header.dim2size = buf.getInt();
        header.dim3size = buf.getInt();
        header.dim4size = buf.getInt();

        header.mri_datatype = buf.getInt();
        header.dof = buf.getInt();

        header.rasGoodFlag = buf.getShort();

        int unusedHeaderSpaceSizeLeft = 254;   // in bytes

        if(header.rasGoodFlag == 1) {
            header.sizeX =  buf.getFloat();
            header.sizeY =  buf.getFloat();
            header.sizeZ =  buf.getFloat();

            for(int i=0; i<9; i++) {
                header.Mdc.add(buf.getFloat());
            }
            for(int i=0; i<3; i++) {
                header.Pxyz_c.add(buf.getFloat());
            }
            unusedHeaderSpaceSizeLeft -= 60;
        }

        // Skip the rest of the unused header space and advance buffer to data part of file.
        // We do not seek (via buf.position()) because we want to be able to use this function also for gzip streams later.
        @SuppressWarnings("unused")
        byte unusedByte;
        while(unusedHeaderSpaceSizeLeft > 0) {
            unusedByte = buf.get();
            unusedHeaderSpaceSizeLeft--;
        }

        return header;
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