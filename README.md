# jneuroformats
Reading and writing structural neuroimaging file formats for Java.

[![main](https://github.com/dfsp-spirit/jneuroFormats/actions/workflows/unittests.yml/badge.svg?branch=main)](https://github.com/dfsp-spirit/jneuroFormats/actions)

## About

File format readers and writers for file formats used in structural neuroimaging research, with a focus on the
surface-based workflow used in [FreeSurfer](https://freesurfer.net).

## Features

* Meshes:
  - Read brain meshes in FreeSurfer binary mesh format (like recon_all output file `<SUBJECTS_DIR>/<subject>/surf/lh.white`): use function `FsSurface.fromFsSurfaceFile()`
  - Write in PLY, OBJ, and FreeSurfer binary mesh formats: `FsSurface.writeToFile()`
* Labels (FreeSurfer volume and surface labels, like `<subject>/label/lh.cortex.label`):
  - Read from FreeSurfer label format:  `FsLabel.fromFsLabelFile()`
  - Write in FreeSurfer label format and to CSV format: `FsLabel.writeToFile()`
* Annots or mesh parcellations (like Desikan atlas parcellation in recon_all output file `<SUBJECTS_DIR>/<subject>/label/lh.aparc.annot`):
  - Read from FreeSurfer annot format: `FsAnnot.fromFsAnnotFile()`
  - Write to FreeSurfer annot format and to CSV (including the color table):  `FsAnnot.writeToFile()`
* Brain volumnes (3D or 4D MRI scans)
  - Read from files in FreeSurfer MGH format: `FsMgh.fromFsMghFile()`

## Usage and Installation

This is work-in-progress, not yet.


## Development

Development information can be found [here](./jneuroformats/README_dev.md).

