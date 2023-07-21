/**
*  This file is part of jneuroformats and subject to its license.
*  See the LICENSE file that accompanies this distribution, or visit
*  the project homepage at https://github.com/dfsp-spirit/jneuroformats.
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

}