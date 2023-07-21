
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
public class FsMghHeader {
    public int mri_datatype = FsMgh.MRI_FLOAT;

    public int dim1size = 0;
    public int dim2size = 0;
    public int dim3size = 0;
    public int dim4size = 0;
}