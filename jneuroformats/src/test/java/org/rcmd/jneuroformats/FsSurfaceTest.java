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

public class FsSurfaceTest {

    @Test
    public void oneCanGenerateCube() {

        FsSurface cube = FsSurface.generateCube();

        assertThat(cube.getNumberOfVertices()).isEqualTo(8);
        assertThat(cube.getNumberOfFaces()).isEqualTo(12);
    }

    @Test
    public void oneCanReadOurDemoSurfFile() {

        Path surfFile = Paths.get("src", "test", "resources", "subjects_dir", "subject1", "surf", "lh.white");
        FsSurface hemi_mesh;
        try {
            hemi_mesh = FsSurface.fromFsSurfaceFile(surfFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(hemi_mesh.getNumberOfVertices()).isEqualTo(149244);
        assertThat(hemi_mesh.getNumberOfFaces()).isEqualTo(298484);
    }
}
