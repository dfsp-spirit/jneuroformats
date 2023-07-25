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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.rcmd.jneuroformats.IO.IOUtil;

public class FsColortable {

    public List<Integer> structureId;
    public List<Integer> red;
    public List<Integer> green;
    public List<Integer> blue;
    public List<Integer> transparency;
    public List<String> structureName;
    public List<Integer> label;

    public FsColortable() {
        structureId = new ArrayList<>(); // the structure ID, seems unused. This is NOT the region code from the annotation file. See the label field for that.
        red = new ArrayList<>(); // RGBA color red channel value, 0 - 255.
        green = new ArrayList<>();
        blue = new ArrayList<>();
        transparency = new ArrayList<>();
        structureName = new ArrayList<>();
        label = new ArrayList<>(); // the region code, as used in the annotation file.
    }

    public FsColortable(List<Integer> structureId, List<Integer> red, List<Integer> green, List<Integer> blue, List<Integer> transparency, List<String> structureName,
                        List<Integer> label) {
        this.structureId = structureId;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.transparency = transparency;
        this.structureName = structureName;
        this.label = label;
    }

    public int numRegions() {
        return structureId.size();
    }

    public void validate() throws IOException {
        if (this.structureId.size() != this.red.size()) {
            throw new IOException("The number of entries in the FsColortable structureID list does not match the number of elements in the red list.");
        }
        if (this.structureId.size() != this.green.size()) {
            throw new IOException("The number of entries in the FsColortable structureID list does not match the number of elements in the green list.");
        }
        if (this.structureId.size() != this.blue.size()) {
            throw new IOException("The number of entries in the FsColortable structureID list does not match the number of elements in the blue list.");
        }
        if (this.structureId.size() != this.transparency.size()) {
            throw new IOException("The number of entries in the FsColortable structureID list does not match the number of elements in the transparency list.");
        }
        if (this.structureId.size() != this.structureName.size()) {
            throw new IOException("The number of entries in the FsColortable structureID list does not match the number of elements in the structureName list.");
        }
        if (this.structureId.size() != this.label.size()) {
            throw new IOException("The number of entries in the FsColortable structureID list does not match the number of elements in the label list.");
        }
    }

