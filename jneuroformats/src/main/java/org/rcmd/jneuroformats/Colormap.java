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

/**
 * An interface to a colormap, i.e., a mapping from a numerical value to a color. This is typically
 * used in plotting, and the most common use case in neuroimaging is the visualization of descriptor
 * values like cortical thickness, atlas regions, or statistical values like t-values or p-values on
 * brain surface meshes.
 *
 * This package does not provide any colormap implementations, but it provides a common interface so
 * you can use your own colormap. You could use the implementation from https://github.com/mahdilamb/colormap.
 */
public interface Colormap {

    /**
     * Return the RGB color for a given value. The value is typically a descriptor value like cortical thickness, and in range 0..1. However, values outside this range, and even NAN or infinite values are supported, and will be set to special colors by most colormaps (NAN color, low color, high color, etc.).
     * @param position a numerical value, typically from range 0..1, see `jneuroformats.Util.scaleToZeroOne` for a basic normalization function. However, the colormap also supports values outside this range and NAN or infinte values, they will be set the special colors.
     * @return the red, green, and blue channel values as an array of three integers in range 0..255.
     */
    public Color get(Float position);
}