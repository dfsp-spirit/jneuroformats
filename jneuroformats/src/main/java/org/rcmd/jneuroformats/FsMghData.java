

package org.rcmd.jneuroformats;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Files;


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
        switch(mri_datatype) {
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
        switch(mri_datatype) {
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
}