# jneuroformats
Reading and writing structural neuroimaging file formats for Java.

## About

This is work-in-progress, nothing to see here atm.

## Development

### Setting up the dev environment

I suggest to install [SDKMan](https://sdkman.io/) and use it to make sure you havea suitable JDK (JDK 17 LTS is what I have installed at the time of writing) and maven:

```shell
sdk install java
sdk install maven
```

Then get Visual Studio Code with the `Extension Pack for Java`. This is a collection of tools for Java development, maintained by Microsoft.


### Tests

To run the unit tests:

```shell
cd <repo>/jneuroformats/    # important: change into the sub directory!
mvn test
```
