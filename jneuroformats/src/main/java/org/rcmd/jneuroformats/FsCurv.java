package org.rcmd.jneuroformats;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;


import org.rcmd.jneuroformats.IO.IOUtil;


public class FsCurv {

    // The per-vertex data.
    public List<Float> data;

    /**
     * Metadata, The number of faces in the surface this data belongs to. Typically not relevant, set to 0 if unknown.
     */
    public Integer numberOfFaces = 0;

    /** Leave this at 1, or terrible things will happen. */
    private Integer numberOfValuesPerVertex = 1;

    // Constructor
    public FsCurv(List<Float> data) {
        this.data = data;
    }

    // Constructor
    public FsCurv() {
        this.data = new ArrayList<>();
    }

    /**
     * Read a file in FreeSurfer curv format and return an FsCurv object.
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


        curv.numberOfValuesPerVertex = buffer.getInt();
        if(curv.numberOfValuesPerVertex != 1) {
            throw new IOException(MessageFormat.format("Invalid number of values per vertex in FreeSurfer curv file: {0}, expected 1. File invalid.",
                    curv.numberOfValuesPerVertex));
        }

        curv.data = new ArrayList<Float>(numberOfVertices);

        for (int i = 0; i < numberOfVertices; i++) {
            curv.data.add(buffer.getFloat());
        }

        return curv;
    }

    /**
     * Generate string representation of this mesh in CSV format.
     * @param with_header whether to include a header row at the top of the CSV.
     * @param with_index whether to include a vertex index column.
     * @return the CSV format string
     */
    public String toCsvFormat(Boolean with_header, Boolean with_index) {
        StringBuilder builder = new StringBuilder();
        if(with_header) {
            if(with_index) {
                builder.append("vertindex,value\n");
            } else {
                builder.append("value\n");
            }
        }

        if(with_index) {
            int vidx = 0;
            for (float value : this.data) {
                builder.append(vidx + "," + value + "\n");
                vidx++;
            }
        } else {
            for (float value : this.data) {
                builder.append(value + "\n");
            }
        }
        return builder.toString();
    }

    /**
     * Write this FsCurv to a file in FreeSurfer curv format.
     * @param filePath the path to the file to write to
     * @throws IOException
     */
    private void writeCurv(Path filePath) throws IOException {
        ByteBuffer buf = writeFsCurvToByteBuffer();
        WritableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.WRITE);
        channel.write(buf);
        channel.close();
    }

    /**
     * Write this mesh to a ByteBuffer in FreeSurfer surface format.
     * @note This method is used internally by writeSurface(Path filePath).
     * @throws IOException
     */
    private ByteBuffer writeFsCurvToByteBuffer() throws IOException {

        ByteBuffer buf = ByteBuffer.allocate(8192);

        // write magic bytes
        buf.put((byte)255);
        buf.put((byte)255);
        buf.put((byte)255);

        // write number of vertices
        buf.putInt(this.data.size());

        // write number of faces
        buf.putInt(this.numberOfFaces);
        buf.putInt(this.numberOfValuesPerVertex);

        // write data
        for (float value : this.data) {
            buf.putFloat(value);
        }

        buf.flip();
        return buf;
    }


    /**
     * Write this FsCurv to a file in curv or CSV format.
     * @param filePath the path to the file to write to
     * @param format the format to write to, either "curv" or "csv".
     * @throws IOException
     */
    public void writeToFile(Path filePath, String format) throws IOException {
        format = format.toLowerCase();
        if (format.equals("csv")) {
            Files.write(filePath, toCsvFormat(Boolean.TRUE, Boolean.TRUE).getBytes());
        } else if (format.equals("curv")) {
            this.writeCurv(filePath);
        } else {
            throw new IOException(MessageFormat.format("Unknown FsCurv export format {0}.", format));
        }
    }

}
