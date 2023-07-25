# jneuroformats Development Information

Note: This is developer information, please refer to [github.com/dfsp-spirit/jneuroformats](https://github.com/dfsp-spirit/jneuroformats) for the main README file.


### Setting up the dev environment

I suggest to install [SDKMan](https://sdkman.io/) and use it to make sure you have a suitable JDK (JDK 17 LTS is what I have installed at the time of writing) and maven:

```shell
curl -s "https://get.sdkman.io" | bash         # install SDKMan
sdk install java    # installs latest LTS version of JDK by default, which is fine. Should be 17.x as of 2023.
sdk install maven
```

Then get [Visual Studio Code](https://code.visualstudio.com/) with the `Extension Pack for Java`. This is a collection of tools for Java development, maintained by Microsoft, that includes everything you need for the beginning.

Now clone the repo:

```shell
cd ~/projects/    # or where ever you want to put it
git clone https://github.com/dfsp-spirit/jneuroformats
cd jneuroformats/     # you are now in a directory we will refer to as <repo>.
code .                # start editing the source code with VS Code.
```


### Tests

To run the unit tests, use the `Testing` button in VS Code, or on the command line:

```shell
cd <repo>/jneuroformats/    # important: change into the sub directory!
mvn test
```



### Building with maven

Make sure you are in the `<repo>/jneuroformats/` directory, not just in the root of the repo. Then run the following command to build this project:

```shell
mvn clean verify
```

Pass the `-Dquick` option to skip all non-essential plug-ins and create the output artifact as quickly as possible:

```shell
mvn clean verify -Dquick
```

Run the following command to format the source code and organize the imports as per the project's conventions:

```shell
mvn process-sources
```

#### Building the docs

Make sure you are in the `<repo>/jneuroformats/` directory, not just in the root of the repo.

```shell
mvn javadoc:javadoc
```

### Running the demo App

Make sure you are in the `<repo>/jneuroformats/` directory, not just in the root of the repo.

```shell
mvn package    # to build the jar file.
java -cp target/jneuroformats-1.0-SNAPSHOT.jar org.rcmd.jneuroformats.App src/test/resources/subjects_dir subject1
```

### Publishing to Maven central

This is easiest via sontype OSSRH, see [docs here](https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-maven).

This may also be relevant: [Publishing to maven central](https://github.com/chhh/sonatype-ossrh-parent/blob/master/publishing-to-maven-central.md).


### Credits


* Thanks to all the authors of the dependencies (and their dependencies), as well as our toolchain.
* This project was bootstrapped using the [ModiTect OSS Quickstart](https://github.com/moditect/oss-quickstart) archetype. At the time when I used it (July 2023), it required some manual updates to package versions though, it was a bit outdated.
