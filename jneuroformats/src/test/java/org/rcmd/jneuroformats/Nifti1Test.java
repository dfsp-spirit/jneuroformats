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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

public class Nifti1Test {

    private static final Path NII_FILE = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.nii");
    private static final Path MGZ_FILE = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgz");

    @Test
    public void oneCanReadOurDemoNiftiFile() {

        Nifti1 nifti;
        try {
            nifti = Nifti1.read(NII_FILE);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        FsMgh brain = nifti.toMgh();
        assertThat(brain.header.dim1Size).isEqualTo(256);
        assertThat(brain.header.dim2Size).isEqualTo(256);
        assertThat(brain.header.dim3Size).isEqualTo(256);
        assertThat(brain.header.dim4Size).isEqualTo(1);
        assertThat(brain.header.mriDatatype).isEqualTo(FsMgh.MRI_UCHAR);
        assertThat(brain.header.rasGoodFlag).isEqualTo((short) 1);
        // Same voxel values as in the demo MGH file.
        assertThat(brain.data.dataMriUchar[99][99][99][0]).isEqualTo(77);
        assertThat(brain.data.dataMriUchar[109][109][109][0]).isEqualTo(71);
        assertThat(brain.data.dataMriUchar[0][0][0][0]).isEqualTo(0);
    }

    @Test
    public void oneCanReadNiftiAndMghGiveSameData() {

        FsMgh fromNii;
        FsMgh fromMgz;
        try {
            fromNii = Nifti1.read(NII_FILE).toMgh();
            fromMgz = FsMgh.read(MGZ_FILE);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertSameData(fromNii, fromMgz);
    }

    @Test
    public void oneCanConvertMghToNiftiAndBack() {

        FsMgh brain;
        try {
            brain = FsMgh.read(MGZ_FILE);
            Path temp = Files.createTempFile("", ".nii");
            brain.writeNifti(temp);
            Nifti1 nifti = Nifti1.read(temp);
            FsMgh brain2 = nifti.toMgh();
            assertSameData(brain, brain2);
            assertSameRas(brain, brain2);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanConvertNiftiToMghAndBack() {

        FsMgh brain;
        try {
            brain = Nifti1.read(NII_FILE).toMgh();
            Path temp = Files.createTempFile("", ".mgh");
            brain.write(temp, "mgh");
            FsMgh brain2 = FsMgh.read(temp);
            assertSameData(brain, brain2);
            assertSameRas(brain, brain2);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanWriteAndReadSmallFloatVolumeAsNifti() {

        FsMgh volume = new FsMgh();
        volume.header.dim1Size = 2;
        volume.header.dim2Size = 3;
        volume.header.dim3Size = 4;
        volume.header.dim4Size = 1;
        volume.header.mriDatatype = FsMgh.MRI_FLOAT;
        volume.header.rasGoodFlag = 0;
        volume.data = new FsMghData(volume.header);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    volume.data.dataMriFloat[i][j][k][0] = i + 10.0f * j + 100.0f * k + 0.5f;
                }
            }
        }

        try {
            Path temp = Files.createTempFile("", ".nii");
            volume.writeNifti(temp);
            FsMgh volume2 = Nifti1.read(temp).toMgh();
            assertSameData(volume, volume2);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanReadQformOnlyNiftiFile() {

        // Build a small NIfTI file that has only a qform (no sform) and read it back.
        Nifti1Header h = new Nifti1Header();
        h.sizeofHdr = 348;
        h.dim[0] = 3;
        h.dim[1] = 2;
        h.dim[2] = 3;
        h.dim[3] = 4;
        h.dim[4] = 1;
        h.dim[5] = 1;
        h.dim[6] = 1;
        h.dim[7] = 1;
        h.datatype = Nifti1Header.DT_FLOAT32;
        h.bitpix = 32;
        h.pixdim[0] = 1.0f;
        h.pixdim[1] = 1.0f;
        h.pixdim[2] = 1.0f;
        h.pixdim[3] = 1.0f;
        h.voxOffset = 352.0f;
        h.qformCode = 1;
        h.sformCode = 0;
        // 90 degree rotation about the z axis: (a, b, c, d) = (sqrt(0.5), 0, 0, sqrt(0.5)).
        h.quaternB = 0.0f;
        h.quaternC = 0.0f;
        h.quaternD = (float) Math.sqrt(0.5);
        h.qoffsetX = 10.0f;
        h.qoffsetY = 20.0f;
        h.qoffsetZ = 30.0f;
        h.magic[0] = 'n';
        h.magic[1] = '+';
        h.magic[2] = '1';
        h.magic[3] = 0;

        int numValues = 2 * 3 * 4;
        ByteBuffer buf = ByteBuffer.allocate(352 + numValues * 4).order(ByteOrder.BIG_ENDIAN);
        h.writeToByteBuffer(buf);
        buf.putInt(348, 0);
        buf.position(352);
        for (int i = 0; i < numValues; i++) {
            buf.putFloat(i);
        }

        try {
            Path temp = Files.createTempFile("", ".nii");
            Files.write(temp, buf.array());
            FsMgh mgh = Nifti1.read(temp).toMgh();
            assertThat(mgh.header.dim1Size).isEqualTo(2);
            assertThat(mgh.header.dim2Size).isEqualTo(3);
            assertThat(mgh.header.dim3Size).isEqualTo(4);
            assertThat(mgh.header.mriDatatype).isEqualTo(FsMgh.MRI_FLOAT);
            assertThat(mgh.header.rasGoodFlag).isEqualTo((short) 1);
            // Expected Mdc for a 90 degree rotation about z (allow small float error from the quaternion reconstruction).
            float[] expectedMdc = { 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f };
            assertThat(mgh.header.Mdc).hasSize(9);
            for (int i = 0; i < 9; i++) {
                assertThat(mgh.header.Mdc.get(i)).isCloseTo(expectedMdc[i], within(0.00001f));
            }
            assertThat(mgh.header.Pxyz_c).isEqualTo(java.util.Arrays.asList(10.0f, 20.0f, 30.0f));
            assertThat(mgh.header.sizeX).isEqualTo(1.0f);
            assertThat(mgh.header.sizeY).isEqualTo(1.0f);
            assertThat(mgh.header.sizeZ).isEqualTo(1.0f);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanReadNiftiUsingFsMghConvenience() {

        FsMgh brain;
        try {
            brain = FsMgh.fromNiftiFile(NII_FILE);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(brain.header.dim1Size).isEqualTo(256);
        assertThat(brain.data.dataMriUchar[99][99][99][0]).isEqualTo(77);
    }

    private static void assertSameData(FsMgh a, FsMgh b) {
        assertThat(a.header.dim1Size).isEqualTo(b.header.dim1Size);
        assertThat(a.header.dim2Size).isEqualTo(b.header.dim2Size);
        assertThat(a.header.dim3Size).isEqualTo(b.header.dim3Size);
        assertThat(a.header.dim4Size).isEqualTo(b.header.dim4Size);
        assertThat(a.header.mriDatatype).isEqualTo(b.header.mriDatatype);

        int d1 = a.header.dim1Size;
        int d2 = a.header.dim2Size;
        int d3 = a.header.dim3Size;
        int d4 = a.header.dim4Size;

        switch (a.header.mriDatatype) {
            case FsMgh.MRI_FLOAT:
                for (int i = 0; i < d1; i++) {
                    for (int j = 0; j < d2; j++) {
                        for (int k = 0; k < d3; k++) {
                            for (int l = 0; l < d4; l++) {
                                assertThat(a.data.dataMriFloat[i][j][k][l]).isEqualTo(b.data.dataMriFloat[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_INT:
                for (int i = 0; i < d1; i++) {
                    for (int j = 0; j < d2; j++) {
                        for (int k = 0; k < d3; k++) {
                            for (int l = 0; l < d4; l++) {
                                assertThat(a.data.dataMriInt[i][j][k][l]).isEqualTo(b.data.dataMriInt[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_SHORT:
                for (int i = 0; i < d1; i++) {
                    for (int j = 0; j < d2; j++) {
                        for (int k = 0; k < d3; k++) {
                            for (int l = 0; l < d4; l++) {
                                assertThat(a.data.dataMriShort[i][j][k][l]).isEqualTo(b.data.dataMriShort[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            case FsMgh.MRI_UCHAR:
                for (int i = 0; i < d1; i++) {
                    for (int j = 0; j < d2; j++) {
                        for (int k = 0; k < d3; k++) {
                            for (int l = 0; l < d4; l++) {
                                assertThat(a.data.dataMriUchar[i][j][k][l]).isEqualTo(b.data.dataMriUchar[i][j][k][l]);
                            }
                        }
                    }
                }
                break;
            default:
                fail("Unexpected data type " + a.header.mriDatatype);
        }
    }

    private static void assertSameRas(FsMgh a, FsMgh b) {
        assertThat(a.header.rasGoodFlag).isEqualTo(b.header.rasGoodFlag);
        assertThat(a.header.sizeX).isEqualTo(b.header.sizeX);
        assertThat(a.header.sizeY).isEqualTo(b.header.sizeY);
        assertThat(a.header.sizeZ).isEqualTo(b.header.sizeZ);
        assertThat(a.header.Mdc).isEqualTo(b.header.Mdc);
        assertThat(a.header.Pxyz_c).isEqualTo(b.header.Pxyz_c);
    }
}
