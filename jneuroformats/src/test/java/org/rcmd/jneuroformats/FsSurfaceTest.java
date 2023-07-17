/*
 *  Copyright 2021 Tim Schäfer
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
