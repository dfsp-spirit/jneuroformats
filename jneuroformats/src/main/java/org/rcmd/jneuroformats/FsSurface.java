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

import java.io.FileNotFoundException;
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

import org.rcmd.jneuroformats.Mesh.MeshFileFormat;

/**
 * Represents a FreeSurfer surface, i.e. a triangular mesh
 * consisting of a set of vertices and a set of faces.
 *
 * In the context of neuroimaging, meshes are typically used
 * to store brain surface reconstructions, e.g., the cortical
 * surface reconstructed from a T1-weighted MRI scan.
 */
public class FsSurface implements IMesh {

    public Mesh mesh;
    public String commentLine = "";
    public String createdLine = "";

    /**
     * Create an empty surface.
     */
    public FsSurface() {
        this.mesh = new Mesh();
    }

    public FsSurface(Mesh mesh) {
        this.mesh = mesh;
    }

    /**
     * Constructor that takes a list of vertices and a list of faces.
     * @param vertices the mesh vertices, as x,y,z coordinates.
     * @param faces the mesh faces, as indices into the vertex list. The mesh is assumed to be triangular.
     */
    public FsSurface(ArrayList<float[]> vertices, ArrayList<int[]> faces) {
        this.mesh = new Mesh();
        this.mesh.vertices = vertices;
        this.mesh.faces = faces;
    }



    /**
     * Read a file in MZ3, PLY, or FreeSurfer mesh format and return an FsSurface object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.ply")`. The file format will be determined from the file extension.
     * @return an FsSurface object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #readFormat(Path, String) if you want to read a file and specify the format.
     */
    public static FsSurface read(Path filePath) throws IOException, FileNotFoundException {
        return FsSurface.readFormat(filePath, "auto");
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
    public static FsSurface readFormat(Path filePath, String format) throws IOException, FileNotFoundException {
        Mesh.MeshFileFormat meshFormat = Mesh.getMeshFileFormat(filePath, format);

        if(meshFormat.equals(Mesh.MeshFileFormat.MZ3)) {
            return new FsSurface(Mesh.fromMz3File(filePath));
        }
        else if(meshFormat.equals(Mesh.MeshFileFormat.PLY)) {
            return new FsSurface(Mesh.fromPlyFile(filePath));
        }
        else if(meshFormat.equals(Mesh.MeshFileFormat.SURF)) {
            return FsSurface.fromFsSurfaceFile(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unknown mesh file format {0}.", meshFormat.toString()));
        }
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
            surface.mesh.addVertex(vertex);
        }

        for (int i = 0; i < numberOfFaces; i++) {
            int[] face = new int[3];
            face[0] = buffer.getInt();
            face[1] = buffer.getInt();
            face[2] = buffer.getInt();
            surface.mesh.addFace(face);
        }

        return surface;
    }

    /**
     * Write this mesh to a file in FreeSurfer surface format.
     * @param filePath the path to the file to write to
     * @throws IOException
     */
    protected void writeSurface(Path filePath) throws IOException {
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
        buf.putInt(this.mesh.getNumberOfVertices());

        // write number of faces
        buf.putInt(this.mesh.getNumberOfFaces());

        // write vertices
        for (float[] vertex : this.mesh.vertices) {
            buf.putFloat(vertex[0]);
            buf.putFloat(vertex[1]);
            buf.putFloat(vertex[2]);
        }

        // write faces
        for (int[] face : this.mesh.faces) {
            buf.putInt(face[0]);
            buf.putInt(face[1]);
            buf.putInt(face[2]);
        }

        buf.flip();
        buf.order(ByteOrder.BIG_ENDIAN);

        return buf;
    }

    /**
     * Write this mesh to a file in PLY, OBJ or FreeSurfer surf format.
     * @param filePath the path to the file to write to
     * @param format the format to write to, either "ply", "obj", or "surf". Alternatively, "auto" to derive the format from the filePath.
     * @throws IOException if IO error occurs.
     */
    public void write(Path filePath, String format) throws IOException {

        MeshFileFormat meshFileType = Mesh.getMeshFileFormat(filePath, format);

        if (meshFileType.equals(MeshFileFormat.SURF)) {
            this.writeSurface(filePath);
        }
        else {
            this.mesh.write(filePath, format);
        }
    }


    @Override
    public List<int[]> getFaces() {
        return this.mesh.getFaces();
    }

    @Override
    public List<float[]> getVertices() {
        return this.mesh.getVertices();
    }

    @Override
    public void setVertices(List<float[]> vertices) {
        this.mesh.setVertices(vertices);
    }

    @Override
    public void setFaces(List<int[]> faces) {
        this.mesh.setFaces(faces);
    }

    public int getNumberOfVertices() {
        return this.mesh.vertices.size();
    }

    /**
     * Get the number of faces in the mesh.
     * @return the number of faces in the mesh.
     */
    public int getNumberOfFaces() {
        return this.mesh.faces.size();
    }



}
