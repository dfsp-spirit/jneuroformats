/**
*  This file is part of jneuroformats and subject to its license.
*  See the LICENSE file that accompanies this distribution, or visit
*  the project homepage at https://github.com/dfsp-spirit/jneuroformats.
*/

package org.rcmd.jneuroformats;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class FsMghTest {

   @Test
   public void oneCanConstructEmptyFsMghInstances() {
       FsMgh mgh = new FsMgh();
       assertThat(mgh.header.dim1size).isEqualTo(0);
   }

   @Test
    public void oneCanReadOurDemoMghFile() {

        Path mghFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgh");
        FsMgh brain;
        try {
            brain = FsMgh.fromFsMghFile(mghFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(brain.header.dim1size).isEqualTo(256);
        assertThat(brain.header.dim2size).isEqualTo(256);
        assertThat(brain.header.dim3size).isEqualTo(256);
        assertThat(brain.header.dim4size).isEqualTo(1);

        assertThat(brain.header.mri_datatype).isEqualTo(FsMgh.MRI_UCHAR);

        short expectedRasGoodFlag = 1;
        assertThat(brain.header.rasGoodFlag).isEqualTo(expectedRasGoodFlag);


        assertThat(brain.data.data_mri_uchar[99][99][99][0]).isEqualTo(77);   // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
        assertThat(brain.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
        assertThat(brain.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
    }

}