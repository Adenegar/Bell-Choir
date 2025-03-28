# Bell Choir

## Project Overview

Bell Choir is a multi-threaded Java application that simulates a bell choir playing musical pieces. The program reads music notation from text files and plays them using bell-like tones. Each note in the music piece is played by a dedicated thread, simulating individual bell ringers working together in coordination.

## How to Use

### Prerequisites

This program was built and tested using Java version 21.0.4 and Apache Ant version 1.10.15. 

### Building the Project
The project uses Ant for building. To compile the project:

```bash
ant compile
```

### Running the Project

Run a song by specifying the song file as a command line argument:

```bash
ant run -Dsong=songs/PlayThatSong.txt
```

If you have your song folder setup in the same way as in our project you can specify a song by simply typing the song name:

```bash
ant run -Dsong=Custom
```

### Running the Tests
To run the tests that validate song file parsing:

```bash
ant test
```

### Testing Other Choirs

If other systems attempt to use these songs, here's some details about the requirements to play some of our project's songs:

PlayThatSongBasic.txt: 
- Note support from C4 to G5

PlayThatSong.txt
- Note support from C4 to G5
- dotted quarter (txt representation: 6, note length: 0.375) and dotted half (txt representation: 3, note length: 0.75) support

## Generating UML Diagrams

To generate UML diagrams:

1. Place the plantuml.jar file in the lib folder.
2. Run the following command to generate an SVG of your UML diagram:

   ```bash
   java -jar lib/plantuml.jar -tsvg src/uml/UML_Diagram.puml
   ```