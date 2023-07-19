package org.rcmd.jneuroformats;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import org.rcmd.jneuroformats.IO.IOUtil;


public class FsCurv {

    // The per-vertex data.
    public ArrayList<Float> data;
    public Integer numberOfFaces;

    /**
     * Read a file in FreeSurfer curv format and return a FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsCurv object.
     * @throws IOException if IO error occurs.
     */
    public static FsCurv fromFsCurvFile(Path filePath) throws IOException {

        FsCurv curv = new FsCurv();

        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read the header
        int magicNumberPart1 = IOUtil.getUint8(buffer);
        int magicNumberPart2 = IOUtil.getUint8(buffer);
        int magicNumberPart3 = IOUtil.getUint8(buffer);

        if (magicNumberPart1 != 255 || magicNumberPart2 != 255 || magicNumberPart3 != 255) {
            throw new IOException(MessageFormat.format("Invalid magic number in FreeSurfer curv file: magic codes {0} {1} {2}, expected 255 255 255. File invalid.",
                    magicNumberPart1, magicNumberPart2, magicNumberPart3));
        }

        int numberOfVertices = buffer.getInt();
        curv.numberOfFaces = buffer.getInt();


        int numberOfValuesPerVertex = buffer.getInt();
        if(numberOfValuesPerVertex != 1) {
            throw new IOException(MessageFormat.format("Invalid number of values per vertex in FreeSurfer curv file: {0}, expected 1. File invalid.",
                    numberOfValuesPerVertex));
        }

        curv.data = new ArrayList<Float>(numberOfVertices);

        for (int i = 0; i < numberOfVertices; i++) {
            curv.data.add(buffer.getFloat());
        }

        return curv;
    }

}
