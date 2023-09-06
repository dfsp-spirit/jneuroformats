package org.rcmd.jneuroformats;

import java.nio.file.Path;
import java.nio.file.Paths;

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
}
