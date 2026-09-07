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

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a MZ3 surface file.
 */
public class Mz3 {

    /** The mesh contained in the MZ3 file. */
    public Mesh mesh;

    /** Per-vertex data, one value per vertex. */
    public List<Float> perVertexData;

    /** Per-vertex colors, one color per vertex. */
    public List<Color> vertexColors;

    /**
     * Default constructor for Mz3. Initializes empty mesh, perVertexData and vertexColors.
     */
    public Mz3() {
        this.mesh = new Mesh();
        this.perVertexData = new ArrayList<>();
        this.vertexColors = new ArrayList<>();
    }

    /**
     * Constructor for Mz3 with data.
     * @param mesh the mesh contained in the MZ3 file.
     * @param perVertexData per-vertex data, one value per vertex.
     * @param vertexColors per-vertex colors, one color per vertex.
     */
    public Mz3(Mesh mesh, List<Float> perVertexData, List<Color> vertexColors) {
        this.mesh = mesh;
        this.perVertexData = perVertexData;
        this.vertexColors = vertexColors;
    }

    /**
     * Read a file in MZ3 surface format and return an FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.mz3")`.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if file not found.
     * @throws BufferUnderflowException if buffer underflow occurs, i.e., the file is too short to read the first header part required to determine the file format.
     */
    public static Mz3 fromMz3File(Path filePath) throws IOException, FileNotFoundException, BufferUnderflowException {

        Mesh mesh = new Mesh();

        Boolean is_gzip = mz3FileIsGzipped(filePath);
        ByteBuffer buf = IO.readAllFileBytesGzipOrNot(filePath, is_gzip, ByteOrder.LITTLE_ENDIAN);

        Short magicNumber = buf.getShort();
        if (!magicNumber.equals((short) 23117)) {
            throw new IOException(MessageFormat.format("Invalid magic number in MZ3 mesh file: magic code {0}, expected 23117. File invalid.",
                    magicNumber));
        }

        Short attr = buf.getShort();
        Integer numFaces = buf.getInt();
        Integer numVertices = buf.getInt();
        Integer numSkip = buf.getInt();

        Boolean is_face = (attr & 1L) != 0L;
        Boolean is_vert = (attr & 2L) != 0L;
        Boolean is_rgba = (attr & 4L) != 0L;
        Boolean is_scalar = (attr & 8L) != 0L;

        if (attr > 15L) {
            throw new IOException(MessageFormat.format("Unsupported MZ3 surface file version detected in file {0}.", filePath.toString()));
        }

        if (numSkip > 0) {
            buf.position(buf.position() + numSkip);
        }

        if (is_face) {
            for (int i = 0; i < numFaces; i++) {
                int[] face = new int[3];
                face[0] = buf.getInt();
                face[1] = buf.getInt();
                face[2] = buf.getInt();
                mesh.addFace(face);
            }
        }

        if (is_vert) {
            for (int i = 0; i < numVertices; i++) {
                float[] vertex = new float[3];
                vertex[0] = buf.getFloat();
                vertex[1] = buf.getFloat();
                vertex[2] = buf.getFloat();
                mesh.addVertex(vertex);
            }
        }

        List<Color> vertexColors = new ArrayList<>(numVertices);
        if (is_rgba) {
            for (int i = 0; i < numVertices; i++) {
                int[] rgba = new int[4];
                rgba[0] = buf.getInt();
                rgba[1] = buf.getInt();
                rgba[2] = buf.getInt();
                rgba[3] = buf.getInt();
                vertexColors.add(new Color(rgba[0], rgba[1], rgba[2], rgba[3]));
            }
        }

        List<Float> perVertexData = new ArrayList<>(numVertices);
        if (is_scalar) {
            for (int i = 0; i < numVertices; i++) {
                perVertexData.add(buf.getFloat());
            }
        }

        Mz3 mz3 = new Mz3();
        mz3.mesh = mesh;
        mz3.perVertexData = perVertexData;
        mz3.vertexColors = vertexColors;

        return mz3;
    }

