/*
 *  This file is part of jneuroformats and subject to its license.
 *  See the LICENSE file that accompanies this distribution, or visit
 *  the project homepage at https://github.com/dfsp-spirit/jneuroformats.
 */

package org.rcmd.jneuroformats;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class FsCurvTest {

    @Test
    public void oneCanReadOurDemoCurvFile() {

        Path curvFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "surf", "lh.sulc");
        FsCurv sulc;
        try {
            sulc = FsCurv.fromFsCurvFile(curvFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(sulc.data.size()).isEqualTo(149244);
    }

}
