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
     * Check whether a MZ3 file is gzipped.
     * @param filePath the path to the file
     * @return true if the file is gzipped, false otherwise
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if file not found.
     * @throws BufferUnderflowException if buffer underflow occurs, i.e., the file is too short to read the first header part required to determine the file format.
     */
    protected static Boolean mz3FileIsGzipped(Path filePath) throws IOException, FileNotFoundException, BufferUnderflowException {
        ByteBuffer buffer = IO.peakIntoFile(filePath, Boolean.TRUE, ByteOrder.LITTLE_ENDIAN, 16);
        Short magicNumber = buffer.getShort();
        return magicNumber.equals((short) 23117);
    }

}
