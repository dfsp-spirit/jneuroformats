# jneuroformats
Reading and writing structural neuroimaging file formats for Java.

[![main](https://github.com/dfsp-spirit/jneuroFormats/actions/workflows/unittests.yml/badge.svg?branch=main)](https://github.com/dfsp-spirit/jneuroFormats/actions)


## About

File format readers and writers for file formats used in structural neuroimaging research, with a focus on the
surface-based workflow used in [FreeSurfer](https://freesurfer.net).


![Vis](./img/rgl_brain_aparc.jpg?raw=true "An aparc brain atlas visualization.")


## Features

* Meshes:
  - Read brain meshes in FreeSurfer binary mesh format (like recon_all output file `<SUBJECTS_DIR>/<subject>/surf/lh.white`): use function `FsSurface.fromFsSurfaceFile()`
  - Read brain meshes from MZ3 format, as used by [Surf-Ice](https://github.com/neurolabusc/surf-ice)
  - Write in PLY, OBJ, and FreeSurfer binary mesh formats: `FsSurface.writeToFile()`
* Labels (FreeSurfer volume and surface labels, like `<subject>/label/lh.cortex.label`):
  - Read from FreeSurfer label format:  `FsLabel.fromFsLabelFile()`
  - Write in FreeSurfer label format and to CSV format: `FsLabel.writeToFile()`
* Annots or mesh parcellations (like Desikan atlas parcellation in recon_all output file `<SUBJECTS_DIR>/<subject>/label/lh.aparc.annot`):
  - Read from FreeSurfer annot format: `FsAnnot.fromFsAnnotFile()`
  - Write to FreeSurfer annot format and to CSV (including the color table):  `FsAnnot.writeToFile()`
* Brain volumes (3D or 4D MRI scans, like `<subject>/mri/brain.mgz`):
  - Read from files in FreeSurfer MGH format: `FsMgh.fromFsMghFile()`
  - Read from files in FreeSurfer MGZ format: `FsMgh.fromFsMgzFile()`
* Per-Vertex data and per-voxel data, like cortical thickness or statistical results:
  - Read from MGH/MGZ files (they can store 4D arrays, which is useful for the raw images and per-vertex/per-voxel data)
  - Read from MZ3 files: `Mz3.fromMz3File()`. Can also read per-vertex colors from MZ3 files.
  - Read from FreeSurfer curv files (like `<subject>/surf/lh.thickness`): `FsCurv.fromFsCurvFile()`
  - Write to FreeSurfer curv files: `FsCurv.writeToFile()`

Many of the classes provide utility methods which are typically needed in structural neuroimaging, so check the API documentation before re-inventing the wheel. Examples include the function `FsAnnot.getVertexColorsRgb()` to compute the vertex colors for a brain atlas from the annotation labels and its color map.


## Installation

This is work-in-progress, not yet.


## Documentation and Usage

The API documentation will be published to a central repository once the package is officially released. See the development information if you want to generate it yourself.

A full example app can be found in the file [App.java](./jneuroformats/src/main/java/org/rcmd/jneuroformats/App.java).

The [unit tests](./jneuroformats/src/test/java/org/rcmd/jneuroformats/) also include various usage examples.


## Development

Development information can be found [here](./jneuroformats/README_dev.md).

