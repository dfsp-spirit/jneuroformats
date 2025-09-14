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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

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
            curv.write(temp, "curv");
            FsCurv curv2 = FsCurv.fromFsCurvFile(temp);
            assertThat(curv2.data.size()).isEqualTo(curv.data.size());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanWriteAndRereadFsCurvInCsvFormat() {

        ArrayList<Float> data = new ArrayList<>(Arrays.asList(1.0f, 1.0f, 1.0f));
        FsCurv curv = new FsCurv(data);

        try {
            Path temp = Files.createTempFile("", ".csv");
            curv.write(temp, "csv");
            FsCurv curv2 = FsCurv.read(temp);
            assertThat(curv2.data.size()).isEqualTo(curv.data.size());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void simpleStatFunctionsOfFsCurvWork() {
        ArrayList<Float> data = new ArrayList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        FsCurv curv = new FsCurv(data);
        assertThat(curv.size()).isEqualTo(3);
        assertThat(curv.mean()).isEqualTo(2.0f);
        assertThat(curv.min()).isEqualTo(1.0f);
        assertThat(curv.max()).isEqualTo(3.0f);
        assertThat(curv.std()).isEqualTo(0.816496580927726, within(1e-6));
        assertThat(curv.containsNaN()).isFalse();
        assertThat(curv.getIndicesOfNaN()).isEmpty();
    }

}
