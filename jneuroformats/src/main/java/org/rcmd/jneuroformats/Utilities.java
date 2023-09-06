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
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    // Scale numerical values in input List to range 0..1
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

    public static List<Color> getAllColormapColors(Colormap colormap, List<Float> positions) {
        List<Color> colors = new ArrayList<>();
        for (float position : positions) {
            colors.add(colormap.get(position));
        }
        return colors;
    }

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

    protected static float[] vectorSubtract(float[] v0, float[] v1) {
        float[] result = new float[3];
        result[0] = v0[0] - v1[0];
        result[1] = v0[1] - v1[1];
        result[2] = v0[2] - v1[2];
        return result;
    }
}
