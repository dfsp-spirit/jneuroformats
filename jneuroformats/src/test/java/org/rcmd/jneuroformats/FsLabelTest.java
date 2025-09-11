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

    @Test
    public void oneCanWriteAndRereadFsLabel() {

        Path labelFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "label", "lh.cortex.label");
        FsLabel cortex;
        try {
            cortex = FsLabel.fromFsLabelFile(labelFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Path temp = Files.createTempFile("", ".tmp");
            cortex.write(temp, "fslabel");
            FsLabel cortex2 = FsLabel.fromFsLabelFile(temp);
            assertThat(cortex2.size()).isEqualTo(cortex.size());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanWriteAndRereadFsLabelFromCsv() {

        Path labelFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "label", "lh.cortex.label");
        FsLabel cortex;
        try {
            cortex = FsLabel.fromFsLabelFile(labelFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Path temp = Files.createTempFile("", ".csv");
            cortex.write(temp, "csv");
            FsLabel cortex2 = FsLabel.read(temp);
            assertThat(cortex2.size()).isEqualTo(cortex.size());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
