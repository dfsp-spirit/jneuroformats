

package org.rcmd.jneuroformats;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Files;

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
        System.err.println("fromFsMghFile: Not implemented yet.");
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

