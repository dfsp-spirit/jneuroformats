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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the structure, and a value that is assigned to all vertices or voxels that are part of the structure. Sometimes the value is not needed, and can be set to 0.
 */
public class FsLabel {

    /** The element index (vertex or voxel index) */
    public List<Integer> elementIndex;

    /** The x coordinate of the element */
    public List<Float> coordX;

    /** The y coordinate of the element */
    public List<Float> coordY;

    /** The z coordinate of the element */
    public List<Float> coordZ;

    /** The value assigned to the element */
    public List<Float> value;

    /**
     * Default constructor for FsLabel. Initializes empty lists for all fields.
     */
    public FsLabel() {
        elementIndex = new ArrayList<>();
        coordX = new ArrayList<>();
        coordY = new ArrayList<>();
        coordZ = new ArrayList<>();
        value = new ArrayList<>();
    }

    /**
     * Return the number of vertices or voxels that are part of the label.
     * @return the number of vertices or voxels that are part of the label.
     */
    public int size() {
        return this.elementIndex.size();
    }

    /**
     * Validate that the label is internally consistent, i.e. that all lists have the same number of elements.
     * @throws IOException if the label is not internally consistent.
     */
    public void validate() throws IOException {
        if (this.elementIndex.size() != this.coordX.size()) {
            throw new IOException("The number of elements in the FsLabel elementIndex list does not match the number of elements in the coordX list.");
        }
        if (this.elementIndex.size() != this.coordY.size()) {
            throw new IOException("The number of elements in the FsLabel elementIndex list does not match the number of elements in the coordY list.");
        }
        if (this.elementIndex.size() != this.coordZ.size()) {
            throw new IOException("The number of elements in the FsLabel elementIndex list does not match the number of elements in the coordZ list.");
        }
        if (this.elementIndex.size() != this.value.size()) {
            throw new IOException("The number of elements in the FsLabel elementIndex list does not match the number of elements in the value list.");
        }
    }

