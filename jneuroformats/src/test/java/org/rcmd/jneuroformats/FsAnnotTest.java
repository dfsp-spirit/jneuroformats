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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class FsAnnotTest {

    @Test
    public void oneCanConstructEmptyFsAnnot() {
        FsAnnot annot = new FsAnnot();
        assertThat(annot.numRegions()).isEqualTo(0);
    }

    @Test
    public void oneCanReadOurDemoAnnotFile() {

        Path annotFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "label", "lh.aparc.annot");
        FsAnnot desikan;
        try {
            desikan = FsAnnot.fromFsAnnotFile(annotFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(desikan.numRegions()).isEqualTo(35);
        assertThat(desikan.numVertices()).isEqualTo(149244);
    }

    @Test
    public void oneCanWriteAndRereadFsAnnot() {

        Path annotFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "label", "lh.aparc.annot");
        FsAnnot desikan;
        try {
            desikan = FsAnnot.fromFsAnnotFile(annotFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Path temp = Files.createTempFile("", ".tmp");
            desikan.writeToFile(temp, "fsannot");
            FsAnnot desikan2 = FsAnnot.fromFsAnnotFile(temp);
            assertThat(desikan.numRegions()).isEqualTo(desikan2.numRegions());
            assertThat(desikan.numVertices()).isEqualTo(desikan2.numVertices());
            assertThat(desikan.colortable.numRegions()).isEqualTo(desikan2.colortable.numRegions());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
