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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;



public class FsAnnot {

    /** The vertex indices to the data in the 'labels'. This seems to always be simply a list from 0 to numVertices - 1, as
     * an annotation typically spans the entire cortex. Vertices not belongning to any relevant region are assigned to an 'unknown' region.
     */
    public List<Integer> vertexIndices;
    /** The labels for the vertices, i.e., a region code. The code can be mapped to a region name and a color using a lookup table.
     * purposes. The color table that comes with FreeSurfer can be found in the FreeSurfer directory under 'FreeSurferColorLUT.txt'.
     * The annot file itself also includes a color table.
     */
    public List<Integer> vertexLabels;

    /**
     * Whether this annot contains its own color table, i.e., whether its colortable attribute is not null.
     */
    public Boolean hasColortable() {
        return this.colortable != null;
    }

    public FsColortable colortable;

    // Constructor
    public FsAnnot(List<Integer> vertexIndices, List<Integer> vertexLabels) {
        this.vertexIndices = vertexIndices;
        this.vertexLabels = vertexLabels;
        this.colortable = null;
    }

    // Constructor
    public FsAnnot(List<Integer> vertexIndices, List<Integer> vertexLabels, FsColortable colortable) {
        this.vertexIndices = vertexIndices;
        this.vertexLabels = vertexLabels;
        this.colortable = colortable;
    }

    // Constructor
    public FsAnnot() {
        this.vertexIndices = new ArrayList<>();
        this.vertexLabels = new ArrayList<>();
        this.colortable = null;
    }

    /**
     * Compute the number of unique regions in this annot based on unique entries in the vertexLabels.
     * @return integer, the number of unique regions.
     */
    public int numRegions() {
        return new HashSet<>(this.vertexLabels).size();
    }

    public int numVertices() {
        return this.vertexIndices.size();
    }

    /**
     * Compute the number of unique regions in this annot based on number of entries in the colortable.
     * @return integer, the number of unique regions. Returns 0 if this annot does not have a colortable.
     */
    private int numRegionsFromColortable() {
        if (this.hasColortable()) {
            return this.colortable.numRegions();
        } else {
            return 0;
        }
    }

    public void validate() throws IOException {
        if(this.vertexIndices.size() != this.vertexLabels.size()) {
            throw new IOException("The number of entries in the FsAnnot vertexIndices list does not match the number of elements in the vertexLabels list.");
        }
        if (this.hasColortable()) {
            if(this.numRegions() != this.numRegionsFromColortable()) {
                throw new IOException("The number of regions based on unique entries in the FsAnnot vertexLabels field does not match the number of regions in the colortable.");
            }
        }
    }


    /**
     * Read a file in FreeSurfer annot format and return an FsAnnot object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsAnnot object.
     * @throws IOException if IO error occurs.
     */
    public static FsAnnot fromFsAnnotFile(Path filePath) throws IOException {

        FsAnnot annot = new FsAnnot();

        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        int numberOfVertices = buffer.getInt();
        for(int i = 0; i < (numberOfVertices * 2); i++) {
            if(i % 2 == 0) {
                annot.vertexIndices.add(buffer.getInt());
            } else {
                annot.vertexLabels.add(buffer.getInt());
            }
        }

        int fileContainsColortable = buffer.getInt();
        if(fileContainsColortable == 1) {
            int numColortableEntriesOldFormat = buffer.getInt();
            if(numColortableEntriesOldFormat > 0) {
                throw new IOException(MessageFormat.format("Reading annotation in old format not supported. Please open an issue and supply an example file if you need this: '{0}'.\n", filePath.toString()));
            } else {
                int colortableFormatVersion = - numColortableEntriesOldFormat; // If the value is negative, we are in new format and its absolute value is the format version.
                if(colortableFormatVersion == 2) {
                    int numColortableEntries = buffer.getInt();   // This time for real.
                    annot.colortable = FsColortable.fromByteBuffer(buffer, numColortableEntries);
                } else {
                    throw new IOException(MessageFormat.format("Reading annotation in format version {0} not supported. Please open an issue and supply an example file if you need this: '{1}'.\n", colortableFormatVersion, filePath.toString()));
                }
            }

        } else {
            throw new IOException(MessageFormat.format("Reading annotation without colortable not supported. Maybe invalid annotation file '{0}'.\n", filePath.toString()));
        }

        return annot;
    }

    /**
     * Generate string representation of this FsAnnot in CSV format. Note that this does not include the colortable, which has its own `toCsvFormat` method.
     * @param with_header whether to include a header row at the top of the CSV.
     * @return the CSV format string
     */
    public String toCsvFormat(Boolean with_header) {
        StringBuilder builder = new StringBuilder();
        if(with_header) {
            builder.append("vertex_index,vertex_label\n");
        }

        for (int i = 0;  i < this.vertexIndices.size(); i++) {
            builder.append(this.vertexIndices.get(i) + "," + this.vertexLabels.get(i) + "\n");
        }
        return builder.toString();
    }

    /**
     * Write this FsCurv to a file in FreeSurfer curv format.
     * @param filePath the path to the file to write to
     * @throws IOException
     */
    private void writeAnnot(Path filePath) throws IOException {
        ByteBuffer buf = writeFsAnnotToByteBuffer();
        WritableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.WRITE);
        channel.write(buf);
        channel.close();
    }

    public List<int[]> getVertexColorsRgb() {
        List<int[]> colors = new ArrayList<>(this.numVertices());
        for(int i = 0; i < this.vertexLabels.size(); i++) {
            colors.add(this.colortable.getRgbForLabel(this.vertexLabels.get(i)));
        }
        return colors;
    }

    /**
     * Write this mesh to a ByteBuffer in FreeSurfer surface format.
     * @note This method is used internally by writeSurface(Path filePath).
     * @throws IOException
     */
    private ByteBuffer writeFsAnnotToByteBuffer() throws IOException {

        ByteBuffer buf = ByteBuffer.allocate(this.numVertices() * 20);

        // write number of vertices
        buf.putInt(this.numVertices());

        // write vertex indices and labels
        for(int i = 0; i < this.numVertices(); i++) {
            buf.putInt(this.vertexIndices.get(i));
            buf.putInt(this.vertexLabels.get(i));
        }

        // has_colortable
        buf.putInt(this.hasColortable() ? 1 : 0);
        buf.putInt(-2);  // colortable format version

        if(this.hasColortable()) {
            buf.putInt(this.colortable.numRegions());
            buf = this.colortable.writeFsColortableToByteBuffer(buf);
        } else {
            System.err.println("Warning: writing annotation without colortable. Most software will not be able to read this file.");
        }


        buf.flip();
        return buf;
    }


    /**
     * Write this FsAnnot to a file in annot or CSV format.
     * @param filePath the path to the file to write to
     * @param format the format to write to, either "fsannot" or "csv".
     * @throws IOException
     */
    public void writeToFile(Path filePath, String format) throws IOException {
        format = format.toLowerCase();
        if (format.equals("csv")) {
            Files.write(filePath, toCsvFormat(Boolean.TRUE).getBytes());
        } else if (format.equals("fsannot")) {
            this.writeAnnot(filePath);
        } else {
            throw new IOException(MessageFormat.format("Unknown FsAnnot export format {0}.", format));
        }
    }

}
