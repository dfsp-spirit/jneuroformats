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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the structure, and a value that is assigned to all vertices or voxels that are part of the structure. Sometimes the value is not needed, and can be set to 0.
 */
public class FsMghHeader {

    /** Size of dimension 1 */
    public int dim1Size = 0;

    /** Size of dimension 2 */
    public int dim2Size = 0;

    /** Size of dimension 3 */
    public int dim3Size = 0;

    /** Size of dimension 4 */
    public int dim4Size = 0;

    /** The MRI data type, one of the MRI_* constants defined in FsMgh. This tells you which of the data arrays to use. */
    public int mriDatatype = FsMgh.MRI_FLOAT;

    /** The degrees of freedom (DOF) for the MRI data. */
    public int dof = 0;

    /** The RAS good flag, indicating if the RAS matrix and info are valid. */
    public short rasGoodFlag = 0; // stored as signed int16 in file. 1 means that the RAS matrix and info (size x/y/z, Mdc, Pxyz_c) is good, everything else means it is not.

    // The voxel size in the x, y, and z dimensions. Sometimes referred to as 'Delta' or 'Step' in some FreeSurfer documentation. 3 float values, stored in the file as 3 float values.

    /** The voxel size in the x dimension */
    public float sizeX = 0.0f;

    /** The voxel size in the y dimension */
    public float sizeY = 0.0f;

    /** The voxel size in the z dimension */
    public float sizeZ = 0.0f;

    /**
     * The Mdc matrix, used to compute the vox2RAS matrix and related information.
     */
    public List<Float> Mdc = new ArrayList<>();

    /**
     * The RAS origin. Sometimes referred to as 'Point XYZ center' or 'Pxyz_c'. 3 float values, stored in the file as 3 float values.
     */
    public List<Float> Pxyz_c = new ArrayList<>();

    /**
     * Default constructor for FsMghHeader. Initializes empty lists for Mdc and Pxyz_c.
     */
    public FsMghHeader() {
        Mdc = new ArrayList<>();
        Pxyz_c = new ArrayList<>();
    }

    /**
     * Get the number of values in the data part of the file.
     * @return the number of values in the data part of the file.
     */
    public int getNumValues() {
        return this.dim1Size * this.dim2Size * this.dim3Size * this.dim4Size;
    }

    /**
     * Read an FsMghHeader instance from a Buffer in FreeSurfer MGH format. Reads the header and advances the buffer to the data part of the file.
     * @param buf the buffer to read from.
     * @return an FsMghHeader instance.
     * @throws IOException if IO error occurs, or if the file is not in valid MGH format.
     */
    public static FsMghHeader fromByteBuffer(ByteBuffer buf) throws IOException {
        FsMghHeader header = new FsMghHeader();

        int mghVersion = buf.getInt();
        if (mghVersion != 1) {
            throw new IOException(MessageFormat.format("Invalid MGH format version in MGH file: expected 1, got {0}. File invalid.", mghVersion));
        }

        header.dim1Size = buf.getInt();
        header.dim2Size = buf.getInt();
        header.dim3Size = buf.getInt();
        header.dim4Size = buf.getInt();

        header.mriDatatype = buf.getInt();
        header.dof = buf.getInt();

        header.rasGoodFlag = buf.getShort();

        int unusedHeaderSpaceSizeLeft = 254; // in bytes

        Short validRasGoodFlagValue = 1;

        if (header.rasGoodFlag == validRasGoodFlagValue) {
            header.sizeX = buf.getFloat();
            header.sizeY = buf.getFloat();
            header.sizeZ = buf.getFloat();

            for (int i = 0; i < 9; i++) {
                header.Mdc.add(buf.getFloat());
            }
            for (int i = 0; i < 3; i++) {
                header.Pxyz_c.add(buf.getFloat());
            }
            unusedHeaderSpaceSizeLeft -= 60;
        }

        // Skip the rest of the unused header space and advance buffer to data part of file.
        // We do not seek (via buf.position()) because we want to be able to use this function also for gzip streams later.
        @SuppressWarnings("unused")
        byte unusedByte;
        while (unusedHeaderSpaceSizeLeft > 0) {
            unusedByte = buf.get();
            unusedHeaderSpaceSizeLeft--;
        }

        return header;
    }

