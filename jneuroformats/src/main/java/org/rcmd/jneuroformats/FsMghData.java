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
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the
 * structure, and a value that is assigned to all vertices or voxels that are
 * part of the structure. Sometimes the value is not needed, and can be set to
 * 0.
 */
public class FsMghData {

    public float[][][][] dataMriFloat;
    public float[][][][] dataMriInt;
    public float[][][][] dataMriShort;
    public float[][][][] dataMriUchar;

    public int mriDataType = FsMgh.MRI_FLOAT;

    public FsMghData(int mriDataType, int dim1Size, int dim2Size, int dim3Size, int dim4Size) {
        this.mriDataType = mriDataType;
        switch (mriDataType) {
            case FsMgh.MRI_FLOAT:
                this.dataMriFloat = new float[dim1Size][dim2Size][dim3Size][dim4Size];
                break;
            case FsMgh.MRI_INT:
                this.dataMriInt = new float[dim1Size][dim2Size][dim3Size][dim4Size];
                break;
            case FsMgh.MRI_SHORT:
                this.dataMriShort = new float[dim1Size][dim2Size][dim3Size][dim4Size];
                break;
            case FsMgh.MRI_UCHAR:
                this.dataMriUchar = new float[dim1Size][dim2Size][dim3Size][dim4Size];
                break;
        }
    }

    public FsMghData() {
        this.mriDataType = FsMgh.MRI_FLOAT;
        this.dataMriFloat = new float[0][0][0][0];
    }

    public FsMghData(FsMghHeader header) {
        this.mriDataType = header.mriDatatype;
        switch (mriDataType) {
            case FsMgh.MRI_FLOAT:
                this.dataMriFloat = new float[header.dim1Size][header.dim2Size][header.dim3Size][header.dim4Size];
                break;
            case FsMgh.MRI_INT:
                this.dataMriInt = new float[header.dim1Size][header.dim2Size][header.dim3Size][header.dim4Size];
                break;
            case FsMgh.MRI_SHORT:
                this.dataMriShort = new float[header.dim1Size][header.dim2Size][header.dim3Size][header.dim4Size];
                break;
            case FsMgh.MRI_UCHAR:
                this.dataMriUchar = new float[header.dim1Size][header.dim2Size][header.dim3Size][header.dim4Size];
                break;
        }

    }

    /**
     * Read an FsMghHeader instance from a Buffer in FreeSurfer MGH format. Reads
     * the header and advances the buffer to the data part of the file.
     *
     * @param buf the buffer to read from.
     * @return an FsMghHeader instance.
     * @throws IOException if IO error occurs, or if the file is not in valid MGH
     *                     format.
     */
    public static FsMghData fromByteBuffer(ByteBuffer buf, FsMghHeader header) throws IOException {
        FsMghData data = new FsMghData(header);

        if (header.mriDatatype == FsMgh.MRI_FLOAT) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            data.dataMriFloat[i][j][k][l] = buf.getFloat();
                        }
                    }
                }
            }
        }
        else if (header.mriDatatype == FsMgh.MRI_INT) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            data.dataMriInt[i][j][k][l] = buf.getInt();
                        }
                    }
                }
            }
        }
        else if (header.mriDatatype == FsMgh.MRI_SHORT) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            data.dataMriShort[i][j][k][l] = buf.getShort();
                        }
                    }
                }
            }
        }
        else if (header.mriDatatype == FsMgh.MRI_UCHAR) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            data.dataMriUchar[i][j][k][l] = buf.get();
                        }
                    }
                }
            }
        }

        return data;
    }

    /**
     * Write the FsMghData to a ByteBuffer.
     *
     * @param buf    an existing ByteBuffer to write to. If null, a new ByteBuffer
     *               will be created. Typically this should be an existing
     *               ByteBuffer filled partly, with the header bytes, and with
     *               enough space to hold the FsMghData.
     * @param header the FsMghHeader from which to obtain information on which data
     *               to write. This header will not be written to the ByteBuffer.
     * @return the ByteBuffer, with the FsMghData written to it.
     * @throws IOException if IO error occurs.
     */
    protected ByteBuffer writeFsMghDataToByteBuffer(ByteBuffer buf, FsMghHeader header) throws IOException {
        if (buf == null) {
            buf = ByteBuffer.allocate(header.getDataSizeInBytes());
        }

        if (this.mriDataType == FsMgh.MRI_FLOAT) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            buf.putFloat(this.dataMriFloat[i][j][k][l]);
                        }
                    }
                }
            }
        }
        else if (this.mriDataType == FsMgh.MRI_INT) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            buf.putInt((int) this.dataMriInt[i][j][k][l]);
                        }
                    }
                }
            }
        }
        else if (this.mriDataType == FsMgh.MRI_SHORT) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            buf.putShort((short) this.dataMriShort[i][j][k][l]);
                        }
                    }
                }
            }
        }
        else if (this.mriDataType == FsMgh.MRI_UCHAR) {
            for (int i = 0; i < header.dim1Size; i++) {
                for (int j = 0; j < header.dim2Size; j++) {
                    for (int k = 0; k < header.dim3Size; k++) {
                        for (int l = 0; l < header.dim4Size; l++) {
                            buf.put((byte) this.dataMriUchar[i][j][k][l]);
                        }
                    }
                }
            }
        }
        else {
            throw new IOException("Invalid MRI data type.");
        }
        return buf;
    }

}