    /**
     * Get the RGB colors for all regions, as a list of integers. The list contains the red, green, and blue channel values for each vertex, in that order (v0r, v0g, v0b, v1r, v1g, v1b, ...). Color values are in range 0 - 255.
     * @return a list of integers, representing RBG vertex colors.
     */
    public List<Integer> getColorsRgb() {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < this.structureId.size(); i++) {
            colors.add(this.red.get(i));
            colors.add(this.green.get(i));
            colors.add(this.blue.get(i));
        }
        return colors;
    }

    /**
     * Get the RGBA colors for all regions, as a list of integers. The list contains the red, green, blue and transparency channel values for each vertex, in that order (v0r, v0g, v0b, v0t, v1r, v1g, v1b, v1t, ...). Color values are in range 0 - 255.
     * See function `FsAnnot.getVertexColorsRgb()` if you need to get the colors for a specific vertex or all vertices.
     * @param as_transparency whether to use transparency or alpha as the last channel. Transparency is 0 - 255, 255 is fully transparent, 0 is fully opaque. Alpha is 0 - 255, 255 is fully opaque, 0 is fully transparent. Note that the FreeSurfer files store transparency, but most graphics libraries use alpha, so set this to `Boolean.FALSE` if in doubt.
     * @return a list of integers, representing RBGA (or RGBT, depending on the setting of the `as_transparency` parameter) vertex colors.
     */
    public List<Integer> getColorsRgba(Boolean as_transparency) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < this.structureId.size(); i++) {
            colors.add(this.red.get(i));
            colors.add(this.green.get(i));
            colors.add(this.blue.get(i));
            if (as_transparency) {
                colors.add(this.transparency.get(i)); // transparency is 0 - 255, 255 is fully transparent, 0 is fully opaque.
            }
            else {
                colors.add(255 - this.transparency.get(i)); // alpha is 0 - 255, 0 is fully opaque, 255 is fully transparent.
            }
        }
        return colors;
    }

    /**
     * Read the colortable part of an annot file into an FsColortable object.
     * @param filePath the path to the annot file.
     * @return an FsColortable object.
     * @throws IOException if IO error occurs.
     */
    public static FsColortable fromFsAnnotFile(Path filePath) throws IOException {
        FsAnnot annot = FsAnnot.fromFsAnnotFile(filePath);
        return annot.colortable;
    }

    /**
     * Read the colortable part of an annot file into an FsColortable object. The colortable is read from the ByteBuffer, starting at the current position in the buffer.
     * @param buf the ByteBuffer to read from.
     * @param colortableNumEntries the number of entries in the colortable. This is the number of entries in the colortable, as specified in the colortable header. Set to -1 if you do not know the number of entries.
     * @return an FsColortable object.
     * @throws IOException if IO error occurs.
     */
    protected static FsColortable fromByteBuffer(ByteBuffer buf, int colortableNumEntries) throws IOException {
        FsColortable colortable = new FsColortable();

        int numCharsOrigFilename = buf.getInt();

        @SuppressWarnings("unused")
        String unusedOrigFilename = IOUtil.readFixedLengthString(buf, numCharsOrigFilename);

        int colortableNumEntriesDuplicated = buf.getInt();
        if (colortableNumEntries >= 0 && colortableNumEntries != colortableNumEntriesDuplicated) {
            System.err.println(MessageFormat.format("Warning: the two number of entries fields in the colortable do not match: {0} versus {1}. Use with care.",
                    colortableNumEntries, colortableNumEntriesDuplicated));
        }

        colortableNumEntries = colortableNumEntriesDuplicated;

        int entryNumChars;
        for (int i = 0; i < colortableNumEntries; i++) {
            colortable.structureId.add(buf.getInt());

            entryNumChars = buf.getInt();
            colortable.structureName.add(IOUtil.readFixedLengthString(buf, entryNumChars));

            colortable.red.add(buf.getInt());
            colortable.green.add(buf.getInt());
            colortable.blue.add(buf.getInt());
            colortable.transparency.add(buf.getInt());
            colortable.label.add(FsColortable.computeLabelFromRgb(colortable.red.get(i), colortable.green.get(i), colortable.blue.get(i)));
        }

        return colortable;
    }

    public static int computeLabelFromRgb(int red, int green, int blue) {
        return red + green*256 + blue*65536;
    }

    public static int[] computeRgbFromLabel(int label) {
        int[] rgbt = new int[3];
        rgbt[0] = label % 256;
        rgbt[1] = (label / 256) % 256;
        rgbt[2] = (label / 65536) % 256;
        return rgbt;
    }

    /**
     * Get the RGB color for a given label. Returns null if the label is not found.
     * @param label the label to get the color for. Typically the vertex label stored in the annotation file.
     * @return an array of three integers, representing the red, green, and blue channel values, in that order. Values are in range 0 - 255.
     */
    public int[] getRgbForLabel(int label) {
        int[] rgb = new int[3];
        for(int i = 0; i < this.label.size(); i++) {
            if(this.label.get(i) == label) {
                rgb[0] = this.red.get(i);
                rgb[1] = this.green.get(i);
                rgb[2] = this.blue.get(i);
                return rgb;
            }
        }
        return null;
    }

    /**
     * Write the colortable to a ByteBuffer.
     * @param buf an existing ByteBuffer to write to. If null, a new ByteBuffer will be created.
     * @return the ByteBuffer, with the colortable written to it.
     * @throws IOException if IO error occurs.
     */
    protected ByteBuffer writeFsColortableToByteBuffer(ByteBuffer buf) throws IOException {
        if (buf == null) {
            buf = ByteBuffer.allocate(8182 * this.numRegions());
        }

        String origFilename = "somefile.txt";
        int numCharsOrigFilename = origFilename.length();
        buf.putInt(numCharsOrigFilename);
        buf.put(origFilename.getBytes());
        buf.putInt(this.numRegions());

        for (int i = 0; i < this.numRegions(); i++) {
            buf.putInt(this.structureId.get(i));

            buf.putInt(this.structureName.get(i).length());
            buf.put(this.structureName.get(i).getBytes());

            buf.putInt(this.red.get(i));
            buf.putInt(this.green.get(i));
            buf.putInt(this.blue.get(i));
            buf.putInt(this.transparency.get(i));
        }

        return buf;
    }

}
