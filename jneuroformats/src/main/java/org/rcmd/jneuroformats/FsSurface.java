/*
 *  Copyright 2023 Tim Schäfer
 *
 *    Licensed under the MIT License (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        https://github.com/dfsp-spirit/jneuroformats/blob/main/LICENSE or at https://opensource.org/licenses/MIT
 *
 *   Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.rcmd.jneuroformats;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.nio.BufferUnderflowException;
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
 * Represents a FreeSurfer surface, i.e. a triangular mesh
 * consisting of a set of vertices and a set of faces.
 *
 * In the context of neuroimaging, meshes are typically used
 * to store brain surface reconstructions, e.g., the cortical
 * surface reconstructed from a T1-weighted MRI scan.
 */
public class FsSurface implements Mesh {

    public List<float[]> vertices;
    public List<int[]> faces;
    public String commentLine = "";
    public String createdLine = "";

    /**
     * Constructor.
     */
    public FsSurface() {
        vertices = new ArrayList<>();
        faces = new ArrayList<>();
    }

    /**
     * Constructor that takes a list of vertices and a list of faces.
     * @param vertices the mesh vertices, as x,y,z coordinates.
     * @param faces the mesh faces, as indices into the vertex list. The mesh is assumed to be triangular.
     */
    public FsSurface(ArrayList<float[]> vertices, ArrayList<int[]> faces) {
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

    public int[] getFace(int index) {
        return faces.get(index);
    }

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
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read the header
        int magicNumberPart1 = IO.getUint8(buffer);
        int magicNumberPart2 = IO.getUint8(buffer);
        int magicNumberPart3 = IO.getUint8(buffer);

        if (magicNumberPart1 != 255 || magicNumberPart2 != 255 || magicNumberPart3 != 254) {
            throw new IOException(MessageFormat.format("Invalid magic number in FreeSurfer surface file: magic codes {0} {1} {2}, expected 255 255 254. File invalid.",
                    magicNumberPart1, magicNumberPart2, magicNumberPart3));
        }

        surface.createdLine = IO.readNewlineTerminatedString(buffer);
        surface.commentLine = IO.readNewlineTerminatedString(buffer);

        int numberOfVertices = buffer.getInt();
        int numberOfFaces = buffer.getInt();

        // System.out.println(MessageFormat.format("CreatedLine is: {0}", unusedCreatedLine));
        // System.out.println(MessageFormat.format("CommentLine is: {0}", unusedCommentLine));
        // System.out.println(MessageFormat.format("Reading surface with {0} vertices and {1} faces.", numberOfVertices, numberOfFaces));

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
     * Read a file in MZ3 surface format and return an FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.mz3")`.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if file not found.
     * @throws BufferUnderflowException if buffer underflow occurs, i.e., the file is too short to read the first header part required to determine the file format.
     */
    public static FsSurface fromMz3File(Path filePath) throws IOException, FileNotFoundException, BufferUnderflowException {
        Mz3 mz3 = Mz3.fromMz3File(filePath);
        return mz3.surface;
    }

    /**
     * Read a file in ASCII PLY format and return a FsSurface object. Skips vertex colors and normals, if any.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.ply")`.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     */
    public static FsSurface fromPlyFile(Path filePath) throws IOException {

        FsSurface surface = new FsSurface();

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
            surface.addVertex(vertex);
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
            surface.addFace(face);
        }

        return surface;
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
     * Generate string representation of this mesh in PLY format.
     * @param vertexColors the vertex colors to use, as a list of int arrays of length 3, with values in the range 0-255. The first 3 values must be the r, g and b value for the first vertex.
     * @return the PLY format string
     */
    public String toPlyFormat(List<Color> vertexColors) {
        Boolean useVertexColors = vertexColors != null;
        StringBuilder builder = new StringBuilder();
        builder.append("ply\n");
        builder.append("format ascii 1.0\n");
        builder.append("comment Created by jneuroformats\n");
        builder.append("element vertex " + getNumberOfVertices() + "\n");
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
        builder.append("element face " + getNumberOfFaces() + "\n");
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
     * Write this mesh to a file in FreeSurfer surface format.
     * @param filePath the path to the file to write to
     * @throws IOException
     */
    private void writeSurface(Path filePath) throws IOException {
        ByteBuffer buf = writeSurfaceToByteBuffer();
        WritableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.WRITE);
        channel.write(buf);
        channel.close();
    }

    /**
     * Write this mesh to a ByteBuffer in FreeSurfer surface format.
     * @note This method is used internally by writeSurface(Path filePath).
     * @throws IOException
     */
    private ByteBuffer writeSurfaceToByteBuffer() throws IOException {

        ByteBuffer buf = ByteBuffer.allocate(8192);

        // write magic bytes
        buf.put((byte) 255);
        buf.put((byte) 255);
        buf.put((byte) 254);

        // write created line
        buf.put(createdLine.getBytes());
        buf.putChar((char) 10); // newline

        // write comment line
        buf.put(commentLine.getBytes());
        buf.putChar((char) 10);

        // write number of vertices
        buf.putInt(getNumberOfVertices());

        // write number of faces
        buf.putInt(getNumberOfFaces());

        // write vertices
        for (float[] vertex : vertices) {
            buf.putFloat(vertex[0]);
            buf.putFloat(vertex[1]);
            buf.putFloat(vertex[2]);
        }

        // write faces
        for (int[] face : faces) {
            buf.putInt(face[0]);
            buf.putInt(face[1]);
            buf.putInt(face[2]);
        }

        buf.flip();
        buf.order(ByteOrder.BIG_ENDIAN);

        return buf;
    }

    public enum MeshFileType {
        PLY, OBJ, SURF
    }

    private MeshFileType meshFileFormatFromFileExtension (Path filePath) {
        String fileNameLower = filePath.getFileName().toString().toLowerCase();
        if(fileNameLower.endsWith(".ply")) {
            return MeshFileType.PLY;
        }
        else if(fileNameLower.endsWith(".obj")) {
            return MeshFileType.OBJ;
        }
        else {  // FreeSurfer surf files typically have no file extension.
            return MeshFileType.SURF;
        }
    }

    private MeshFileType getMeshFileFormat(Path filePath, String format) throws IOException {
        String formatLower = format.toLowerCase();
        if(formatLower.equals("auto")) {
            return meshFileFormatFromFileExtension(filePath);
        } else {

            if (format.equals("ply")) {
                return MeshFileType.PLY;
            }
            else if (format.equals("obj")) {
                return MeshFileType.OBJ;
            }
            else if (format.equals("surf")) {
                return MeshFileType.SURF;
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

        MeshFileType meshFileType = getMeshFileFormat(filePath, format);

        if (meshFileType.equals(MeshFileType.PLY)) {
            Files.write(filePath, toPlyFormat().getBytes());
        }
        else if (meshFileType.equals(MeshFileType.OBJ)) {
            Files.write(filePath, toObjFormat().getBytes());
        }
        else if (meshFileType.equals(MeshFileType.SURF)) {
            this.writeSurface(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unhandled mesh export format {0}.", meshFileType));
        }
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
