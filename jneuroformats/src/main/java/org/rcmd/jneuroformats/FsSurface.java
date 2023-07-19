/*
 *  Copyright 2023 Tim Schäfer
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.rcmd.jneuroformats.IO.IOUtil;

/**
 * Represents a FreeSurfer surface, i.e. a triangular mesh
 * consisting of a set of vertices and a set of faces.
 *
 * In the context of neuroimaging, meshes are typically used
 * to store brain surface reconstructions, e.g., the cortical
 * surface reconstructed from a T1-weighted MRI scan.
 */
public class FsSurface {

    public ArrayList<float[]> vertices;
    public ArrayList<int[]> faces;
    public String commentLine = "";
    public String createdLine = "";

    public FsSurface() {
        vertices = new ArrayList<float[]>();
        faces = new ArrayList<int[]>();
    }

    public FsSurface(ArrayList<float[]> vertices, ArrayList<int[]> faces) {
        this.vertices = vertices;
        this.faces = faces;
    }

    public void addVertex(float[] vertex) {
        vertices.add(vertex);
    }

    public void addFace(int[] face) {
        faces.add(face);
    }

    public float[] getVertex(int index) {
        return vertices.get(index);
    }

    public int[] getFace(int index) {
        return faces.get(index);
    }

    public int getNumberOfVertices() {
        return vertices.size();
    }

    public int getNumberOfFaces() {
        return faces.size();
    }

    public void setVertices(ArrayList<float[]> vertices) {
        this.vertices = vertices;
    }

    public void setFaces(ArrayList<int[]> faces) {
        this.faces = faces;
    }

    public ArrayList<float[]> getVertices() {
        return vertices;
    }

    public ArrayList<int[]> getFaces() {
        return faces;
    }

    /**
     * Generate a cube with side length 1 and centered at the origin.
     *
     * Used mainly for testing purposes.
     *
     * @return a cube with side length 1 and centered at the origin.
     */
    public static FsSurface generateCube() {
        FsSurface cube = new FsSurface();
        float[][] vertices = {
                { 0, 0, 0 },
                { 1, 0, 0 },
                { 1, 1, 0 },
                { 0, 1, 0 },
                { 0, 0, 1 },
                { 1, 0, 1 },
                { 1, 1, 1 },
                { 0, 1, 1 }
        };
        int[][] faces = {
                { 0, 1, 2 },
                { 0, 2, 3 },
                { 0, 1, 4 },
                { 1, 4, 5 },
                { 1, 2, 5 },
                { 2, 5, 6 },
                { 2, 3, 6 },
                { 3, 6, 7 },
                { 3, 0, 7 },
                { 0, 4, 7 },
                { 4, 5, 6 },
                { 4, 6, 7 }
        };
        for (float[] vertex : vertices) {
            cube.addVertex(vertex);
        }
        for (int[] face : faces) {
            cube.addFace(face);
        }
        return cube;
    }

    /**
     * Read a file in FreeSurfer surface format and return a FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     */
    public static FsSurface fromFsSurfaceFile(Path filePath) throws IOException {

        FsSurface surface = new FsSurface();

        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Read the header
        int magicNumberPart1 = IOUtil.getUint8(buffer);
        int magicNumberPart2 = IOUtil.getUint8(buffer);
        int magicNumberPart3 = IOUtil.getUint8(buffer);

        if (magicNumberPart1 != 255 || magicNumberPart2 != 255 || magicNumberPart3 != 254) {
            throw new IOException(MessageFormat.format("Invalid magic number in FreeSurfer surface file: magic codes {0} {1} {2}, expected 255 255 254. File invalid.",
                    magicNumberPart1, magicNumberPart2, magicNumberPart3));
        }

        surface.createdLine = IOUtil.readNewlineTerminatedString(buffer);
        surface.commentLine = IOUtil.readNewlineTerminatedString(buffer);

        int numberOfVertices = buffer.getInt();
        int numberOfFaces = buffer.getInt();

        //System.out.println(MessageFormat.format("CreatedLine is: {0}", unusedCreatedLine));
        //System.out.println(MessageFormat.format("CommentLine is: {0}", unusedCommentLine));
        //System.out.println(MessageFormat.format("Reading surface with {0} vertices and {1} faces.", numberOfVertices, numberOfFaces));

        for (int i = 0; i < numberOfVertices; i++) {
            float[] vertex = new float[3];
            vertex[0] = buffer.getFloat();
            vertex[1] = buffer.getFloat();
            vertex[2] = buffer.getFloat();
            surface.addVertex(vertex);
        }

        for (int i = 0; i < numberOfFaces; i++) {
            int[] face = new int[3];
            face[0] = buffer.getInt();
            face[1] = buffer.getInt();
            face[2] = buffer.getInt();
            surface.addFace(face);
        }

        return surface;
    }

    /**
     * Generate string representation of this mesh in PLY format.
     * @return the PLY format string
     */
    public String toPlyFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("ply\n");
        builder.append("format ascii 1.0\n");
        builder.append("comment Created by jneuroformats\n");
        builder.append("element vertex " + getNumberOfVertices() + "\n");
        builder.append("property float x\n");
        builder.append("property float y\n");
        builder.append("property float z\n");
        builder.append("element face " + getNumberOfFaces() + "\n");
        builder.append("property list uchar int vertex_indices\n");
        builder.append("end_header\n");
        for (float[] vertex : vertices) {
            builder.append(vertex[0] + " " + vertex[1] + " " + vertex[2] + "\n");
        }
        for (int[] face : faces) {
            builder.append("3 " + face[0] + " " + face[1] + " " + face[2] + "\n");
        }
        return builder.toString();
    }


    /**
     * Generate string representation of this mesh in Wavefront Object (OBJ) format.
     * @return the OBJ format string
     */
    public String toObjFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("o mesh\n");
        for (float[] vertex : vertices) {
            builder.append("v " + vertex[0] + " " + vertex[1] + " " + vertex[2] + "\n");
        }
        for (int[] face : faces) {
            builder.append("f " + face[0] + " " + face[1] + " " + face[2] + "\n");
        }
        return builder.toString();
    }


    /**
     * Write this mesh to a file in PLY or OBJ format.
     * @param filePath the path to the file to write to
     * @param format the format to write to, either "ply" or "obj"
     * @throws IOException
     */
    public void writeToFile(Path filePath, String format) throws IOException {
        if (format.equals("ply")) {
            Files.write(filePath, toPlyFormat().getBytes());
        } else if (format.equals("obj")) {
            Files.write(filePath, toObjFormat().getBytes());
        } else {
            throw new IOException(MessageFormat.format("Unknown mesh export format {0}.", format));
        }
    }

}
