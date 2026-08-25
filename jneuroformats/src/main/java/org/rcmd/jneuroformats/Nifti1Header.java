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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Models the header of a NIfTI-1 volume file (the single-file .nii variant).
 *
 * The header is exactly 348 bytes long and contains, among others, the volume
 * dimensions, the voxel data type, the voxel sizes (pixdim), and the spatial
 * transform from voxel to world coordinates, given either as an affine
 * transform (sform) or as a quaternion (qform).
 *
 * Only standard-compliant NIfTI-1 files are supported. The FreeSurfer "hack"
 * of storing surface data in NIfTI files (using dim[1] as a signed vertex
 * count) is not supported.
 */
public class Nifti1Header {

    /** NIfTI-1 data type code for unsigned 8-bit integer data. */
    public static final short DT_UINT8 = 2;

    /** NIfTI-1 data type code for signed 16-bit integer data. */
    public static final short DT_INT16 = 4;

    /** NIfTI-1 data type code for signed 32-bit integer data. */
    public static final short DT_INT32 = 8;

    /** NIfTI-1 data type code for 32-bit float data. */
    public static final short DT_FLOAT32 = 16;

    /** NIfTI-1 xform code meaning "no transform". */
    public static final short XFORM_UNKNOWN = 0;

    /** NIfTI-1 xform code meaning "Scanner Anat". */
    public static final short XFORM_SCANNER_ANAT = 1;

    /** The size of the NIfTI-1 header, in bytes. */
    public static final int HEADER_SIZE_IN_BYTES = 348;

    /** The size of the header including the 4-byte extension indicator. */
    public static final int HEADER_PLUS_EXT_SIZE_IN_BYTES = 352;

    /** The value of the sizeof_hdr field, always 348. */
    public int sizeofHdr = 348;

    /** Unused in the NIfTI-1 standard. */
    public byte[] dataType = new byte[10];

    /** Unused in the NIfTI-1 standard. */
    public byte[] dbName = new byte[18];

    /** Unused in the NIfTI-1 standard. */
    public int extents = 0;

    /** Unused in the NIfTI-1 standard. */
    public short sessionError = 0;

    /** Unused in the NIfTI-1 standard. */
    public byte regular = 0;

    /** Slice ordering info, unused here. */
    public byte dimInfo = 0;

    /** Volume dimensions: dim[0] = number of dimensions, dim[1..7] = sizes. */
    public short[] dim = new short[8];

    /** Intent parameters, unused here. */
    public float intentP1 = 0.0f;

    /** Intent parameters, unused here. */
    public float intentP2 = 0.0f;

    /** Intent parameters, unused here. */
    public float intentP3 = 0.0f;

    /** Intent code, unused here. */
    public short intentCode = 0;

    /** The NIfTI-1 voxel data type, one of the DT_* constants. */
    public short datatype = 0;

    /** The number of bits per voxel. */
    public short bitpix = 0;

    /** First slice index, unused here. */
    public short sliceStart = 0;

    /** Voxel sizes: pixdim[0] = qfac, pixdim[1..3] = voxel sizes in mm, pixdim[4..7] unused. */
    public float[] pixdim = new float[8];

    /** Byte offset to the start of the voxel data, relative to the start of the file. */
    public float voxOffset = 352.0f;

    /** Scaling slope. A value of 0 means no scaling (interpreted as 1). */
    public float sclSlope = 1.0f;

    /** Scaling intercept. */
    public float sclInter = 0.0f;

    /** Last slice index, unused here. */
    public short sliceEnd = 0;

    /** Slice timing code, unused here. */
    public byte sliceCode = 0;

    /** Units of the pixdim values, unused here. */
    public byte xyztUnits = 0;

    /** Calibration maximum, unused here. */
    public float calMax = 0.0f;

    /** Calibration minimum, unused here. */
    public float calMin = 0.0f;

    /** Slice duration, unused here. */
    public float sliceDuration = 0.0f;

    /** Time offset, unused here. */
    public float toffset = 0.0f;

    /** Global maximum, unused here. */
    public int glmax = 0;

    /** Global minimum, unused here. */
    public int glmin = 0;

