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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a FreeSurfer curv file, or a CSV file with per-vertex data for a surface (mesh)
 */
public class FsCurv {

    /** The per-vertex data, one value per vertex. */
    public List<Float> data;

    /**
     * Metadata, The number of faces in the surface this data belongs to. Typically not relevant, set to 0 if unknown.
     */
    public Integer numberOfFaces = 0;

    /** Leave this at 1, or terrible things will happen. It exists in the header, but I have never seen a FreeSurfer file with more than one value per vertex, and you can be sure that various software and scripts will not handle it properly. */
    private Integer numberOfValuesPerVertex = 1;

    /**
     * Constructor for FsCurv with data.
     */
    public FsCurv(List<Float> data) {
        this.data = data;
    }

    /**
     * Default constructor for FsCurv. Initializes an empty list for the data.
     */
    public FsCurv() {
        this.data = new ArrayList<>();
    }

    /**
     * Read a file in CSV format and return an FsCurv object.
     *
     * The file can have one of two formats: either one value per line, or two values per line, separated by a comma. In the latter case, the first value is ignored.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @param has_header whether the file has a header row at the top.
     * @return an FsCurv object.
     * @throws IOException if IO error occurs.
     */
    protected static FsCurv fromCsvFile(Path filePath, Boolean has_header) throws IOException {
        FsCurv curv = new FsCurv();
        List<String> lines = Files.readAllLines(filePath);
        if (has_header) {
            lines.remove(0);
        }
        for (String line : lines) {
            String[] parts = line.trim().split(",");
            if (parts.length == 1) {
                curv.data.add(Float.parseFloat(parts[0]));
            }
            else if (parts.length == 2) {
                curv.data.add(Float.parseFloat(parts[1]));
            }
            else {
                throw new IOException(MessageFormat.format("Invalid CSV file format: {0}.", filePath.toString()));
            }
        }
        return curv;
    }

    /**
     * Read a file in FreeSurfer curv or CSV format and return an FsCurv object.
     *
     * The file format is determined by the file extension. If the file extension is ".csv", the file is read as CSV, otherwise as FreeSurfer curv.
     *
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`. For csv files, the file can have one of two formats: either one value per line, or two values per line (index and per-vertex value), separated by a comma. In the latter case, the first value (index) is ignored.
     * @return an FsCurv object.
     * @throws IOException if IO error occurs.
     */
    public static FsCurv read(Path filePath) throws IOException {
        if (filePath.toString().toLowerCase().endsWith(".csv")) {
            return FsCurv.fromCsvFile(filePath, Boolean.FALSE);
        }
        else {
            return FsCurv.fromFsCurvFile(filePath);
        }
    }

    /**
     * Read a file in FreeSurfer curv format and return an FsCurv object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsCurv object.
     * @throws IOException if IO error occurs.
     */
    protected static FsCurv fromFsCurvFile(Path filePath) throws IOException {

        FsCurv curv = new FsCurv();

        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read the header
        int magicNumberPart1 = IO.getUint8(buffer);
        int magicNumberPart2 = IO.getUint8(buffer);
        int magicNumberPart3 = IO.getUint8(buffer);

        if (magicNumberPart1 != 255 || magicNumberPart2 != 255 || magicNumberPart3 != 255) {
            throw new IOException(MessageFormat.format("Invalid magic number in FreeSurfer curv file: magic codes {0} {1} {2}, expected 255 255 255. File '{3}' invalid.",
                    magicNumberPart1, magicNumberPart2, magicNumberPart3, filePath.toString()));
        }

        int numberOfVertices = buffer.getInt();
        curv.numberOfFaces = buffer.getInt();

        curv.numberOfValuesPerVertex = buffer.getInt();
        if (curv.numberOfValuesPerVertex != 1) {
            throw new IOException(MessageFormat.format("Invalid number of values per vertex in FreeSurfer curv file: {0}, expected 1. File '{1}' invalid.",
                    curv.numberOfValuesPerVertex, filePath.toString()));
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
        if (with_header) {
            if (with_index) {
                builder.append("vertindex,value\n");
            }
            else {
                builder.append("value\n");
            }
        }

        if (with_index) {
            int vidx = 0;
            for (float value : this.data) {
                builder.append(vidx + "," + value + "\n");
                vidx++;
            }
        }
        else {
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
        buf.put((byte) 255);
        buf.put((byte) 255);
        buf.put((byte) 255);

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
    public void write(Path filePath, String format) throws IOException {
        format = format.toLowerCase();
        if (format.equals("csv")) {
            Files.write(filePath, toCsvFormat(Boolean.FALSE, Boolean.FALSE).getBytes());
        }
        else if (format.equals("curv")) {
            this.writeCurv(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unknown FsCurv export format {0}.", format));
        }
    }

}
