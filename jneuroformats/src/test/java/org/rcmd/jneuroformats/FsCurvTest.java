/*
 *  This file is part of jneuroformats and subject to its license.
 *  See the LICENSE file that accompanies this distribution, or visit
 *  the project homepage at https://github.com/dfsp-spirit/jneuroformats.
 */

package org.rcmd.jneuroformats;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class FsCurvTest {

    @Test
    public void oneCanConstructEmptyFsCurv() {
        FsCurv curv = new FsCurv();
        assertThat(curv.data.size()).isEqualTo(0);
    }

    @Test
    public void oneCanConstructFsCurvWithData() {
        ArrayList<Float> data = new ArrayList<>(Arrays.asList(1.0f, 1.0f, 1.0f));
        FsCurv curv = new FsCurv(data);
        assertThat(curv.data.size()).isEqualTo(data.size());
    }

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

    @Test
    public void oneCanWriteAndRereadFsCurv() {

        ArrayList<Float> data = new ArrayList<>(Arrays.asList(1.0f, 1.0f, 1.0f));
        FsCurv curv = new FsCurv(data);

        try {
            Path temp = Files.createTempFile("", ".tmp");
            curv.writeToFile(temp, "curv");
            FsCurv curv2 = FsCurv.fromFsCurvFile(temp);
            assertThat(curv2.data.size()).isEqualTo(curv.data.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
