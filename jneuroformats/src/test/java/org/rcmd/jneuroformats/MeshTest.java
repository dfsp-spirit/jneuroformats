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
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class MeshTest {

    @Test
    public void oneCanGenerateCube() {

        Mesh cube = Mesh.generateCube();

        assertThat(cube.getNumberOfVertices()).isEqualTo(8);
        assertThat(cube.getNumberOfFaces()).isEqualTo(12);
    }

    @Test
    public void oneCanComputeVertexNormals() {

        Mesh cube = Mesh.generateCube();

        List<float[]> vNormals = cube.computeVertexNormals();

        assertThat(vNormals).isNotNull();
        assertThat(vNormals.size()).isEqualTo(cube.getNumberOfVertices());
    }

    @Test
    public void oneCanComputeFaceNormals() {

        Mesh cube = Mesh.generateCube();

        List<float[]> fNormals = cube.computeFaceNormals();

        assertThat(fNormals).isNotNull();
        assertThat(fNormals.size()).isEqualTo(cube.getNumberOfFaces());
    }

    @Test
    public void oneCanWriteAndRereadObj() {

        Mesh cube = Mesh.generateCube();
        try {
            Path temp = Files.createTempFile("", ".obj");
            cube.write(temp, "obj");
            Mesh cube2 = Mesh.read(temp);
            assertThat(cube2.getNumberOfVertices()).isEqualTo(8);
            assertThat(cube2.getNumberOfFaces()).isEqualTo(12);
            assertThat(cube2.vertices.toArray()).isEqualTo(cube.vertices.toArray());
            assertThat(cube2.faces.toArray()).isEqualTo(cube.faces.toArray());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void oneCanWriteAndRereadMeshAsMz3() {

        Mesh cube = Mesh.generateCube();
        try {
            Path temp = Files.createTempFile("", ".mz3");
            cube.write(temp, "mz3");
            Mesh cube2 = Mesh.read(temp);
            assertThat(cube2.getNumberOfVertices()).isEqualTo(8);
            assertThat(cube2.getNumberOfFaces()).isEqualTo(12);
            assertThat(cube2.vertices.toArray()).isEqualTo(cube.vertices.toArray());
            assertThat(cube2.faces.toArray()).isEqualTo(cube.faces.toArray());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
