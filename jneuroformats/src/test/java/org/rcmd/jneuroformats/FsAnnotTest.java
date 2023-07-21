/**
*  This file is part of jneuroformats and subject to its license.
*  See the LICENSE file that accompanies this distribution, or visit
*  the project homepage at https://github.com/dfsp-spirit/jneuroformats.
*/

package org.rcmd.jneuroformats;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
