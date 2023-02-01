# Raytrace Demo

A simple demo of software raytracing.

![Screenshot](docs/Screen%20Shot%202023-02-01%20at%2022.29.12.png)

## Features

* Camera
  * Adjustable position
  * Adjustable rotation (pitch, yaw, roll)
* Shapes
  * Position
  * Color
  * Reflections
      * Reflectivity
  * Types
    * Sphere
* Lighting
  * Color and intensity
  * Specular highlights
  * Shadows
  * Types
      * Ambient
      * Directional
      * Point

### TODO

* Transparency; opacity, refraction

### Maybe?
* Other geometric shapes
* Scripted movement/video output
* Loading scene from file

## Requirements
* Maven (optional)
* Java 19 (with `--enable-preview`)

## Quick guide

* Compiling with Maven
```bash
mvn compile
```

* Running with Maven
```bash
mvn exec:java
```

* Running without Maven (after compiling)
```bash
java --enable-preview -cp target/classes fi.misaki.raytrace.Main
```