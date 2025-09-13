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
import java.lang.IllegalArgumentException;
import java.nio.BufferUnderflowException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

/**
 * Class representing a 3D mesh, with vertices and faces.
 * The mesh is assumed to be made of triangular faces.
 */
public class Mesh implements IMesh {

    /**
     * The mesh vertices, as x,y,z coordinates.
     */
    public List<float[]> vertices;

    /**
     * The mesh faces, as indices into the vertex list. The mesh is assumed to be triangular.
     */
    public List<int[]> faces;

    /**
     * Constructor.
     */
    public Mesh() {
        vertices = new ArrayList<>();
        faces = new ArrayList<>();
    }

    /**
     * Constructor that takes a list of vertices and a list of faces.
     * @param vertices the mesh vertices, as x,y,z coordinates.
     * @param faces the mesh faces, as indices into the vertex list. The mesh is assumed to be triangular.
     */
    public Mesh(ArrayList<float[]> vertices, ArrayList<int[]> faces) {
        this.vertices = vertices;
        this.faces = faces;
    }

    /**
     * Add a vertex to the mesh.
     * @param vertex the vertex to add, as x,y,z coordinates.
     */
    public void addVertex(float[] vertex) {
        vertices.add(vertex);
    }

    /**
     * Add a face to the mesh.
     * @param face the face to add, as indices into the vertex list. The face is assumed to be triangular.
     */
    public void addFace(int[] face) {
        faces.add(face);
    }

    /**
     * Get a vertex from the mesh.
     * @param index the index of the vertex to get.
     * @return the vertex, as x,y,z coordinates.
     */
    public float[] getVertex(int index) {
        return vertices.get(index);
    }

    /**
     * Get a face from the mesh.
     * @param index the index of the face to get.
     * @return the face, as indices into the vertex list. The face is assumed to be triangular.
     */
    public int[] getFace(int index) {
        return faces.get(index);
    }

    /**
     * Get the number of vertices in the mesh.
     * @return the number of vertices in the mesh.
     */
    public int getNumberOfVertices() {
        return vertices.size();
    }

    /**
     * Get the number of faces in the mesh.
     * @return the number of faces in the mesh.
     */
    public int getNumberOfFaces() {
        return faces.size();
    }

    /**
     * Set the vertices of the mesh. Does not change the faces, you have to ensure consistency yourself.
     * @param vertices the new vertices.
     */
    public void setVertices(List<float[]> vertices) {
        this.vertices = vertices;
    }

    /**
     * Set the faces of the mesh. Does not change the vertices, you have to ensure consistency yourself.
     * @param faces the new faces.
     */
    public void setFaces(List<int[]> faces) {
        this.faces = faces;
    }

    /**
     * Get all vertices of the mesh.
     * @return the vertices of the mesh.
     */
    public List<float[]> getVertices() {
        return this.vertices;
    }

    /**
     * Get all faces of the mesh.
     * @return the faces of the mesh.
     */
    public List<int[]> getFaces() {
        return this.faces;
    }

    /**
     * Compute the vertex normals of the mesh.
     * @return the vertex normals of the mesh.
     */
    public List<float[]> computeVertexNormals() {
        List<float[]> vertexNormals = new ArrayList<>();
        for (int i = 0; i < this.vertices.size(); i++) {
            vertexNormals.add(new float[]{ 0, 0, 0 });
        }
        for (int[] face : this.faces) {
            float[] v0 = this.vertices.get(face[0]);
            float[] v1 = this.vertices.get(face[1]);
            float[] v2 = this.vertices.get(face[2]);
            float[] normal = computeNormal(v0, v1, v2);
            for (int i = 0; i < 3; i++) {
                vertexNormals.get(face[i])[0] += normal[0];
                vertexNormals.get(face[i])[1] += normal[1];
                vertexNormals.get(face[i])[2] += normal[2];
            }
        }
        for (int i = 0; i < vertexNormals.size(); i++) {
            double[] normal = Utilities.convertFloatsToDoubles(vertexNormals.get(i));
            double length = new Vector3d(normal).length();
            if (length > 0) {
                normal[0] /= length;
                normal[1] /= length;
                normal[2] /= length;
            }
        }
        return vertexNormals;
    }

    /**
     * Compute the face normals of the mesh.
     * @return the face normals of the mesh.
     */
    public List<float[]> computeFaceNormals() {
        List<float[]> faceNormals = new ArrayList<>();
        for (int[] face : this.faces) {
            float[] v0 = this.vertices.get(face[0]);
            float[] v1 = this.vertices.get(face[1]);
            float[] v2 = this.vertices.get(face[2]);
            faceNormals.add(computeNormal(v0, v1, v2));
        }
        return faceNormals;
    }

