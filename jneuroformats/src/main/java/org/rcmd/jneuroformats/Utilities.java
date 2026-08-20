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
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions.
 */
public class Utilities {

    /** Default constructor for Utilities. */
    public Utilities() {
    }

    /**
     * Scale a list of float values to the range 0.0 to 1.0.
     * @param values the list of float values to scale
     * @return a new list containing the scaled values
     */
    public static List<Float> scaleToZeroOne(List<Float> values) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (Float value : values) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        float range = max - min;
        List<Float> scaledValues = new ArrayList<>();
        for (Float value : values) {
            scaledValues.add((value - min) / range);
        }
        return scaledValues;
    }

    /**
     * Get colors from a colormap for a list of positions.
     * @param colormap the colormap to use
     * @param positions the list of positions, in the range 0.0 to 1.0
     * @return a list of colors corresponding to the positions in the colormap
     */
    public static List<Color> getAllColormapColors(Colormap colormap, List<Float> positions) {
        List<Color> colors = new ArrayList<>();
        for (float position : positions) {
            colors.add(colormap.get(position));
        }
        return colors;
    }

    /**
     * Convert an array of floats to an array of doubles.
     * @param input the array of floats to convert
     * @return an array of doubles
     */
    protected static double[] convertFloatsToDoubles(float[] input) {
        if (input == null) {
            return null;
        }

        double[] output = new double[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }

    /**
     * Subtract two 3D vectors.
     * @param v0 the first vector
     * @param v1 the second vector
     * @return the result of v0 - v1
     */
    protected static float[] vectorSubtract(float[] v0, float[] v1) {
        float[] result = new float[3];
        result[0] = v0[0] - v1[0];
        result[1] = v0[1] - v1[1];
        result[2] = v0[2] - v1[2];
        return result;
    }
}
