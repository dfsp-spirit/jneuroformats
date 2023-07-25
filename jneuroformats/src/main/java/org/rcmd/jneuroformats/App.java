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

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.rcmd.jneuroformats.Util.Viridis;

import static org.rcmd.jneuroformats.Utilities.getAllColormapColors;
import static org.rcmd.jneuroformats.Utilities.scaleToZeroOne;

public class App {

    private static void usage() {
        System.out.println("=== jneuroformats Demo App: load some data from a FreeSurfer subject directory ===");
        System.out.println("USAGE: java -jar app.jar <subjects_dir> <subject_id>");
        System.out.println("Parameters details:");
        System.out.println(" <subjects_dir> : string, path to the subjects directory created by FreeSurfer's recon-all program for all subjects.");
        System.out.println(" <subject_id>   : string, subject identifier, e.g. 'bert'. A sub directory with this name must exist under <subjects_dir>.");
    }

    public static void main(String[] args) {

        // Minimal command line argument parsing.
        String subjectsDir = null;
        String subjectId = null;

        if (args.length == 2) {
            subjectsDir = args[0];
            subjectId = args[1];
        }
        else {
            usage();
            System.exit(1);
        }

        // Check whether subjectsDir/ exists
        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(subjectsDir))) {
            System.out.println("ERROR: subjects_dir '" + subjectsDir + "'does not exist.");
            System.exit(1);
        }

        // Check whether subjectsDir/subjectId/ exists
        Path subjectDir = java.nio.file.Paths.get(subjectsDir, subjectId);
        if (!java.nio.file.Files.exists(subjectDir)) {
            System.out.println("ERROR: The subject directory '" + subjectDir + "'does not exist under the subjects_dir.");
            System.exit(1);
        }

        // Now read various files from the subject directory.
        // We start with a mesh/FsSurface.
        Path lhWhitePath = java.nio.file.Paths.get(subjectDir.toString(), "surf", "lh.white");
        FsSurface lhSurface;
        try {
            lhSurface = FsSurface.fromFsSurfaceFile(lhWhitePath);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Read " + lhSurface.getNumberOfVertices() + " vertices and " + lhSurface.getNumberOfFaces() + " faces from the surface file.");

        // Now read the annotation file.
        Path lhAnnotPath = java.nio.file.Paths.get(subjectDir.toString(), "label", "lh.aparc.annot");
        FsAnnot lhAnnot;
        try {
            lhAnnot = FsAnnot.fromFsAnnotFile(lhAnnotPath);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Read " + lhAnnot.numRegions() + " regions from the annotation file.");

        // Read per-vertex data: sulcal depth for the left hemisphere from a curv file.
        Path lhCurvPath = java.nio.file.Paths.get(subjectDir.toString(), "surf", "lh.sulc");
        FsCurv lhSulc;
        try {
            lhSulc = FsCurv.fromFsCurvFile(lhCurvPath);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Read " + lhSulc.data.size() + " per-vertex values from the sulc file.");

        // Now export a vertex-colored mesh in PLY format from the per-vertex sulc data.
        Path plyFileSulc = java.nio.file.Paths.get(subjectDir.toString(), "surf", "lh.white.sulc.ply");
        try {
            Colormap viridis = new Viridis();
            List<Color> vertexColorsRgb = getAllColormapColors(viridis, scaleToZeroOne(lhSulc.data));
            Files.write(plyFileSulc, lhSurface.toPlyFormat(vertexColorsRgb).getBytes());
            System.out.println("Wrote mesh vertex-colored by per-vertex sulcal depth to file: " + plyFileSulc.toString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Now export a vertex-colored mesh in PLY format from the annotation.
        Path plyFileAnnot = java.nio.file.Paths.get(subjectDir.toString(), "label", "lh.aparc.annot.ply");
        try {
            Files.write(plyFileAnnot, lhSurface.toPlyFormat(lhAnnot.getVertexColorsRgb()).getBytes());
            System.out.println("Wrote mesh vertex-colored by Desikan regions to file: " + plyFileAnnot.toString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
