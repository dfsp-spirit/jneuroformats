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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Class representing a volume stored in NIfTI-1 format.
 *
 * Only standard-compliant single-file NIfTI (.nii and .nii.gz) files are
 * supported. The FreeSurfer "hack" of storing surface data in NIfTI files is
 * explicitly not supported.
 *
 * The volume data is stored in an {@link FsMgh} instance (with an
 * {@link FsMghHeader} and {@link FsMghData}), which makes converting between
 * NIfTI and the FreeSurfer MGH/MGZ formats straightforward: read a NIfTI file
 * with {@link #read(Path)} and get the volume as an FsMgh via {@link #toMgh()},
 * or wrap an existing FsMgh via {@link #fromMgh(FsMgh)} and write it out with
 * {@link #write(Path)}.
 */
public class Nifti1 {

    /** The NIfTI-1 header of the file. */
    public Nifti1Header header;

    /** The volume data, in the MGH data model (header and 4D data). */
    public FsMgh mgh;

    /**
     * Default constructor for Nifti1. Initializes an empty header and volume.
     */
    public Nifti1() {
        this.header = new Nifti1Header();
        this.mgh = new FsMgh();
    }

    /**
     * Constructor for Nifti1 with header and volume.
     * @param header the Nifti1Header object.
     * @param mgh the FsMgh object holding the volume data.
     */
    public Nifti1(Nifti1Header header, FsMgh mgh) {
        this.header = header;
        this.mgh = mgh;
    }

    /**
     * Wrap an existing FsMgh volume so that it can be written as a NIfTI file.
     * @param mgh the volume to wrap.
     * @return a Nifti1 instance holding the volume.
     */
    public static Nifti1 fromMgh(FsMgh mgh) {
        Nifti1 nifti = new Nifti1();
        nifti.mgh = mgh;
        return nifti;
    }

    /**
     * Get the volume as an FsMgh instance. This is the volume read from a NIfTI
     * file, or the volume wrapped via {@link #fromMgh(FsMgh)}.
     * @return the volume data.
     */
    public FsMgh toMgh() {
        return this.mgh;
    }

    /**
     * Read a file in NIfTI-1 format and return a Nifti1 object. The file format
     * is determined from the file extension (.nii or .nii.gz).
     * @param filePath the name of the file to read, as a Path object.
     * @return a Nifti1 object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #readFormat(Path, String) if you want to read a file and specify the format.
     */
    public static Nifti1 read(Path filePath) throws IOException, FileNotFoundException {
        return Nifti1.readFormat(filePath, "auto");
    }

    /**
     * Read a file in NIfTI-1 format and return a Nifti1 object.
     * @param filePath the name of the file to read, as a Path object.
     * @param format the file format to read, either "nifti" (or "nii") or "auto" to auto-detect from the file name.
     * @return a Nifti1 object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     * @see #read(Path) if you want to read a file without specifying the format.
     */
    public static Nifti1 readFormat(Path filePath, String format) throws IOException, FileNotFoundException {

        String formatLower = format.toLowerCase();
        if (!(formatLower.equals("auto") || formatLower.equals("nifti") || formatLower.equals("nii"))) {
            throw new IOException(MessageFormat.format("Unknown NIfTI file format {0}.", format));
        }

        String fileNameLower = filePath.getFileName().toString().toLowerCase();
        boolean isGzip = fileNameLower.endsWith(".nii.gz");
        boolean isNifti = fileNameLower.endsWith(".nii") || isGzip;
        if (formatLower.equals("auto") && !isNifti) {
            throw new IOException(MessageFormat.format("Cannot determine NIfTI file format for file {0} from name: unknown file extension.",
                    filePath.getFileName().toString()));
        }

        byte[] bytes;
        if (isGzip) {
            bytes = readAllBytesGzip(filePath);
        }
        else {
            bytes = Files.readAllBytes(filePath);
        }

        if (bytes.length < Nifti1Header.HEADER_SIZE_IN_BYTES) {
            throw new IOException("NIfTI file too small for header: expected at least " + Nifti1Header.HEADER_SIZE_IN_BYTES + " bytes.");
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        ByteOrder byteOrder = detectByteOrder(buf);
        buf.order(byteOrder);

        Nifti1Header header = Nifti1Header.fromByteBuffer(buf);

        // Validate magic.
        if (Nifti1Header.magicIs(header.magic, "ni1\0")) {
            throw new IOException("NIfTI files split into separate .hdr and .img files are not supported. Only single-file .nii files are supported.");
        }
        if (!Nifti1Header.magicIs(header.magic, "n+1\0")) {
            throw new IOException("NIfTI file has invalid magic string. Only single-file .nii files are supported.");
        }

        // Map dimensions. dim[i] for i > dim[0] is usually 1 in standard files, but some writers use 0.
        int dim1 = header.dim[1];
        int dim2 = (header.dim[2] > 0) ? header.dim[2] : 1;
        int dim3 = (header.dim[3] > 0) ? header.dim[3] : 1;
        int dim4 = (header.dim[4] > 0) ? header.dim[4] : 1;
        if (dim1 <= 0) {
            throw new IOException(MessageFormat.format(
                    "Invalid NIfTI file: dim[1] = {0}. Note that FreeSurfer surface data stored in NIfTI files (the FreeSurfer hack) is not supported.",
                    dim1));
        }

        int mriType = niftiDtypeToMri(header.datatype);

        FsMgh mgh = new FsMgh();
        mgh.header.dim1Size = dim1;
        mgh.header.dim2Size = dim2;
        mgh.header.dim3Size = dim3;
        mgh.header.dim4Size = dim4;
        mgh.header.mriDatatype = mriType;
        mgh.header.dof = 0;
        mgh.data = new FsMghData(mgh.header);

        // Extract the voxel-to-world transform (sform preferred, qform as fallback).
        extractRas(header, mgh.header);

        int voxOffset = (int) header.voxOffset;
        if (voxOffset < Nifti1Header.HEADER_SIZE_IN_BYTES) {
            throw new IOException(MessageFormat.format("Invalid NIfTI file: vox_offset = {0} is smaller than the header size.", voxOffset));
        }
        buf.position(voxOffset);

        // Apply the scaling (scl_slope / scl_inter) to the raw values.
        float slope = (header.sclSlope != 0.0f) ? header.sclSlope : 1.0f;
        float inter = header.sclInter;

        readData(buf, mgh, slope, inter);

        return new Nifti1(header, mgh);
    }

    /**
     * Write this volume to a file in NIfTI-1 format (single-file .nii, big-endian byte order).
     * @param filePath the path to the file to write to
     * @throws IOException if IO error occurs.
     */
    public void write(Path filePath) throws IOException {

        FsMghHeader mh = this.mgh.header;

        // NIfTI-1 stores dimensions as int16, so they must fit.
        if (mh.dim1Size > 32767 || mh.dim2Size > 32767 || mh.dim3Size > 32767 || mh.dim4Size > 32767) {
            throw new IOException("MGH dimensions exceed the NIfTI-1 int16 limit (32767). Cannot write as NIfTI.");
        }

        Nifti1Header h = new Nifti1Header();
        h.sizeofHdr = Nifti1Header.HEADER_SIZE_IN_BYTES;
        h.dim[0] = 4;
        h.dim[1] = (short) mh.dim1Size;
        h.dim[2] = (short) mh.dim2Size;
        h.dim[3] = (short) mh.dim3Size;
        h.dim[4] = (short) mh.dim4Size;
        h.dim[5] = 1;
        h.dim[6] = 1;
        h.dim[7] = 1;

        h.datatype = mriDtypeToNifti(mh.mriDatatype);
        switch (mh.mriDatatype) {
            case FsMgh.MRI_UCHAR:
                h.bitpix = 8;
                break;
            case FsMgh.MRI_SHORT:
                h.bitpix = 16;
                break;
            case FsMgh.MRI_INT:
                h.bitpix = 32;
                break;
            case FsMgh.MRI_FLOAT:
                h.bitpix = 32;
                break;
            default:
                throw new IOException("Invalid MRI data type " + mh.mriDatatype + " for NIfTI output.");
        }

        h.pixdim[0] = 1.0f;
        h.pixdim[1] = (mh.sizeX > 0.0f) ? mh.sizeX : 1.0f;
        h.pixdim[2] = (mh.sizeY > 0.0f) ? mh.sizeY : 1.0f;
        h.pixdim[3] = (mh.sizeZ > 0.0f) ? mh.sizeZ : 1.0f;
        h.pixdim[4] = 1.0f;
        h.pixdim[5] = 1.0f;
        h.pixdim[6] = 1.0f;
        h.pixdim[7] = 1.0f;

        h.voxOffset = Nifti1Header.HEADER_PLUS_EXT_SIZE_IN_BYTES;
        h.sclSlope = 1.0f;
        h.sclInter = 0.0f;

        if (mh.rasGoodFlag == 1 && mh.Mdc.size() >= 9 && mh.Pxyz_c.size() >= 3) {
            float sx = h.pixdim[1];
            float sy = h.pixdim[2];
            float sz = h.pixdim[3];

            // sform: the vox2ras affine = Mdc * diag(size), translation = Pxyz_c.
            h.sformCode = Nifti1Header.XFORM_SCANNER_ANAT;
            h.srowX[0] = mh.Mdc.get(0) * sx;
            h.srowX[1] = mh.Mdc.get(1) * sy;
            h.srowX[2] = mh.Mdc.get(2) * sz;
            h.srowX[3] = mh.Pxyz_c.get(0);
            h.srowY[0] = mh.Mdc.get(3) * sx;
            h.srowY[1] = mh.Mdc.get(4) * sy;
            h.srowY[2] = mh.Mdc.get(5) * sz;
            h.srowY[3] = mh.Pxyz_c.get(1);
            h.srowZ[0] = mh.Mdc.get(6) * sx;
            h.srowZ[1] = mh.Mdc.get(7) * sy;
            h.srowZ[2] = mh.Mdc.get(8) * sz;
            h.srowZ[3] = mh.Pxyz_c.get(2);

            // qform: encode the same transform as a quaternion.
            h.qformCode = Nifti1Header.XFORM_SCANNER_ANAT;
            float qfac = determinant3x3(mh.Mdc) < 0.0f ? -1.0f : 1.0f;
            h.pixdim[0] = qfac;
            float[][] rotation = new float[3][3];
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    rotation[row][col] = mh.Mdc.get(row * 3 + col);
                }
            }
            if (qfac < 0.0f) {
                // Make the rotation proper (det = +1) by flipping the z column; qfac encodes the flip.
                for (int row = 0; row < 3; row++) {
                    rotation[row][2] = -rotation[row][2];
                }
            }
            float[] quat = rotationMatrixToQuaternion(rotation);
            h.quaternB = quat[1];
            h.quaternC = quat[2];
            h.quaternD = quat[3];
            h.qoffsetX = mh.Pxyz_c.get(0);
            h.qoffsetY = mh.Pxyz_c.get(1);
            h.qoffsetZ = mh.Pxyz_c.get(2);
        }
        else {
            h.sformCode = Nifti1Header.XFORM_UNKNOWN;
            h.qformCode = Nifti1Header.XFORM_UNKNOWN;
        }

        h.magic[0] = 'n';
        h.magic[1] = '+';
        h.magic[2] = '1';
        h.magic[3] = 0;

        int dataSizeInBytes = mh.getNumValues() * bytesPerValue(mh.mriDatatype);
        ByteBuffer buf = ByteBuffer.allocate(Nifti1Header.HEADER_PLUS_EXT_SIZE_IN_BYTES + dataSizeInBytes);
        buf.order(ByteOrder.BIG_ENDIAN);

        h.writeToByteBuffer(buf); // writes the 348-byte header, advances position to 348.
        buf.putInt(Nifti1Header.HEADER_SIZE_IN_BYTES, 0); // 4-byte extension indicator: 0 = no extensions.
        buf.position(Nifti1Header.HEADER_PLUS_EXT_SIZE_IN_BYTES);

        writeData(buf, this.mgh);

        IO.writeFile(filePath, buf);
    }

    /**
     * Detect the byte order of a NIfTI file from its sizeof_hdr field.
     * @param buf the buffer containing the file bytes.
     * @return the byte order of the file.
     * @throws IOException if sizeof_hdr is not 348 in either byte order.
     */
    protected static ByteOrder detectByteOrder(ByteBuffer buf) throws IOException {
        int sizeofHdrLittleEndian = buf.order(ByteOrder.LITTLE_ENDIAN).getInt(0);
        if (sizeofHdrLittleEndian == Nifti1Header.HEADER_SIZE_IN_BYTES) {
            return ByteOrder.LITTLE_ENDIAN;
        }
        int sizeofHdrBigEndian = buf.order(ByteOrder.BIG_ENDIAN).getInt(0);
        if (sizeofHdrBigEndian == Nifti1Header.HEADER_SIZE_IN_BYTES) {
            return ByteOrder.BIG_ENDIAN;
        }
        throw new IOException(MessageFormat.format("Invalid NIfTI file: sizeof_hdr = {0} (expected {1}).",
                sizeofHdrLittleEndian, Nifti1Header.HEADER_SIZE_IN_BYTES));
    }

    /**
     * Map a NIfTI-1 data type code to an MGH MRI_* data type constant.
     * @param niftiDtype the NIfTI-1 data type code.
     * @return the MGH MRI_* data type constant.
     * @throws IOException if the NIfTI data type is not supported.
     */
    protected static int niftiDtypeToMri(short niftiDtype) throws IOException {
        switch (niftiDtype) {
            case Nifti1Header.DT_UINT8:
                return FsMgh.MRI_UCHAR;
            case Nifti1Header.DT_INT16:
                return FsMgh.MRI_SHORT;
            case Nifti1Header.DT_INT32:
                return FsMgh.MRI_INT;
            case Nifti1Header.DT_FLOAT32:
                return FsMgh.MRI_FLOAT;
            default:
                throw new IOException(MessageFormat.format(
                        "Unsupported NIfTI data type {0}. Supported types: UINT8 (2), INT16 (4), INT32 (8), FLOAT32 (16).", niftiDtype));
        }
    }

    /**
     * Map an MGH MRI_* data type constant to a NIfTI-1 data type code.
     * @param mriType the MGH MRI_* data type constant.
     * @return the NIfTI-1 data type code.
     * @throws IOException if the MGH data type is not supported for NIfTI output.
     */
    protected static short mriDtypeToNifti(int mriType) throws IOException {
        switch (mriType) {
            case FsMgh.MRI_UCHAR:
                return Nifti1Header.DT_UINT8;
            case FsMgh.MRI_SHORT:
                return Nifti1Header.DT_INT16;
            case FsMgh.MRI_INT:
                return Nifti1Header.DT_INT32;
            case FsMgh.MRI_FLOAT:
                return Nifti1Header.DT_FLOAT32;
            default:
                throw new IOException(MessageFormat.format("Unsupported MGH data type {0} for NIfTI output.", mriType));
        }
    }

    /**
     * Extract the voxel-to-world (RAS) transform from a NIfTI-1 header into an MGH header.
     * Prefers the sform (per the NIfTI-1 standard); falls back to the qform.
     * @param h the NIfTI-1 header.
     * @param mghHeader the MGH header to fill.
     */
    protected static void extractRas(Nifti1Header h, FsMghHeader mghHeader) {

        float sx = h.pixdim[1];
        float sy = h.pixdim[2];
        float sz = h.pixdim[3];

        mghHeader.sizeX = sx;
        mghHeader.sizeY = sy;
        mghHeader.sizeZ = sz;
        mghHeader.Mdc.clear();
        mghHeader.Pxyz_c.clear();

        if (h.sformCode > 0) {
            // The sform maps voxel indices to world coordinates: column j = world vector of one step in voxel dimension j.
            // MGH stores Mdc as the pure rotation (direction cosines), i.e., the sform columns normalized by the voxel sizes.
            mghHeader.rasGoodFlag = 1;
            mghHeader.Mdc.add(divideOrOne(h.srowX[0], sx));
            mghHeader.Mdc.add(divideOrOne(h.srowX[1], sy));
            mghHeader.Mdc.add(divideOrOne(h.srowX[2], sz));
            mghHeader.Mdc.add(divideOrOne(h.srowY[0], sx));
            mghHeader.Mdc.add(divideOrOne(h.srowY[1], sy));
            mghHeader.Mdc.add(divideOrOne(h.srowY[2], sz));
            mghHeader.Mdc.add(divideOrOne(h.srowZ[0], sx));
            mghHeader.Mdc.add(divideOrOne(h.srowZ[1], sy));
            mghHeader.Mdc.add(divideOrOne(h.srowZ[2], sz));
            mghHeader.Pxyz_c.add(h.srowX[3]);
            mghHeader.Pxyz_c.add(h.srowY[3]);
            mghHeader.Pxyz_c.add(h.srowZ[3]);
        }
        else if (h.qformCode > 0) {
            // Compute the rotation matrix from the quaternion.
            float b = h.quaternB;
            float c = h.quaternC;
            float d = h.quaternD;
            float a = (float) Math.sqrt(Math.max(0.0f, 1.0f - (b * b + c * c + d * d)));
            float qfac = (h.pixdim[0] < 0.0f) ? -1.0f : 1.0f;

            float R11 = a * a + b * b - c * c - d * d;
            float R12 = 2.0f * (b * c - a * d);
            float R13 = 2.0f * (b * d + a * c);
            float R21 = 2.0f * (b * c + a * d);
            float R22 = a * a + c * c - b * b - d * d;
            float R23 = 2.0f * (c * d - a * b);
            float R31 = 2.0f * (b * d - a * c);
            float R32 = 2.0f * (c * d + a * b);
            float R33 = a * a + d * d - b * b - c * c;

            mghHeader.rasGoodFlag = 1;
            // Mdc = R, with the third column scaled by qfac to encode the possible reflection.
            mghHeader.Mdc.add(R11);
            mghHeader.Mdc.add(R12);
            mghHeader.Mdc.add(R13 * qfac);
            mghHeader.Mdc.add(R21);
            mghHeader.Mdc.add(R22);
            mghHeader.Mdc.add(R23 * qfac);
            mghHeader.Mdc.add(R31);
            mghHeader.Mdc.add(R32);
            mghHeader.Mdc.add(R33 * qfac);
            mghHeader.Pxyz_c.add(h.qoffsetX);
            mghHeader.Pxyz_c.add(h.qoffsetY);
            mghHeader.Pxyz_c.add(h.qoffsetZ);
        }
        else {
            mghHeader.rasGoodFlag = 0;
        }
    }

    /**
     * Read the voxel data from a NIfTI file and apply scaling.
     * @param buf the buffer, positioned at the start of the voxel data.
     * @param mgh the volume to fill.
     * @param slope the scaling slope.
     * @param inter the scaling intercept.
     * @throws IOException if IO error occurs.
     */
    protected static void readData(ByteBuffer buf, FsMgh mgh, float slope, float inter) throws IOException {

        int dim1 = mgh.header.dim1Size;
        int dim2 = mgh.header.dim2Size;
        int dim3 = mgh.header.dim3Size;
        int dim4 = mgh.header.dim4Size;

        switch (mgh.header.mriDatatype) {
            case FsMgh.MRI_FLOAT:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                mgh.data.dataMriFloat[i][j][k][l] = buf.getFloat() * slope + inter;
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_INT:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                mgh.data.dataMriInt[i][j][k][l] = Math.round(buf.getInt() * slope + inter);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_SHORT:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                mgh.data.dataMriShort[i][j][k][l] = (short) Math.round(buf.getShort() * slope + inter);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_UCHAR:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                float value = Math.round((buf.get() & 0xFF) * slope + inter);
                                mgh.data.dataMriUchar[i][j][k][l] = Math.max(0.0f, Math.min(255.0f, value));
                            }
                        }
                    }
                }
                break;
            default:
                throw new IOException("Invalid MRI data type " + mgh.header.mriDatatype + ".");
        }
    }

    /**
     * Write the voxel data from an FsMgh volume to a ByteBuffer, in NIfTI order
     * (the last dimension varies fastest, same as in MGH).
     * @param buf the buffer, positioned at the start of the voxel data area.
     * @param mgh the volume to write.
     * @throws IOException if IO error occurs.
     */
    protected static void writeData(ByteBuffer buf, FsMgh mgh) throws IOException {

        int dim1 = mgh.header.dim1Size;
        int dim2 = mgh.header.dim2Size;
        int dim3 = mgh.header.dim3Size;
        int dim4 = mgh.header.dim4Size;

        switch (mgh.data.mriDataType) {
            case FsMgh.MRI_FLOAT:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                buf.putFloat(mgh.data.dataMriFloat[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_INT:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                buf.putInt((int) mgh.data.dataMriInt[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_SHORT:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                buf.putShort((short) mgh.data.dataMriShort[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_UCHAR:
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        for (int k = 0; k < dim3; k++) {
                            for (int l = 0; l < dim4; l++) {
                                buf.put((byte) mgh.data.dataMriUchar[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            default:
                throw new IOException("Invalid MRI data type " + mgh.data.mriDataType + ".");
        }
    }

    /**
     * Divide, guarding against a zero divisor (returns the numerator unchanged in that case).
     * @param numerator the numerator.
     * @param divisor the divisor.
     * @return numerator / divisor, or numerator if divisor is 0.
     */
    private static float divideOrOne(float numerator, float divisor) {
        if (divisor == 0.0f) {
            return numerator;
        }
        return numerator / divisor;
    }

    /**
     * Compute the determinant of a 3x3 matrix given in row-major order.
     * @param m the 9 matrix entries, in row-major order.
     * @return the determinant.
     */
    protected static float determinant3x3(List<Float> m) {
        return m.get(0) * (m.get(4) * m.get(8) - m.get(5) * m.get(7)) -
                m.get(1) * (m.get(3) * m.get(8) - m.get(5) * m.get(6)) +
                m.get(2) * (m.get(3) * m.get(7) - m.get(4) * m.get(6));
    }

    /**
     * Convert a 3x3 rotation matrix (proper rotation, det = +1) to a quaternion.
     * @param r the rotation matrix, as a 3x3 array of floats.
     * @return the quaternion as a float array [a, b, c, d], where a is the scalar part and b, c, d the vector parts.
     */
    protected static float[] rotationMatrixToQuaternion(float[][] r) {
        float trace = r[0][0] + r[1][1] + r[2][2];
        float a;
        float b;
        float c;
        float d;
        if (trace > 0.0f) {
            float s = (float) Math.sqrt(trace + 1.0f) * 2.0f;
            a = 0.25f * s;
            b = (r[2][1] - r[1][2]) / s;
            c = (r[0][2] - r[2][0]) / s;
            d = (r[1][0] - r[0][1]) / s;
        }
        else if (r[0][0] > r[1][1] && r[0][0] > r[2][2]) {
            float s = (float) Math.sqrt(1.0f + r[0][0] - r[1][1] - r[2][2]) * 2.0f;
            a = (r[2][1] - r[1][2]) / s;
            b = 0.25f * s;
            c = (r[0][1] + r[1][0]) / s;
            d = (r[0][2] + r[2][0]) / s;
        }
        else if (r[1][1] > r[2][2]) {
            float s = (float) Math.sqrt(1.0f + r[1][1] - r[0][0] - r[2][2]) * 2.0f;
            a = (r[0][2] - r[2][0]) / s;
            b = (r[0][1] + r[1][0]) / s;
            c = 0.25f * s;
            d = (r[1][2] + r[2][1]) / s;
        }
        else {
            float s = (float) Math.sqrt(1.0f + r[2][2] - r[0][0] - r[1][1]) * 2.0f;
            a = (r[1][0] - r[0][1]) / s;
            b = (r[0][2] + r[2][0]) / s;
            c = (r[1][2] + r[2][1]) / s;
            d = 0.25f * s;
        }
        return new float[]{ a, b, c, d };
    }

    /**
     * Get the number of bytes per voxel for an MGH MRI_* data type.
     * @param mriType the MGH MRI_* data type.
     * @return the number of bytes per voxel.
     * @throws IOException if the data type is invalid.
     */
    private static int bytesPerValue(int mriType) throws IOException {
        switch (mriType) {
            case FsMgh.MRI_UCHAR:
                return 1;
            case FsMgh.MRI_SHORT:
                return 2;
            case FsMgh.MRI_INT:
                return 4;
            case FsMgh.MRI_FLOAT:
                return 4;
            default:
                throw new IOException("Invalid MRI data type " + mriType + ".");
        }
    }

    /**
     * Read the full contents of a gzipped file into a byte array.
     * @param filePath the path to the file.
     * @return the decompressed bytes.
     * @throws IOException if IO error occurs.
     */
    private static byte[] readAllBytesGzip(Path filePath) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gzis = new GZIPInputStream(Files.newInputStream(filePath))) {
            byte[] buffer = new byte[65536];
            int numRead;
            while ((numRead = gzis.read(buffer)) != -1) {
                baos.write(buffer, 0, numRead);
            }
        }
        return baos.toByteArray();
    }

}
