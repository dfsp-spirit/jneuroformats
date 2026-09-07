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
    private static final Path MGH_FILE = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "mri", "brain.mgh");

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
    public void theFreesurferBrainNiftiMatchesTheBrainMgz() {

        // `brain.nii` was created from `brain.mgz`/`brain.mgh` with FreeSurfer's `mri_convert`, so
        // all three files must describe the same volume: same voxel data and same voxel-to-RAS
        // geometry. This is an end-to-end cross-check against an independent reference (real
        // FreeSurfer output), not just a self-round-trip.
        Nifti1 nifti;
        FsMgh fromNii;
        FsMgh fromMgz;
        FsMgh fromMgh;
        try {
            nifti = Nifti1.read(NII_FILE);
            fromNii = nifti.toMgh();
            fromMgz = FsMgh.read(MGZ_FILE);
            fromMgh = FsMgh.read(MGH_FILE);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // (1) Same dimensions, data type and RAS flag.
        assertThat(fromNii.header.dim1Size).isEqualTo(256);
        assertThat(fromNii.header.dim2Size).isEqualTo(256);
        assertThat(fromNii.header.dim3Size).isEqualTo(256);
        assertThat(fromNii.header.dim4Size).isEqualTo(1);
        assertThat(fromNii.header.mriDatatype).isEqualTo(FsMgh.MRI_UCHAR);
        assertThat(fromNii.header.rasGoodFlag).isEqualTo((short) 1);
        assertThat(fromMgz.header.rasGoodFlag).isEqualTo((short) 1);
        assertThat(fromMgh.header.rasGoodFlag).isEqualTo((short) 1);

        // (2) Same voxel data.
        assertSameData(fromNii, fromMgz);
        assertSameData(fromMgz, fromMgh);

        // (3) The RAS header fields derived from the NIfTI file equal the ones read from the MGH
        // files, and match the known reference values of this demo volume.
        float[] expectedMdc = { -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
        float[] expectedPxyzC = { -0.49995422f, 29.372742f, -48.90473f };
        for (FsMgh vol : new FsMgh[] { fromNii, fromMgz, fromMgh }) {
            assertThat(vol.header.sizeX).isEqualTo(1.0f);
            assertThat(vol.header.sizeY).isEqualTo(1.0f);
            assertThat(vol.header.sizeZ).isEqualTo(1.0f);
            assertThat(vol.header.Mdc).hasSize(9);
            for (int i = 0; i < 9; i++) {
                assertThat(vol.header.Mdc.get(i)).isCloseTo(expectedMdc[i], within(0.0001f));
            }
            assertThat(vol.header.Pxyz_c).hasSize(3);
            for (int i = 0; i < 3; i++) {
                assertThat(vol.header.Pxyz_c.get(i)).isCloseTo(expectedPxyzC[i], within(0.0001f));
            }
        }

        // (4) The vox2ras matrices of all three files agree, and equal the affine that FreeSurfer
        // stored directly in the NIfTI s-form (the raw srow_x/y/z rows of `brain.nii`).
        float[][] expectedVox2ras = {
                { -1.0f, 0.0f, 0.0f, 127.5f },
                { 0.0f, 0.0f, 1.0f, -98.6273f },
                { 0.0f, -1.0f, 0.0f, 79.0953f },
                { 0.0f, 0.0f, 0.0f, 1.0f }
        };
        for (FsMgh vol : new FsMgh[] { fromNii, fromMgz, fromMgh }) {
            float[][] vox2ras = vol.header.computeVox2ras();
            assertThat(vox2ras).isNotNull();
            assertVox2rasClose(vox2ras, expectedVox2ras, 0.01);
        }
        // And our reconstruction reproduces the s-form stored in the file exactly.
        float[][] vox2rasNii = fromNii.header.computeVox2ras();
        assertThat(vox2rasNii[0][3]).isCloseTo(nifti.header.srowX[3], within(0.01f));
        assertThat(vox2rasNii[1][3]).isCloseTo(nifti.header.srowY[3], within(0.01f));
        assertThat(vox2rasNii[2][3]).isCloseTo(nifti.header.srowZ[3], within(0.01f));

        // (5) Anchor handling: the s-form translation stored in `brain.nii` is the RAS of voxel
        // (0,0,0) (P0 = (127.5, -98.6, 79.1)), which is *not* the MGH center voxel Pxyz_c
        // (about (-0.5, 29.4, -48.9)). Reading the file must re-anchor that translation into the
        // MGH center convention (see (3)).
        assertThat(nifti.header.srowX[3]).isCloseTo(127.50005f, within(0.01f));
        assertThat(nifti.header.srowY[3]).isCloseTo(-98.62726f, within(0.01f));
        assertThat(nifti.header.srowZ[3]).isCloseTo(79.09527f, within(0.01f));
        for (int i = 0; i < 3; i++) {
            float[] srow = (i == 0) ? nifti.header.srowX : (i == 1) ? nifti.header.srowY : nifti.header.srowZ;
            float diff = Math.abs(srow[3] - fromNii.header.Pxyz_c.get(i));
            assertThat(diff).isGreaterThan(10.0f);
        }
    }

    @Test
    public void oneCanWriteMgzAsNiftiMatchingFreesurferReference() {

        // Writing the demo MGH/MGZ volume as NIfTI must produce the same voxel-to-RAS geometry
        // that FreeSurfer's mri_convert stored in the reference brain.nii (same s-form rows,
        // q-offset = voxel-(0,0,0) RAS, qfac).
        FsMgh brain;
        try {
            brain = FsMgh.read(MGZ_FILE);
            Path temp = Files.createTempFile("", ".nii");
            brain.writeNifti(temp);
            Nifti1Header h = Nifti1.read(temp).header;
            Files.delete(temp);

            assertThat(h.sformCode).isEqualTo(Nifti1Header.XFORM_SCANNER_ANAT);
            assertThat(h.qformCode).isEqualTo(Nifti1Header.XFORM_SCANNER_ANAT);
            float[][] expectedSform = {
                    { -1.0f, 0.0f, 0.0f, 127.50005f },
                    { 0.0f, 0.0f, 1.0f, -98.62726f },
                    { 0.0f, -1.0f, 0.0f, 79.09527f }
            };
            float[][] sform = { h.srowX.clone(), h.srowY.clone(), h.srowZ.clone() };
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 4; j++) {
                    assertThat(sform[i][j]).isCloseTo(expectedSform[i][j], within(0.01f));
                }
            }
            // q-offset equals the s-form translation (voxel-(0,0,0) RAS), and qfac is -1.
            assertThat(h.qoffsetX).isCloseTo(127.50005f, within(0.01f));
            assertThat(h.qoffsetY).isCloseTo(-98.62726f, within(0.01f));
            assertThat(h.qoffsetZ).isCloseTo(79.09527f, within(0.01f));
            assertThat(h.pixdim[0]).isCloseTo(-1.0f, within(0.001f));
            assertThat(h.pixdim[1]).isEqualTo(1.0f);
            assertThat(h.pixdim[2]).isEqualTo(1.0f);
            assertThat(h.pixdim[3]).isEqualTo(1.0f);
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
            // For a 90 degree rotation about the z axis (allow small float error from the quaternion
            // reconstruction), the MGH Mdc rows are the unit direction cosines of the 3 volume axes,
            // i.e. the (normalized) columns of the rotation matrix: row 0 = (0, 1, 0), row 1 = (-1, 0, 0),
            // row 2 = (0, 0, 1).
            float[] expectedMdc = { 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f };
            assertThat(mgh.header.Mdc).hasSize(9);
            for (int i = 0; i < 9; i++) {
                assertThat(mgh.header.Mdc.get(i)).isCloseTo(expectedMdc[i], within(0.00001f));
            }
            // Pxyz_c is the RAS of the center voxel (c_crs = (1, 1, 2) for dims 2x3x4):
            // Pxyz_c = qoffset + rotation * (1, 1, 2) = (10, 20, 30) + (-1, 1, 2) = (9, 21, 32).
            assertThat(mgh.header.Pxyz_c).hasSize(3);
            assertThat(mgh.header.Pxyz_c.get(0)).isCloseTo(9.0f, within(0.0001f));
            assertThat(mgh.header.Pxyz_c.get(1)).isCloseTo(21.0f, within(0.0001f));
            assertThat(mgh.header.Pxyz_c.get(2)).isCloseTo(32.0f, within(0.0001f));
            // The voxel sizes are the norms of the columns of the reconstructed rotation, which
            // is 1 with tiny float error (the quaternion uses sqrt(0.5)).
            assertThat(mgh.header.sizeX).isCloseTo(1.0f, within(0.0001f));
            assertThat(mgh.header.sizeY).isCloseTo(1.0f, within(0.0001f));
            assertThat(mgh.header.sizeZ).isCloseTo(1.0f, within(0.0001f));
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

    private static void assertVox2rasClose(float[][] actual, float[][] expected, double eps) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertThat(actual[i][j]).isCloseTo(expected[i][j], within((float) eps));
            }
        }
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
        if (a.header.rasGoodFlag == 1) {
            assertThat(a.header.sizeX).isCloseTo(b.header.sizeX, within(0.001f));
            assertThat(a.header.sizeY).isCloseTo(b.header.sizeY, within(0.001f));
            assertThat(a.header.sizeZ).isCloseTo(b.header.sizeZ, within(0.001f));
            assertThat(a.header.Mdc).hasSize(9);
            assertThat(b.header.Mdc).hasSize(9);
            for (int i = 0; i < 9; i++) {
                assertThat(a.header.Mdc.get(i)).isCloseTo(b.header.Mdc.get(i), within(0.001f));
            }
            assertThat(a.header.Pxyz_c).hasSize(3);
            assertThat(b.header.Pxyz_c).hasSize(3);
            for (int i = 0; i < 3; i++) {
                assertThat(a.header.Pxyz_c.get(i)).isCloseTo(b.header.Pxyz_c.get(i), within(0.001f));
            }
            // And the full vox2ras matrices must agree.
            float[][] vox2rasA = a.header.computeVox2ras();
            float[][] vox2rasB = b.header.computeVox2ras();
            assertThat(vox2rasA).isNotNull();
            assertThat(vox2rasB).isNotNull();
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    assertThat(vox2rasA[i][j]).isCloseTo(vox2rasB[i][j], within(0.01f));
                }
            }
        }
    }
}
