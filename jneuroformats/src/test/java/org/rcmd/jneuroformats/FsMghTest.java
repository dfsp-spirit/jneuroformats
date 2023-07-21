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
    }

}