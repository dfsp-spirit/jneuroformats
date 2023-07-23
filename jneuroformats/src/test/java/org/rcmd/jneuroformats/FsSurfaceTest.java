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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

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

    @Test
    public void oneCanWriteAndRereadASurface() {

        FsSurface cube = FsSurface.generateCube();

        try {
            Path temp = Files.createTempFile("", ".tmp");
            cube.writeToFile(temp, "surf");
            FsSurface cube2 = FsSurface.fromFsSurfaceFile(temp);
            assertThat(cube2.getNumberOfVertices()).isEqualTo(cube.getNumberOfVertices());
            assertThat(cube2.getNumberOfFaces()).isEqualTo(cube.getNumberOfFaces());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
