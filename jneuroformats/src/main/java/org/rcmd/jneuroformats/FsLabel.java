

package org.rcmd.jneuroformats;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.nio.file.Path;
import java.io.File;

/**
 * Models a FreeSurfer label, can be a surface label or a volume label.
 *
 * The label is defined by a set of vertices or voxels that are part of the structure, and a value that is assigned to all vertices or voxels that are part of the structure. Sometimes the value is not needed, and can be set to 0.
 */
public class FsLabel {

    public List<Integer> elementIndex;
    public List<Float> coordX;
    public List<Float> coordY;
    public List<Float> coordZ;
    public List<Float> value;

    public FsLabel() {
        elementIndex = new ArrayList<>();
        coordX = new ArrayList<>();
        coordY = new ArrayList<>();
        coordZ = new ArrayList<>();
        value = new ArrayList<>();
    }

    public int size() {
        return this.elementIndex.size();
    }

    public void validate() throws Exception {
        if(this.elementIndex.size() != this.coordX.size()) {
            throw new Exception("The number of elements in the FsLabel elementIndex list does not match the number of elements in the coordX list.");
        }
        if(this.elementIndex.size() != this.coordY.size()) {
            throw new Exception("The number of elements in the FsLabel elementIndex list does not match the number of elements in the coordY list.");
        }
        if(this.elementIndex.size() != this.coordZ.size()) {
            throw new Exception("The number of elements in the FsLabel elementIndex list does not match the number of elements in the coordZ list.");
        }
        if(this.elementIndex.size() != this.value.size()) {
            throw new Exception("The number of elements in the FsLabel elementIndex list does not match the number of elements in the value list.");
        }
    }

    /**
     * Compute for all vertices whether they are part of the label.
     * @param numberOfElements the total number of vertices in the surface, or voxels in the volume the label is defined on.
     * @return a Boolean list, containing at position i whether vertex (or voxel) with index i is part of this label.
     */
    public List<Boolean> elementIndexIsPartOfLabel(int numberOfElements) {
        if(numberOfElements < this.size()) {
            throw new IllegalArgumentException(MessageFormat.format("The number of vertices ({0}) given as parameter 'numberOfVertices' is smaller than the number of elements in the label ({1}). Label invalid for given element count.", numberOfElements, this.size()));
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
    public static FsLabel fromFsLabelFile(Path filePath) throws IOException, FileNotFoundException {

        FsLabel label = new FsLabel();

        Scanner lineScanner = new Scanner(new File(filePath.toString()));

        int num_elements_hdr = 0;

        String line, line_preproc;
        Scanner rowScanner;
        int line_idx = 0;
        while (lineScanner.hasNextLine()) {
            line = lineScanner.nextLine();

            if(line_idx == 0) {  //Skip the first line, it is a comment string.
                line_idx++;
                continue;
            }

            if(line_idx == 1) {  // The second line contains the number of elements in the label (vertices or voxels).
                rowScanner = new Scanner(line);
                num_elements_hdr = rowScanner.nextInt();
                rowScanner.close();
                line_idx++;
                continue;
            }


            //Scan lines >= 3 for tokens
            // Note: the current Scanner approach is rather slow.
            line_preproc = line.trim().replaceAll(" +", ",");  // The lines do not contain consistent field separators. Some fields are separated by 2 spaces "  ", some by a single space " ". We replace all spaces by commas, and then use a comma as field separator.
            rowScanner = new Scanner(line_preproc);
            rowScanner.useDelimiter(",");
            label.elementIndex.add(rowScanner.nextInt());
            label.coordX.add(rowScanner.nextFloat());
            label.coordY.add(rowScanner.nextFloat());
            label.coordZ.add(rowScanner.nextFloat());
            label.value.add(rowScanner.nextFloat());
            assert ! rowScanner.hasNext() : MessageFormat.format("The label file contains more than 5 columns in line {0}. Invalid label file.", line_idx);
            rowScanner.close();
            line_idx++;
        }

        if(num_elements_hdr != label.size()) {
            throw new IOException(MessageFormat.format("The number of elements in the header ({0}) does not match the number of elements in the label ({1}). Label invalid.", num_elements_hdr, label.size()));
        }

        return label;
    }

}

