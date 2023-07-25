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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class FsColortableTest {

    @Test
    public void oneCanConstructEmptyFsColortable() {
        FsColortable ct = new FsColortable();
        assertThat(ct.numRegions()).isEqualTo(0);
    }

    @Test
    public void computingLabelFromColorAndReverseWorks() {
        int r = 255;
        int g = 0;
        int b = 0;
        int a = 0;

        int label = FsColortable.computeLabelFromRgb(r, g, b, a);
        int[] col = FsColortable.computeRgbFromLabel(label);
        assertThat(col.length).isEqualTo(4);
        assertThat(col[0]).isEqualTo(r);
        assertThat(col[1]).isEqualTo(g);
        assertThat(col[2]).isEqualTo(b);
        assertThat(col[3]).isEqualTo(a);
    }

}