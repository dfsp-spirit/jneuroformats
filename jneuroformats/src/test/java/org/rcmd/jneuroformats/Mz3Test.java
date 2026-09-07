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

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class Mz3Test {

    @Test
    public void initMz3() {

        Mz3 mz3 = new Mz3();
        assertThat(mz3.mesh.getNumberOfVertices()).isEqualTo(0);
        assertThat(mz3.perVertexData.size()).isEqualTo(0);
        assertThat(mz3.vertexColors.size()).isEqualTo(0);
    }

    @Test
    public void oneCanReadOurDemoMz3File() {

        Path mz3SurfFile = Paths.get("src", "test", "resources", "mesh", "cube.mz3");
        Mz3 mz3;
        try {
            mz3 = Mz3.fromMz3File(mz3SurfFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(mz3.mesh.getNumberOfVertices()).isEqualTo(8);
        assertThat(mz3.perVertexData.size()).isEqualTo(0);
        assertThat(mz3.vertexColors.size()).isEqualTo(0);
    }

    @Test
    public void oneCanWriteAndRereadMz3() {

        Path mz3SurfFile = Paths.get("src", "test", "resources", "mesh", "cube.mz3");
        Mz3 mz3;
        try {
            mz3 = Mz3.fromMz3File(mz3SurfFile);
            Path temp = Files.createTempFile("", ".mz3");
            mz3.write(temp);
            Mz3 mz3_2 = Mz3.fromMz3File(temp);
            assertThat(mz3_2.mesh.getNumberOfVertices()).isEqualTo(8);
            assertThat(mz3_2.mesh.getNumberOfFaces()).isEqualTo(12);
            assertThat(mz3_2.mesh.vertices.toArray()).isEqualTo(mz3.mesh.vertices.toArray());
            assertThat(mz3_2.mesh.faces.toArray()).isEqualTo(mz3.mesh.faces.toArray());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanWriteAndRereadMz3WithColorsAndScalarData() {

        Mesh cube = Mesh.generateCube();
        List<Float> perVertexData = new ArrayList<>();
        List<Color> vertexColors = new ArrayList<>();
        for (int i = 0; i < cube.getNumberOfVertices(); i++) {
            perVertexData.add((float) i);
            vertexColors.add(new Color(i * 10, i * 20, i * 30, 255));
        }
        Mz3 mz3 = new Mz3(cube, perVertexData, vertexColors);
        try {
            Path temp = Files.createTempFile("", ".mz3");
            mz3.write(temp);
            Mz3 mz3_2 = Mz3.fromMz3File(temp);
            assertThat(mz3_2.mesh.getNumberOfVertices()).isEqualTo(8);
            assertThat(mz3_2.mesh.getNumberOfFaces()).isEqualTo(12);
            assertThat(mz3_2.perVertexData).isEqualTo(perVertexData);
            assertThat(mz3_2.vertexColors.size()).isEqualTo(8);
            assertThat(mz3_2.vertexColors.get(3)).isEqualTo(new Color(30, 60, 90, 255));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