    /**
     * Get the size of the header part of the file, in bytes.
     * @return the size of the header part of the file, in bytes. This is always the same for the MGH format, which uses a fixed size header.
     */
    public int getHeaderSizeInBytes() {
        return 284;
    }

    /**
     * Get the size of the data part of the file, in bytes. Data size is computed based on header information: data type and number of values.
     * @return the size of the data part of the file, in bytes.
     */
    public int getDataSizeInBytes() {
        return this.getNumValues() * this.getNumBytesPerValue();
    }

    private int getNumBytesPerValue() {
        int numBytesPerValue = 4;
        if (this.mriDatatype == FsMgh.MRI_FLOAT) {
            numBytesPerValue = 4;
        }
        else if (this.mriDatatype == FsMgh.MRI_INT) {
            numBytesPerValue = 4;
        }
        else if (this.mriDatatype == FsMgh.MRI_SHORT) {
            numBytesPerValue = 2;
        }
        else if (this.mriDatatype == FsMgh.MRI_UCHAR) {
            numBytesPerValue = 1;
        }
        return numBytesPerValue;
    }

    /**
     * Write the FsMghHeader to a ByteBuffer.
     * @param buf an existing ByteBuffer to write to. If null, a new ByteBuffer will be created.
     * @return the ByteBuffer, with the FsMghHeader written to it.
     * @throws IOException if IO error occurs.
     */
    protected ByteBuffer writeFsMghHeaderToByteBuffer(ByteBuffer buf) throws IOException {
        if (buf == null) {
            buf = ByteBuffer.allocate(this.getHeaderSizeInBytes() + this.getDataSizeInBytes() + 1000);
        }

        int mghVersionNumber = 1;
        buf.putInt(mghVersionNumber);

        buf.putInt(this.dim1Size);
        buf.putInt(this.dim2Size);
        buf.putInt(this.dim3Size);
        buf.putInt(this.dim4Size);

        buf.putInt(this.mriDatatype);
        buf.putInt(this.dof);
        buf.putShort(rasGoodFlag);

        Short validRasGoodFlagValue = 1;

        if (this.rasGoodFlag == validRasGoodFlagValue) {
            buf.putFloat(this.sizeX);
            buf.putFloat(this.sizeY);
            buf.putFloat(this.sizeZ);

            for (int i = 0; i < 9; i++) {
                buf.putFloat(this.Mdc.get(i));
            }

            for (int i = 0; i < 3; i++) {
                buf.putFloat(this.Pxyz_c.get(i));
            }

        }
        else {
            for (int i = 0; i < 60; i++) {
                buf.put((byte) 0);
            }
        }

        // fill rest of the reserved header space with zeros
        for (int i = 0; i < 194; i++) {
            buf.put((byte) 0);
        }

        return buf;
    }

    /**
     * Read an FsMghHeader instance from a file.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsMghHeader instance.
     * @throws IOException if IO error occurs, or if the file is not in valid MGH format.
     * @throws FileNotFoundException if the file does not exist.
     */
    public static FsMghHeader fromFsMghFile(Path filePath) throws IOException, FileNotFoundException {
        byte[] data = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return FsMghHeader.fromByteBuffer(buffer);
    }

