# prettymd
prettymd is a scriptable .md file pretty printer using GraalVM.

## About
This project provides a pretty printer implemented in Java for the markdown language (.md files). Moreover, it also provides a JavaScript interface to customize the behaviour of the pretty printer (see [Documentation](#documentation)).

GraalVM is able to build a native image out of this application. This comes in handy regarding the performance. 

The parser and visitor pattern used, derive from the Atlassian [commonmark](https://github.com/atlassian/commonmark-java) library.

## Requirements

  - Maven (see [Apache Maven](https://maven.apache.org))
  - GraalVM (see [GraalVM](https://www.graalvm.org))
  - For compilation `native-image` depends on the local toolchain, so please make sure: `glibc-devel`, `zlib-devel` (header files for the C library and `zlib`) and `gcc` are available on your system.

## Getting Started

#### Set up GraalVM
See the documentation on the [GraalVM website](https://www.graalvm.org/docs/getting-started/) how to install and use GraalVM.

Basically you have to [download](https://www.graalvm.org/downloads/) GraalVM and use it as your JDK instead of your normal one.

#### Install Maven
For installing maven please see [Installing Apache Maven](https://maven.apache.org/install.html).

On Debian based distros, you can use `sudo apt-get install maven` to install maven on your machine.

## Usage
The following examples use [test.md](test.md) as markdown input and [example.js](example.js) as JavaScript script.

Export the GraalVM temporarily as `JAVA_HOME`:

```
export JAVA_HOME=/home/pasci/Downloads/graalvm-ee-1.0.0-rc9
```

Pretty print a markdown file using Java:

```
$JAVA_HOME/bin/java -cp "target/prettymd.jar" com.pascal.prettymd.FormatPretty test.md
```

Pretty print a markdown file using Java and customize it with a script:

```
$JAVA_HOME/bin/java -cp "target/prettymd.jar" com.pascal.prettymd.FormatPretty test.md example.js
```

Build maven package (Hint: this will also directly build the native image. This should take around 5 minutes.):

```
mvn package
```

Pretty print a markdown file using the native image:

```
./prettymd test.md
```

Pretty print a markdown file using the native image and customize it with a script:

```
./prettymd test.md example.js
```

## Documentation
An extensive documentation of the JavaScript API can be seen at [JavaScript.md](JavaScript.md).


## Known Issues
  - Because the pretty printer is implemented with a visitor pattern, there can be misbehaviour if the markdown file contains many nested items.
  - The Atlassian Commonmark parser loses the information if a heading is an alternative heading.

    ```
    Header
    ======
    ```

    will become

    ``` 
    # Header
    ```

## Remarks
  - This [.md file](README.md) and [JavaScript.md](JavaScript.md) were formatted using this printer
