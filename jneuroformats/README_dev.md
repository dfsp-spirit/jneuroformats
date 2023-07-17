# jneuroformats Development Information

Note: This is developer information, please refer to [github.com/dfsp-spirit/jneuroformats](https://github.com/dfsp-spirit/jneuroformats) for the main README file.


### Setting up the dev environment

I suggest to install [SDKMan](https://sdkman.io/) and use it to make sure you have a suitable JDK (JDK 17 LTS is what I have installed at the time of writing) and maven:

```shell
curl -s "https://get.sdkman.io" | bash         # install SDKMan
sdk install java    # installs latest LTS version of JDK by default, which is fine. Should be 17.x
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

To run the unit tests:

```shell
cd <repo>/jneuroformats/    # important: change into the sub directory!
mvn test
```



### Building with maven

Make sure you are in the `<repo>/jneuroformats/` directory. Then run the following command to build this project:

```
mvn clean verify
```

Pass the `-Dquick` option to skip all non-essential plug-ins and create the output artifact as quickly as possible:

```
mvn clean verify -Dquick
```

Run the following command to format the source code and organize the imports as per the project's conventions:

```
mvn process-sources
```


### Credits


* Thanks to all the authors of the dependencies (and their dependencies), as well as our toolchain.
* This project was bootstrapped using the [ModiTect OSS Quickstart](https://github.com/moditect/oss-quickstart) archetype.