    /**
     * Write this Mz3 object to a file in MZ3 surface format (not gzipped).
     * @param filePath the path to the file to write to
     * @throws IOException if IO error occurs.
     */
    public void write(Path filePath) throws IOException {
        IO.writeFile(filePath, toMz3ByteBuffer());
    }

    /**
     * Write this Mz3 object to a ByteBuffer in MZ3 surface format.
     * @return a ByteBuffer containing the MZ3 data.
     * @throws IOException if IO error occurs.
     */
    protected ByteBuffer toMz3ByteBuffer() throws IOException {

        int numFaces = this.mesh.getNumberOfFaces();
        int numVertices = this.mesh.getNumberOfVertices();

        Boolean is_face = numFaces > 0;
        Boolean is_vert = numVertices > 0;
        Boolean is_rgba = this.vertexColors != null && this.vertexColors.size() > 0;
        Boolean is_scalar = this.perVertexData != null && this.perVertexData.size() > 0;

        if (is_rgba && this.vertexColors.size() != numVertices) {
            throw new IOException(MessageFormat.format(
                    "Number of vertex colors ({0}) does not match the number of vertices ({1}).",
                    this.vertexColors.size(), numVertices));
        }
        if (is_scalar && this.perVertexData.size() != numVertices) {
            throw new IOException(MessageFormat.format(
                    "Number of per-vertex data values ({0}) does not match the number of vertices ({1}).",
                    this.perVertexData.size(), numVertices));
        }

        int attr = 0;
        if (is_face) {
            attr |= 1;
        }
        if (is_vert) {
            attr |= 2;
        }
        if (is_rgba) {
            attr |= 4;
        }
        if (is_scalar) {
            attr |= 8;
        }

        int bufferSize = 2 + 2 + 4 + 4 + 4; // magic, attr, numFaces, numVertices, numSkip
        if (is_face) {
            bufferSize += numFaces * 3 * 4;
        }
        if (is_vert) {
            bufferSize += numVertices * 3 * 4;
        }
        if (is_rgba) {
            bufferSize += numVertices * 4 * 4;
        }
        if (is_scalar) {
            bufferSize += numVertices * 4;
        }

        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // Write the header.
        buf.putShort((short) 23117); // magic number
        buf.putShort((short) attr);
        buf.putInt(numFaces);
        buf.putInt(numVertices);
        buf.putInt(0); // numSkip

        if (is_face) {
            for (int[] face : this.mesh.faces) {
                buf.putInt(face[0]);
                buf.putInt(face[1]);
                buf.putInt(face[2]);
            }
        }

        if (is_vert) {
            for (float[] vertex : this.mesh.vertices) {
                buf.putFloat(vertex[0]);
                buf.putFloat(vertex[1]);
                buf.putFloat(vertex[2]);
            }
        }

        if (is_rgba) {
            for (Color vertexColor : this.vertexColors) {
                buf.putInt(vertexColor.getRed());
                buf.putInt(vertexColor.getGreen());
                buf.putInt(vertexColor.getBlue());
                buf.putInt(vertexColor.getAlpha());
            }
        }

        if (is_scalar) {
            for (float value : this.perVertexData) {
                buf.putFloat(value);
            }
        }

        buf.flip();
        return buf;
    }

    /**
     * Check whether a MZ3 file is gzipped.
     * @param filePath the path to the file
     * @return true if the file is gzipped, false otherwise
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if file not found.
     * @throws BufferUnderflowException if buffer underflow occurs, i.e., the file is too short to read the first header part required to determine the file format.
     */
    protected static Boolean mz3FileIsGzipped(Path filePath) throws IOException, FileNotFoundException, BufferUnderflowException {
        // Peek at the raw bytes (without decompressing): a gzipped MZ3 file starts with the gzip magic bytes 0x1F 0x8B.
        ByteBuffer buffer = IO.peakIntoFile(filePath, Boolean.FALSE, ByteOrder.LITTLE_ENDIAN, 16);
        Short magicNumber = buffer.getShort();
        return magicNumber.equals((short) 35615); // 0x8B1F in little endian, i.e., the gzip magic bytes 0x1F 0x8B.
    }

}