    /**
     * Compute the normal given the three vertices of a face.
     * @param v0 coordinates of the first vertex
     * @param v1 coordinates of the second vertex
     * @param v2 coordinates of the third vertex
     * @return the normal vector
     */
    private float[] computeNormal(float[] v0, float[] v1, float[] v2) {
        float[] normal = new float[3];
        float[] v01 = Utilities.vectorSubtract(v1, v0);
        float[] v02 = Utilities.vectorSubtract(v2, v0);
        normal[0] = v01[1] * v02[2] - v01[2] * v02[1];
        normal[1] = v01[2] * v02[0] - v01[0] * v02[2];
        normal[2] = v01[0] * v02[1] - v01[1] * v02[0];
        return normal;
    }

    /**
     * Generate a cube with side length 1 and centered at the origin.
     *
     * Used mainly for testing purposes.
     *
     * @return a cube with side length 1 and centered at the origin.
     */
    public static Mesh generateCube() {
        Mesh cube = new Mesh();
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
     * Enumeration of supported mesh file formats.
     */
    public enum MeshFileFormat {
        PLY,
        OBJ,
        SURF,
        MZ3
    }

    /**
     * Generate string representation of this mesh in PLY format.
     * @return the PLY format string
     */
    private static MeshFileFormat meshFileFormatFromFileExtension(Path filePath) {
        String fileNameLower = filePath.getFileName().toString().toLowerCase();
        if (fileNameLower.endsWith(".ply")) {
            return MeshFileFormat.PLY;
        }
        else if (fileNameLower.endsWith(".obj")) {
            return MeshFileFormat.OBJ;
        }
        else { // FreeSurfer surf files typically have no file extension.
            return MeshFileFormat.SURF;
        }
    }

    /**
     * Get the mesh file format from the file path and format string.
     * @param filePath the path to the file
     * @param format the format string
     * @return the mesh file format
     * @throws IOException if an error occurs
     */
    protected static MeshFileFormat getMeshFileFormat(Path filePath, String format) throws IOException {
        String formatLower = format.toLowerCase();
        if (formatLower.equals("auto")) {
            return meshFileFormatFromFileExtension(filePath);
        }
        else {

            if (format.equals("ply")) {
                return MeshFileFormat.PLY;
            }
            else if (format.equals("obj")) {
                return MeshFileFormat.OBJ;
            }
            else if (format.equals("surf")) {
                return MeshFileFormat.SURF;
            }
            else {
                throw new IOException(MessageFormat.format("Unknown mesh format {0}.", format));
            }
        }
    }

    /**
     * Write this mesh to a file in PLY, OBJ or FreeSurfer surf format.
     * @param filePath the path to the file to write to
     * @param format the format to write to, either "ply", "obj", or "surf". Alternatively, "auto" to derive the format from the filePath.
     * @throws IOException if IO error occurs.
     */
    public void write(Path filePath, String format) throws IOException {

        MeshFileFormat meshFileType = getMeshFileFormat(filePath, format);

        if (meshFileType.equals(MeshFileFormat.PLY)) {
            Files.write(filePath, toPlyFormat().getBytes());
        }
        else if (meshFileType.equals(MeshFileFormat.OBJ)) {
            Files.write(filePath, toObjFormat().getBytes());
        }
        else if (meshFileType.equals(MeshFileFormat.SURF)) {
            FsSurface surface = new FsSurface(this);
            surface.writeSurface(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unhandled mesh export format {0}.", meshFileType));
        }
    }

    /**
     * Read a file in MZ3, PLY, or FreeSurfer mesh format and return an FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.ply")`. The file format will be determined from the file extension.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #readFormat(Path, String) if you want to read a file and specify the format.
     */
    public static Mesh read(Path filePath) throws IOException, FileNotFoundException {
        return Mesh.readFormat(filePath, "auto");
    }

    /**
     * Read a file in MZ3, PLY, or FreeSurfer mesh format and return an FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.mgz")`. The file format will be determined from the file extension if parameter `format` is set to `auto`.
     * @param format the file format to read, either "mz3", "ply", "surf", or "auto" to auto-detect from the file name.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #read(Path) if you want to read a file without specifying the format.
     */
    public static Mesh readFormat(Path filePath, String format) throws IOException, FileNotFoundException {
        MeshFileFormat meshFormat = getMeshFileFormat(filePath, format);

        if (meshFormat.equals(MeshFileFormat.MZ3)) {
            return fromMz3File(filePath);
        }
        else if (meshFormat.equals(MeshFileFormat.PLY)) {
            return fromPlyFile(filePath);
        }
        else if (meshFormat.equals(MeshFileFormat.SURF)) {
            FsSurface surface = FsSurface.fromFsSurfaceFile(filePath);
            return surface.mesh;
        }
        else {
            throw new IOException(MessageFormat.format("Unknown mesh file format {0}.", meshFormat.toString()));
        }
    }

    /**
     * Read a file in MZ3 surface format and return an FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.mz3")`.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if file not found.
     * @throws BufferUnderflowException if buffer underflow occurs, i.e., the file is too short to read the first header part required to determine the file format.
     */
    public static Mesh fromMz3File(Path filePath) throws IOException, FileNotFoundException, BufferUnderflowException {
        Mz3 mz3 = Mz3.fromMz3File(filePath);
        return mz3.mesh;
    }

    /**
     * Read a file in ASCII PLY format and return a FsSurface object. Skips vertex colors and normals, if any.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.ply")`.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     */
    public static Mesh fromPlyFile(Path filePath) throws IOException {

        Mesh mesh = new Mesh();

        List<String> lines = Files.readAllLines(filePath);
        PlyHeaderInfo headerInfo = parsePlyHeader(lines, filePath);

        List<String> vertexLines = lines.subList(headerInfo.headerEndLineIndex + 1, headerInfo.headerEndLineIndex + 1 + headerInfo.vertexCount);
        for (String vertexLine : vertexLines) {
            String[] vertexLineParts = vertexLine.split(" ");
            if (vertexLineParts.length < 3) {
                throw new IOException(MessageFormat.format("PLY file {0} contains an invalid vertex line: {1}.", filePath.toString(), vertexLine));
            }
            float[] vertex = new float[3];
            vertex[0] = Float.parseFloat(vertexLineParts[0]);
            vertex[1] = Float.parseFloat(vertexLineParts[1]);
            vertex[2] = Float.parseFloat(vertexLineParts[2]);
            mesh.addVertex(vertex);
        }

        List<String> faceLines = lines.subList(headerInfo.headerEndLineIndex + 1 + headerInfo.vertexCount,
                headerInfo.headerEndLineIndex + 1 + headerInfo.vertexCount + headerInfo.faceCount);
        for (String faceLine : faceLines) {
            String[] faceLineParts = faceLine.split(" ");
            if (faceLineParts.length < 4) {
                throw new IOException(MessageFormat.format("PLY file {0} contains an invalid face line: {1}.", filePath.toString(), faceLine));
            }
            int[] face = new int[3];
            face[0] = Integer.parseInt(faceLineParts[1]);
            face[1] = Integer.parseInt(faceLineParts[2]);
            face[2] = Integer.parseInt(faceLineParts[3]);
            mesh.addFace(face);
        }

        return mesh;
    }

    /**
     * Parse information from the header lines of a PLY file, ASCII version.
     * @param plyLines the lines of the PLY file, as a list of strings.
     * @param filePath the path to the PLY file, as a Path object. Only used in error messages, as we already have the plyLines.
     * @return a PlyHeaderInfo object containing the parsed information.
     * @throws IOException if IO error occurs.
     */
    protected static PlyHeaderInfo parsePlyHeader(List<String> plyLines, Path filePath) throws IOException {

        if (plyLines.size() < 9) {
            throw new IOException(MessageFormat.format("PLY files must have at least 9 lines but {0} has {1}.", filePath.toString(), plyLines.size()));
        }

        if (plyLines.get(0).compareTo("ply") != 0) {
            throw new IOException(MessageFormat.format("First line of PLY file {0} must be 'ply' but is '{1}'.", filePath.toString(), plyLines.get(0)));
        }

        if (plyLines.get(1).compareTo("format ascii 1.0") != 0) {
            throw new IOException(MessageFormat.format("Second line of PLY file {0} must be 'format ascii 1.0' but is '{1}'.", filePath.toString(), plyLines.get(1)));
        }

        PlyHeaderInfo headerInfo = new PlyHeaderInfo();
        int headerEndLineIndex = plyLines.indexOf("end_header");
        headerInfo.headerEndLineIndex = headerEndLineIndex;

        List<String> headerLines = plyLines.subList(0, headerEndLineIndex);

        // Determine vertex count
        int vertexCountLineIndex = getListIndexStringStartingWith(headerLines, "element vertex");

        if (vertexCountLineIndex == -1) {
            throw new IOException(MessageFormat.format("PLY file {0} does not contain a line 'element vertex'.", filePath.toString()));
        }
        String vertexCountLine = headerLines.get(vertexCountLineIndex);
        String[] vertexCountLineParts = vertexCountLine.split(" ");
        if (vertexCountLineParts.length != 3) {
            throw new IOException(MessageFormat.format("PLY file {0} contains an invalid line 'element vertex': {1}.", filePath.toString(), vertexCountLine));
        }
        headerInfo.vertexCount = Integer.parseInt(vertexCountLineParts[2]);

        // Determine face count
        int faceCountLineIndex = getListIndexStringStartingWith(headerLines, "element face");
        if (faceCountLineIndex == -1) {
            throw new IOException(MessageFormat.format("PLY file {0} does not contain a line 'element face'.", filePath.toString()));
        }
        String faceCountLine = headerLines.get(faceCountLineIndex);
        String[] faceCountLineParts = faceCountLine.split(" ");
        if (faceCountLineParts.length != 3) {
            throw new IOException(MessageFormat.format("PLY file {0} contains an invalid line 'element face': {1}.", filePath.toString(), faceCountLine));
        }
        headerInfo.faceCount = Integer.parseInt(faceCountLineParts[2]);

        headerInfo.containsVertexColors = headerLines.indexOf("property uchar red") >= 0;
        headerInfo.containsVertexNormals = headerLines.indexOf("property float nx") >= 0;

        return headerInfo;

    }

    /**
     * Utility function to find index of line starting with a given prefix in a list of strings.
     * @param lines the list of strings to search in.
     * @param prefix the prefix to search for.
     * @return the index of the first line starting with the given prefix, or -1 if no such line exists.
     */
    protected static int getListIndexStringStartingWith(List<String> lines, String prefix) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Generate string representation of this mesh in PLY format.
     * @return the PLY format string
     */
    public String toPlyFormat() {
        List<Color> vertexColors = null;
        return toPlyFormat(vertexColors);
    }

    /**
     * Generate string representation of this mesh in PLY format, with vertex colors.
     * @param vertexColors the vertex colors to use, as a list of `java.awt.color` instances.
     * @return the PLY format string
     */
    public String toPlyFormat(List<Color> vertexColors) {
        Boolean useVertexColors = vertexColors != null;
        StringBuilder builder = new StringBuilder();
        builder.append("ply\n");
        builder.append("format ascii 1.0\n");
        builder.append("comment Created by jneuroformats\n");
        builder.append("element vertex " + this.getNumberOfVertices() + "\n");
        builder.append("property float x\n");
        builder.append("property float y\n");
        builder.append("property float z\n");
        if (useVertexColors) {
            if (vertexColors.size() != this.vertices.size()) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Number of vertex coordinates and vertex colors must match when writing PLY file, but got {0} and {1} respectively.",
                                this.vertices.size(), vertexColors.size()));
            }
            builder.append("property uchar red\nproperty uchar green\nproperty uchar blue\n");
        }
        builder.append("element face " + this.getNumberOfFaces() + "\n");
        builder.append("property list uchar int vertex_indices\n");
        builder.append("end_header\n");
        float[] vertex;
        Color vertexColor;
        for (int i = 0; i < this.vertices.size(); i++) {
            vertex = this.vertices.get(i);
            builder.append(vertex[0] + " " + vertex[1] + " " + vertex[2]);
            if (useVertexColors) {
                vertexColor = vertexColors.get(i);
                builder.append(" " + vertexColor.getRed() + " " + vertexColor.getGreen() + " " + vertexColor.getBlue());
            }
            builder.append("\n");
        }
        for (int[] face : this.faces) {
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
        for (float[] vertex : this.vertices) {
            builder.append("v " + vertex[0] + " " + vertex[1] + " " + vertex[2] + "\n");
        }
        for (int[] face : this.faces) {
            builder.append("f " + face[0] + " " + face[1] + " " + face[2] + "\n");
        }
        return builder.toString();
    }

    /**
     * Models the header information from an ASCII PLY file.
     */
    protected static class PlyHeaderInfo {

        public int headerEndLineIndex;
        public int vertexCount;
        public int faceCount;
        public Boolean containsVertexColors;
        public Boolean containsVertexNormals;

    }

}
