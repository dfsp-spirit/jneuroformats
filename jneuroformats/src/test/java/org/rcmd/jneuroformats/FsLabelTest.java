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

public class FsLabelTest {

   @Test
   public void oneCanConstructEmptyFsLabel() {
       FsLabel label = new FsLabel();
       assertThat(label.size()).isEqualTo(0);
   }

   @Test
    public void oneCanReadOurDemoLabelFile() {

        Path labelFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "label", "lh.cortex.label");
        FsLabel cortex;
        try {
            cortex = FsLabel.fromFsLabelFile(labelFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(cortex.size()).isEqualTo(140891);
    }

}
