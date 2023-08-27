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

        assertThat(brain.header.mriDatatype).isEqualTo(FsMgh.MRI_UCHAR);

        short expectedRasGoodFlag = 1;
        assertThat(brain.header.rasGoodFlag).isEqualTo(expectedRasGoodFlag);

        assertThat(brain.data.data_mri_uchar[99][99][99][0]).isEqualTo(77); // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
        assertThat(brain.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
        assertThat(brain.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
    }

    @Test
    public void oneCanReadOurDemoMghFileUsingRead() {

        Path mghFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgh");
        FsMgh brain;
        try {
            brain = FsMgh.read(mghFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        short expectedRasGoodFlag = 1;
        assertThat(brain.header.rasGoodFlag).isEqualTo(expectedRasGoodFlag);

        assertThat(brain.data.data_mri_uchar[99][99][99][0]).isEqualTo(77); // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
        assertThat(brain.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
        assertThat(brain.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
    }

    // Also test MGZ format, which is just a gzipped MGH file.
    @Test
    public void oneCanReadOurDemoMgZFile() {

        Path mghFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgz");
        FsMgh brain;
        try {
            brain = FsMgh.fromFsMgzFile(mghFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(brain.header.dim1size).isEqualTo(256);
        assertThat(brain.header.dim2size).isEqualTo(256);
        assertThat(brain.header.dim3size).isEqualTo(256);
        assertThat(brain.header.dim4size).isEqualTo(1);

        assertThat(brain.header.mriDatatype).isEqualTo(FsMgh.MRI_UCHAR);

        short expectedRasGoodFlag = 1;
        assertThat(brain.header.rasGoodFlag).isEqualTo(expectedRasGoodFlag);

        assertThat(brain.data.data_mri_uchar[99][99][99][0]).isEqualTo(77); // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
        assertThat(brain.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
        assertThat(brain.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
    }

    // Also test MGZ format, which is just a gzipped MGH file.
    @Test
    public void oneCanReadOurDemoMgZFileUsingRead() {

        Path mghFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgz");
        FsMgh brain;
        try {
            brain = FsMgh.read(mghFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        short expectedRasGoodFlag = 1;
        assertThat(brain.header.rasGoodFlag).isEqualTo(expectedRasGoodFlag);

        assertThat(brain.data.data_mri_uchar[99][99][99][0]).isEqualTo(77); // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
        assertThat(brain.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
        assertThat(brain.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
    }

    @Test
    public void oneCanWriteAndRereadFsMgh() {

        Path mghFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgz");
        FsMgh brain;
        try {
            brain = FsMgh.fromFsMgzFile(mghFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Path temp = Files.createTempFile("", ".mgh");
            brain.write(temp, "mgh");
            FsMgh brain2 = FsMgh.fromFsMghFile(temp);
            assertThat(brain2.header.dim1size).isEqualTo(brain.header.dim1size);
            assertThat(brain2.header.dim2size).isEqualTo(brain.header.dim2size);
            assertThat(brain2.header.dim3size).isEqualTo(brain.header.dim3size);
            assertThat(brain2.header.dim4size).isEqualTo(brain.header.dim4size);

            assertThat(brain2.data.data_mri_uchar[99][99][99][0]).isEqualTo(77); // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
            assertThat(brain2.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
            assertThat(brain2.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanWriteAndRereadFsMghWithMgzFormat() {

        Path mghFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgz");
        FsMgh brain;
        try {
            brain = FsMgh.fromFsMgzFile(mghFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Path temp = Files.createTempFile("", ".mgz");
            brain.write(temp, "mgz");
            FsMgh brain2 = FsMgh.fromFsMgzFile(temp);
            assertThat(brain2.header.dim1size).isEqualTo(brain.header.dim1size);
            assertThat(brain2.header.dim2size).isEqualTo(brain.header.dim2size);
            assertThat(brain2.header.dim3size).isEqualTo(brain.header.dim3size);
            assertThat(brain2.header.dim4size).isEqualTo(brain.header.dim4size);

            assertThat(brain2.data.data_mri_uchar[99][99][99][0]).isEqualTo(77); // try on command line: mri_info --voxel 99 99 99 pathto/subjects_dir/subject1/mri/brain.mgh
            assertThat(brain2.data.data_mri_uchar[109][109][109][0]).isEqualTo(71);
            assertThat(brain2.data.data_mri_uchar[0][0][0][0]).isEqualTo(0);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
