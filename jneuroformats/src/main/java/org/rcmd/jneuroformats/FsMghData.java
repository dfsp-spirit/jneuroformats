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

/**
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the structure, and a value that is assigned to all vertices or voxels that are part of the structure. Sometimes the value is not needed, and can be set to 0.
 */
public class FsMghData {

    public float[][][][] data_mri_float;
    public float[][][][] data_mri_int;
    public float[][][][] data_mri_short;
    public float[][][][] data_mri_uchar;

    public int mri_datatype = FsMgh.MRI_FLOAT;

    public FsMghData(int mri_datatype, int dim1size, int dim2size, int dim3size, int dim4size) {
        this.mri_datatype = mri_datatype;
        switch (mri_datatype) {
            case FsMgh.MRI_FLOAT:
                data_mri_float = new float[dim1size][dim2size][dim3size][dim4size];
                break;
            case FsMgh.MRI_INT:
                data_mri_int = new float[dim1size][dim2size][dim3size][dim4size];
                break;
            case FsMgh.MRI_SHORT:
                data_mri_short = new float[dim1size][dim2size][dim3size][dim4size];
                break;
            case FsMgh.MRI_UCHAR:
                data_mri_uchar = new float[dim1size][dim2size][dim3size][dim4size];
                break;
        }
    }

    public FsMghData() {
        this.mri_datatype = FsMgh.MRI_FLOAT;
        data_mri_float = new float[0][0][0][0];
    }

    public FsMghData(FsMghHeader header) {
        this.mri_datatype = header.mri_datatype;
        switch (mri_datatype) {
            case FsMgh.MRI_FLOAT:
                data_mri_float = new float[header.dim1size][header.dim2size][header.dim3size][header.dim4size];
                break;
            case FsMgh.MRI_INT:
                data_mri_int = new float[header.dim1size][header.dim2size][header.dim3size][header.dim4size];
                break;
            case FsMgh.MRI_SHORT:
                data_mri_short = new float[header.dim1size][header.dim2size][header.dim3size][header.dim4size];
                break;
            case FsMgh.MRI_UCHAR:
                data_mri_uchar = new float[header.dim1size][header.dim2size][header.dim3size][header.dim4size];
                break;
        }

    }

    /**
     * Read an FsMghHeader instance from a Buffer in FreeSurfer MGH format. Reads the header and advances the buffer to the data part of the file.
     * @param buf the buffer to read from.
     * @return an FsMghHeader instance.
     * @throws IOException if IO error occurs, or if the file is not in valid MGH format.
     */
    public static FsMghData fromByteBuffer(ByteBuffer buf, FsMghHeader header) throws IOException {
        FsMghData data = new FsMghData(header);

        if (header.mri_datatype == FsMgh.MRI_FLOAT) {
            for (int i = 0; i < header.dim1size; i++) {
                for (int j = 0; j < header.dim2size; j++) {
                    for (int k = 0; k < header.dim3size; k++) {
                        for (int l = 0; l < header.dim4size; l++) {
                            data.data_mri_float[i][j][k][l] = buf.getFloat();
                        }
                    }
                }
            }
        }
        else if (header.mri_datatype == FsMgh.MRI_INT) {
            for (int i = 0; i < header.dim1size; i++) {
                for (int j = 0; j < header.dim2size; j++) {
                    for (int k = 0; k < header.dim3size; k++) {
                        for (int l = 0; l < header.dim4size; l++) {
                            data.data_mri_int[i][j][k][l] = buf.getInt();
                        }
                    }
                }
            }
        }
        else if (header.mri_datatype == FsMgh.MRI_SHORT) {
            for (int i = 0; i < header.dim1size; i++) {
                for (int j = 0; j < header.dim2size; j++) {
                    for (int k = 0; k < header.dim3size; k++) {
                        for (int l = 0; l < header.dim4size; l++) {
                            data.data_mri_short[i][j][k][l] = buf.getShort();
                        }
                    }
                }
            }
        }
        else if (header.mri_datatype == FsMgh.MRI_UCHAR) {
            for (int i = 0; i < header.dim1size; i++) {
                for (int j = 0; j < header.dim2size; j++) {
                    for (int k = 0; k < header.dim3size; k++) {
                        for (int l = 0; l < header.dim4size; l++) {
                            data.data_mri_uchar[i][j][k][l] = buf.get();
                        }
                    }
                }
            }
        }

        return data;
    }

}