    /** Description of the data. */
    public byte[] descrip = new byte[80];

    /** Auxiliary file name, unused here. */
    public byte[] auxFile = new byte[24];

    /** Transform code for the qform, e.g. XFORM_SCANNER_ANAT. A value > 0 means the qform is valid. */
    public short qformCode = 0;

    /** Transform code for the sform, e.g. XFORM_SCANNER_ANAT. A value > 0 means the sform is valid. */
    public short sformCode = 0;

    /** Quaternion parameter b. */
    public float quaternB = 0.0f;

    /** Quaternion parameter c. */
    public float quaternC = 0.0f;

    /** Quaternion parameter d. */
    public float quaternD = 0.0f;

    /** Quaternion x offset. */
    public float qoffsetX = 0.0f;

    /** Quaternion y offset. */
    public float qoffsetY = 0.0f;

    /** Quaternion z offset. */
    public float qoffsetZ = 0.0f;

    /** First row of the affine (sform) transform, [x1, x2, x3, tx]. */
    public float[] srowX = new float[4];

    /** Second row of the affine (sform) transform, [y1, y2, y3, ty]. */
    public float[] srowY = new float[4];

    /** Third row of the affine (sform) transform, [z1, z2, z3, tz]. */
    public float[] srowZ = new float[4];

    /** Intent name, unused here. */
    public byte[] intentName = new byte[16];

    /** The magic string, "n+1\0" for single-file NIfTI. */
    public byte[] magic = new byte[4];

    /**
     * Default constructor for Nifti1Header. Initializes the magic string to "n+1\0".
     */
    public Nifti1Header() {
        this.magic[0] = 'n';
        this.magic[1] = '+';
        this.magic[2] = '1';
        this.magic[3] = 0;
    }

    /**
     * Read a Nifti1Header instance from a ByteBuffer that contains the first 348 bytes of a NIfTI file.
     * @param buf the buffer to read from. The byte order of the buffer must already be set correctly.
     * @return a Nifti1Header instance.
     * @throws IOException if IO error occurs.
     */
    public static Nifti1Header fromByteBuffer(ByteBuffer buf) throws IOException {

        if (buf.remaining() < HEADER_SIZE_IN_BYTES) {
            throw new IOException("NIfTI file too small for header: expected at least " + HEADER_SIZE_IN_BYTES + " bytes.");
        }

        Nifti1Header header = new Nifti1Header();

        header.sizeofHdr = buf.getInt(0);
        buf.position(4);
        buf.get(header.dataType);
        buf.position(14);
        buf.get(header.dbName);
        header.extents = buf.getInt(32);
        header.sessionError = buf.getShort(36);
        header.regular = buf.get(38);
        header.dimInfo = buf.get(39);
        for (int i = 0; i < 8; i++) {
            header.dim[i] = buf.getShort(40 + i * 2);
        }
        header.intentP1 = buf.getFloat(56);
        header.intentP2 = buf.getFloat(60);
        header.intentP3 = buf.getFloat(64);
        header.intentCode = buf.getShort(68);
        header.datatype = buf.getShort(70);
        header.bitpix = buf.getShort(72);
        header.sliceStart = buf.getShort(74);
        for (int i = 0; i < 8; i++) {
            header.pixdim[i] = buf.getFloat(76 + i * 4);
        }
        header.voxOffset = buf.getFloat(108);
        header.sclSlope = buf.getFloat(112);
        header.sclInter = buf.getFloat(116);
        header.sliceEnd = buf.getShort(120);
        header.sliceCode = buf.get(122);
        header.xyztUnits = buf.get(123);
        header.calMax = buf.getFloat(124);
        header.calMin = buf.getFloat(128);
        header.sliceDuration = buf.getFloat(132);
        header.toffset = buf.getFloat(136);
        header.glmax = buf.getInt(140);
        header.glmin = buf.getInt(144);
        buf.position(148);
        buf.get(header.descrip);
        buf.position(228);
        buf.get(header.auxFile);
        header.qformCode = buf.getShort(252);
        header.sformCode = buf.getShort(254);
        header.quaternB = buf.getFloat(256);
        header.quaternC = buf.getFloat(260);
        header.quaternD = buf.getFloat(264);
        header.qoffsetX = buf.getFloat(268);
        header.qoffsetY = buf.getFloat(272);
        header.qoffsetZ = buf.getFloat(276);
        for (int i = 0; i < 4; i++) {
            header.srowX[i] = buf.getFloat(280 + i * 4);
        }
        for (int i = 0; i < 4; i++) {
            header.srowY[i] = buf.getFloat(296 + i * 4);
        }
        for (int i = 0; i < 4; i++) {
            header.srowZ[i] = buf.getFloat(312 + i * 4);
        }
        buf.position(328);
        buf.get(header.intentName);
        buf.position(344);
        buf.get(header.magic);

        return header;
    }

