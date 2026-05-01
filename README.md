# OSProgram3

Operating Systems Program 3 submission repository for the Belmont/TEC project-pair collaboration.

## Project Goal

This project counts right triangles from 2D point sets using:
- single-process execution
- multi-process execution
- multi-threaded execution

The implementation supports both text and binary input formats through a shared `PointStore` interface.

## Repository Structure

- `com/tryright/` - Java source files
- `test/` - test data files and test plan artifacts

## Build

From the repository root:

```bash
javac com/tryright/*.java
```

## Run

Single process:

```bash
java com.tryright.Triangles <input_file>
```

Multi-process:

```bash
java com.tryright.ProcessTriangles <input_file> <num_processes>
```

Multi-threaded:

```bash
java com.tryright.ThreadTriangles <input_file> <num_threads>
```

## Input Formats

Text format:
- first line: number of points
- remaining lines: `x y`

Binary format (`.dat`):
- sequence of 8-byte point records
- each record: 4-byte big-endian `x`, then 4-byte big-endian `y`

## Current Ownership

Primary maintainer: Oren Kagan

Additional group contributor: Charlie Davis, Xain Alsaad