    /**
     * Compute the 4x4 voxel-to-RAS (vox2ras) matrix from the RAS information in this header, if available.
     *
     * The vox2ras matrix maps voxel indices to world (RAS) coordinates: the world coordinate of a
     * voxel (i, j, k) is `vox2ras * [i, j, k, 1]`. Its linear part (the upper left 3x3 block)
     * maps a unit step along voxel axis j to a world displacement of `size_j * Mdc_row_j`, i.e.
     * column j is scaled by the voxel size of axis j. This is the same convention FreeSurfer uses
     * (vox2ras = Mdc^T * diag(delta), with the rows of the MGH `Mdc` matrix being the unit
     * direction cosines of the 3 volume axes). The translation is the RAS coordinate of voxel
     * (0, 0, 0), which is derived from the center voxel `Pxyz_c` stored in the header.
     *
     * @return the vox2ras matrix as a 4x4 float array (row-major, `m[row][col]`), or `null` if the
     *     header does not carry valid RAS information.
     */
    public float[][] computeVox2ras() {
        if (this.rasGoodFlag != 1 || this.Mdc.size() < 9 || this.Pxyz_c.size() < 3) {
            return null;
        }

        float[] sizes = { this.sizeX, this.sizeY, this.sizeZ };
        // The index of the center voxel, with integer division (matching FreeSurfer's mri_info).
        int[] cCrs = { this.dim1Size / 2, this.dim2Size / 2, this.dim3Size / 2 };

        float[][] m = new float[4][4];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Row j of the MGH Mdc matrix is the unit direction cosine of voxel axis j, so
                // affine[i][j] = size[j] * Mdc[j*3 + i].
                m[i][j] = sizes[j] * this.Mdc.get(j * 3 + i);
            }
        }
        // RAS of voxel (0,0,0) = RAS of the center voxel (Pxyz_c) - linear part * center index.
        for (int i = 0; i < 3; i++) {
            float centerContribution = 0.0f;
            for (int j = 0; j < 3; j++) {
                centerContribution += m[i][j] * cCrs[j];
            }
            m[i][3] = this.Pxyz_c.get(i) - centerContribution;
        }
        m[3][3] = 1.0f;
        return m;
    }

    /**
     * Set the RAS fields (`sizeX`/`sizeY`/`sizeZ`, `Mdc`, `Pxyz_c`) of this header from a
     * voxel-to-RAS affine matrix, and set `rasGoodFlag` to 1.
     *
     * This is the inverse of {@link #computeVox2ras()}: the voxel sizes are taken to be the norms
     * of the columns of the linear part, the rows of `Mdc` are set to the (normalized) column
     * directions (i.e. the unit direction cosines of the 3 volume axes), and `Pxyz_c` is set to
     * the RAS coordinate of the center voxel (the affine applied to the center voxel index, which
     * uses integer division `dim/2`).
     *
     * @param affine the voxel-to-RAS affine as a 4x4 float array (`affine[row][col]`, translation
     *     in `affine[i][3]`). If `null` or too small, `rasGoodFlag` is set to 0.
     */
    public void extractRasFromAffine(float[][] affine) {
        this.Mdc.clear();
        this.Pxyz_c.clear();
        if (affine == null || affine.length < 4 || affine[0].length < 4) {
            this.rasGoodFlag = 0;
            return;
        }

        // Voxel sizes are the norms of the columns of the linear part.
        float[] sizes = new float[3];
        float[][] linear = new float[3][3];
        for (int j = 0; j < 3; j++) {
            float norm = 0.0f;
            for (int i = 0; i < 3; i++) {
                linear[i][j] = affine[i][j];
                norm += affine[i][j] * affine[i][j];
            }
            sizes[j] = (float) Math.sqrt(norm);
        }

        // Mdc row j = direction of voxel axis j = normalized column j of the affine.
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                if (sizes[j] > 0.0f) {
                    this.Mdc.add(linear[i][j] / sizes[j]);
                }
                else {
                    // Degenerate column (zero voxel size): fall back to a unit vector along the world axis.
                    this.Mdc.add(i == j ? 1.0f : 0.0f);
                }
            }
        }
        for (int j = 0; j < 3; j++) {
            if (sizes[j] <= 0.0f) {
                sizes[j] = 1.0f;
            }
        }
        this.sizeX = sizes[0];
        this.sizeY = sizes[1];
        this.sizeZ = sizes[2];

        // Pxyz_c = translation + linear part * center voxel index (integer division).
        int[] cCrs = { this.dim1Size / 2, this.dim2Size / 2, this.dim3Size / 2 };
        for (int i = 0; i < 3; i++) {
            float centerContribution = 0.0f;
            for (int j = 0; j < 3; j++) {
                centerContribution += affine[i][j] * cCrs[j];
            }
            this.Pxyz_c.add(affine[i][3] + centerContribution);
        }
        this.rasGoodFlag = 1;
    }

}