    /**
     * Write this Nifti1Header to a ByteBuffer, in the byte order of the buffer.
     * @param buf an existing ByteBuffer to write to. If null, a new ByteBuffer will be created.
     * @return the ByteBuffer, with the Nifti1Header written to it.
     */
    protected ByteBuffer writeToByteBuffer(ByteBuffer buf) {
        if (buf == null) {
            buf = ByteBuffer.allocate(HEADER_SIZE_IN_BYTES);
        }

        buf.putInt(0, this.sizeofHdr);
        buf.position(4);
        buf.put(this.dataType);
        buf.position(14);
        buf.put(this.dbName);
        buf.putInt(32, this.extents);
        buf.putShort(36, this.sessionError);
        buf.put(38, this.regular);
        buf.put(39, this.dimInfo);
        for (int i = 0; i < 8; i++) {
            buf.putShort(40 + i * 2, this.dim[i]);
        }
        buf.putFloat(56, this.intentP1);
        buf.putFloat(60, this.intentP2);
        buf.putFloat(64, this.intentP3);
        buf.putShort(68, this.intentCode);
        buf.putShort(70, this.datatype);
        buf.putShort(72, this.bitpix);
        buf.putShort(74, this.sliceStart);
        for (int i = 0; i < 8; i++) {
            buf.putFloat(76 + i * 4, this.pixdim[i]);
        }
        buf.putFloat(108, this.voxOffset);
        buf.putFloat(112, this.sclSlope);
        buf.putFloat(116, this.sclInter);
        buf.putShort(120, this.sliceEnd);
        buf.put(122, this.sliceCode);
        buf.put(123, this.xyztUnits);
        buf.putFloat(124, this.calMax);
        buf.putFloat(128, this.calMin);
        buf.putFloat(132, this.sliceDuration);
        buf.putFloat(136, this.toffset);
        buf.putInt(140, this.glmax);
        buf.putInt(144, this.glmin);
        buf.position(148);
        buf.put(this.descrip);
        buf.position(228);
        buf.put(this.auxFile);
        buf.putShort(252, this.qformCode);
        buf.putShort(254, this.sformCode);
        buf.putFloat(256, this.quaternB);
        buf.putFloat(260, this.quaternC);
        buf.putFloat(264, this.quaternD);
        buf.putFloat(268, this.qoffsetX);
        buf.putFloat(272, this.qoffsetY);
        buf.putFloat(276, this.qoffsetZ);
        for (int i = 0; i < 4; i++) {
            buf.putFloat(280 + i * 4, this.srowX[i]);
        }
        for (int i = 0; i < 4; i++) {
            buf.putFloat(296 + i * 4, this.srowY[i]);
        }
        for (int i = 0; i < 4; i++) {
            buf.putFloat(312 + i * 4, this.srowZ[i]);
        }
        buf.position(328);
        buf.put(this.intentName);
        buf.position(344);
        buf.put(this.magic);

        return buf;
    }

    /**
     * Check whether the magic string matches a given value.
     * @param magic the magic bytes.
     * @param expected the expected string, e.g. "n+1\0".
     * @return true if the magic matches.
     */
    public static boolean magicIs(byte[] magic, String expected) {
        byte[] expectedBytes = expected.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        if (magic.length != expectedBytes.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != expectedBytes[i]) {
                return false;
            }
        }
        return true;
    }

}