    /**
     * Compute for all vertices whether they are part of the label.
     * @param numberOfElements the total number of vertices in the surface, or voxels in the volume the label is defined on.
     * @return a Boolean list, containing at position i whether vertex (or voxel) with index i is part of this label.
     */
    public List<Boolean> elementIndexIsPartOfLabel(int numberOfElements) {
        if (numberOfElements < this.size()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The number of vertices ({0}) given as parameter 'numberOfVertices' is smaller than the number of elements in the label ({1}). Label invalid for given element count.",
                    numberOfElements, this.size()));
        }
        List<Boolean> result = new ArrayList<>(numberOfElements);
        for (int i = 0; i < numberOfElements; i++) {
            result.add(false);
        }
        for (int i = 0; i < this.size(); i++) {
            result.set(elementIndex.get(i), true);
        }
        return result;
    }

    /**
     * Read a file in FreeSurfer label format and return a FsLabel object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`.
     * @return an FsLabel object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     */
    protected static FsLabel fromFsLabelFile(Path filePath) throws IOException, FileNotFoundException {

        FsLabel label = new FsLabel();
        List<String> lines = Files.readAllLines(filePath);

        if (lines.size() < 2) {
            throw new IOException("The label file contains less than 2 lines. Invalid label file.");
        }

        for (int i = 2; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != 5) {
                throw new IOException(MessageFormat.format("The label file contains more than 5 columns in line {0}. Invalid label file.", i));
            }
            label.elementIndex.add(Integer.parseInt(tokens[0]));
            label.coordX.add(Float.parseFloat(tokens[1]));
            label.coordY.add(Float.parseFloat(tokens[2]));
            label.coordZ.add(Float.parseFloat(tokens[3]));
            label.value.add(Float.parseFloat(tokens[4]));
        }
        return label;
    }

    /**
     * Generate string representation of this FsLabel in CSV format.
     * @param with_header whether to include a header row at the top of the CSV.
     * @return the CSV format string
     */
    public String toCsvFormat(Boolean with_header) {
        StringBuilder builder = new StringBuilder();
        if (with_header) {
            builder.append("index,coordx,coordy,coordz,value\n");
        }

        for (int i = 0; i < this.size(); i++) {
            builder.append(this.elementIndex.get(i) + "," + this.coordX.get(i) + "," + this.coordY.get(i) + "," + this.coordZ.get(i) + "," + this.value.get(i) + "\n");
        }

        return builder.toString();
    }

    /**
     * Read a file in CSV format and return an FsLabel object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`. The file format is assumed to be CSV, with 5 columns in this order: index, coordx, coordy, coordz, value. The datatypes of the columns are integer for the index column, and float for all other columns.
     * @param has_header whether the file has a header row at the top.
     * @return an FsLabel object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.
     */
    protected static FsLabel fromCsvFile(Path filePath, Boolean has_header) throws IOException, FileNotFoundException {

        FsLabel label = new FsLabel();
        List<String> lines = Files.readAllLines(filePath);

        if (has_header && lines.size() > 0) {
            lines.remove(0);
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] tokens = line.trim().split(",");
            if (tokens.length != 5) {
                throw new IOException(MessageFormat.format("The label file contains more than 5 columns in line {0}. Invalid label file.", i));
            }
            label.elementIndex.add(Integer.parseInt(tokens[0]));
            label.coordX.add(Float.parseFloat(tokens[1]));
            label.coordY.add(Float.parseFloat(tokens[2]));
            label.coordZ.add(Float.parseFloat(tokens[3]));
            label.value.add(Float.parseFloat(tokens[4]));
        }
        return label;
    }

    /**
     * Read a file in CSV or FreeSurfer label format and return an FsLabel object.
     * @param filePath the name of the file to read, as a Path object. Get on from a string by something like `java.nio.file.Paths.Path.get("myfile.txt")`. The file format is assumed to be CSV, with 5 columns in this order: index, coordx, coordy, coordz, value. The datatypes of the columns are integer for the index column, and float for all other columns.
     * @return an FsLabel object.
     * @throws IOException if IO error occurs.
     * @throws FileNotFoundException if the file does not exist.h
     */
    public static FsLabel read(Path filePath) throws IOException, FileNotFoundException {
        if (filePath.toString().toLowerCase().endsWith(".csv")) {
            return FsLabel.fromCsvFile(filePath, Boolean.TRUE);
        }
        else if (filePath.toString().endsWith(".label")) {
            return FsLabel.fromFsLabelFile(filePath);
        }
        else {
            throw new IOException(MessageFormat.format("Unknown FsLabel file format for file {0}.", filePath.toString()));
        }
    }

    /**
     * Generate string representation of this FsLabel in FsLabel format.
     * @return the FsLabel format string
     */
    public String toFsLabelFormat() {
        StringBuilder builder = new StringBuilder();

        builder.append("#!ascii label  , from subject  vox2ras=TkReg\n");
        builder.append(this.size() + "\n");

        for (int i = 0; i < this.size(); i++) {
            builder.append(this.elementIndex.get(i) + "  " + this.coordX.get(i) + "  " + this.coordY.get(i) + "  " + this.coordZ.get(i) + " " + this.value.get(i) + "\n");
        }

        return builder.toString();
    }

    /**
     * Write this label to a file in CSV or FsLabel format.
     * @param filePath the path to the file to write to
     * @param format the format to write in, either "csv" or "fslabel".
     * @throws IOException  if IO error occurs.
     */
    public void write(Path filePath, String format) throws IOException {
        format = format.toLowerCase();
        if (format.equals("csv")) {
            Files.write(filePath, toCsvFormat(Boolean.TRUE).getBytes());
        }
        else if (format.equals("fslabel")) {
            Files.write(filePath, toFsLabelFormat().getBytes());
        }
        else {
            throw new IOException(MessageFormat.format("Unknown FsLabel export format {0}.", format));
        }
    }

    /**
     * Apply this label to a list of float values, such as the data in an FsCurv object. This makes sense only if the label is a surface label, and the list of float values contains per-vertex data for the same surface the label is defined on.
     * @param curvData the list of float values to apply the label to. This function will return the subset of the data in the list that is part of the label.
     * @return a new ArrayList containing the subset of the data in the input list that is part of the label. The size of the returned list is equal to the number of elements in this label.
     */
    public List<Float> applyToCurvData(List<Float> curvData) {
        ArrayList<Float> result = new ArrayList<>(this.size());
        for (int i = 0; i < this.size(); i++) {
            int vertexIndex = this.elementIndex.get(i);
            if (vertexIndex < 0 || vertexIndex >= curvData.size()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The vertex index {0} in the label is out of bounds for the FsCurv data, which has {1} vertices.",
                        vertexIndex, curvData.size()));
            }
            result.add(curvData.get(vertexIndex));
        }
        return result;
    }

    /**
     * Apply this label to a list of float values, such as the data in an FsCurv object. This makes sense only if the label is a surface label, and the list of float values contains per-vertex data for the same surface the label is defined on.
     * @param curvData the list of float values to apply the label to. This function will return the subset of the data in the list that is part of the label.
     * @param invert if set to True, the values returned will be only the values which are NOT part of the label.
     * @return a new ArrayList containing the subset of the data in the input list that is part of the label. The size of the returned list is equal to the number of elements in this label.
     */
    public List<Float> applyToCurvData(List<Float> curvData, boolean invert) {
        ArrayList<Float> result = new ArrayList<>(this.size());
        if (invert) {
            for (int i = 0; i < curvData.size(); i++) {
                if (!this.elementIndex.contains(i)) {
                    result.add(curvData.get(i));
                }
            }
            return result;
        }
        else {
            return applyToCurvData(curvData);
        }
    }

    /**
     * Apply this label to an FsCurv object containing per-vertex data. This makes sense only if the label is a surface label, and the list of float values contains per-vertex data for the same surface the label is defined on.
     * @param curv the FsCurv object to apply the label to. This function will return the subset of the data in the FsCurv object that is part of the label.
     * @return a new ArrayList containing the subset of the data in the FsCurv object that is part of the label. The size of the returned list is equal to the number of elements in this label.
     * @throws IOException if an error occurs, e.g. if the label contains vertex indices that are out of bounds for the FsCurv data.
     */
    public List<Float> applyToCurv(FsCurv curv) throws IOException {
        return applyToCurvData(curv.data);
    }

    /**
     * Apply this label to an FsCurv object containing per-vertex data. This makes sense only if the label is a surface label, and the list of float values contains per-vertex data for the same surface the label is defined on.
     * @param curv the FsCurv object to apply the label to. This function will return the subset of the data in the FsCurv object that is part of the label.
     * @param invert if set to True, the values returned will be only the values which are NOT part of the label.
     * @return a new ArrayList containing the subset of the data in the FsCurv object that is part of the label. The size of the returned list is equal to the number of elements in this label.
     * @throws IOException if an error occurs, e.g. if the label contains vertex indices that are out of bounds for the FsCurv data.
     */
    public List<Float> applyToCurv(FsCurv curv, boolean invert) throws IOException {
        return applyToCurvData(curv.data, invert);
    }
}
