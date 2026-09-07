# jneuroformats Development Information

Note: This is developer information, please refer to [github.com/dfsp-spirit/jneuroformats](https://github.com/dfsp-spirit/jneuroformats) for the main README file.


### Setting up the dev environment

I suggest to install [SDKMan](https://sdkman.io/) and use it to make sure you have a suitable JDK (JDK 17 LTS is what I have installed at the time of writing) and maven:

```shell
curl -s "https://get.sdkman.io" | bash         # install SDKMan
bash     # Starts a new shell session, so the 'sdk' command becomes available.
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


### Build errors

If you get an error from maven plugin `license-maven-plugin:4.1:check (default) on project jneuroformats: Some files do not have the expected license header`, you can run this command to add the header automatically. The header is defined in `src/main/resources/header.txt`.

```shell
mvn license:format
```

#### Building the docs

Make sure you are in the `<repo>/jneuroformats/` directory, not just in the root of the repo.

```shell
mvn javadoc:javadoc
```

Note: You only need this to check locally that the docs build without warnings about undocumented code. Publishing the docs to GitHub pages is done automatically by the GitHub Action [javadocs.yml](../.github/workflows/javadocs.yml), which deploys the built docs to the `gh-pages` branch on every push to `main` and on tags `v*`.

### Running the demo App

The demo app loads various FreeSurfer data for a subjects from the FreeSurfer output directory, known as the subjects_dir in FreeSurfer speech. Some demo data in a directory that is organized like a FreeSurfer subjects_dir comes with this software in the repo, so you do **not** need to acquire and process an MRI scan to run the demo app.

Make sure you are in the `<repo>/jneuroformats/` sub directory, not just in the root of the repo.

```shell
mvn package    # to build the jar file.
java -cp target/jneuroformats-1.0-SNAPSHOT.jar org.rcmd.jneuroformats.App --help  # to get usage info
java -cp target/jneuroformats-1.0-SNAPSHOT.jar org.rcmd.jneuroformats.App src/test/resources/subjects_dir subject1  # to run with demo data from the repo
```

Of course, replace `jneuroformats-1.0-SNAPSHOT.jar` with the jar of the version you intend to run.

The last command above runs the app `org.rcmd.jneuroformats.App` with two arguments: The subjects_dir (`src/test/resources/subjects_dir`) and the subject (`subject1`), and prints some information on the loaded files and the data contained in them.

### Publishing to Maven Central

We publish to the [Maven Central Repository](https://central.sonatype.com) via the Sonatype Central Portal. The old OSSRH workflow is deprecated; the setup below uses the current Central Portal and the `central-publishing-maven-plugin`, all configured in `pom.xml` under the `central` profile.

One-time prerequisites:

1. **Namespace.** Register the `org.rcmd` namespace in your Central Portal account and verify it with a DNS TXT record on `rcmd.org`. Verification can take a while, DNS is slow.
2. **User token.** In the Central Portal, go to your publishing settings and generate a user token (it looks like a username/password pair).
3. **GPG key.** Maven Central requires every published file to be signed with GPG. Create a key (no expiry is recommended for signing keys) and publish the *public* key to a keyserver:

   ```shell
   gpg --full-generate-key
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
   ```

4. **Credentials in `~/.m2/settings.xml`.** Add a server with id `central` (your user token) and a `gpg` profile that provides the passphrase:

   ```xml
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
     <servers>
       <server>
         <id>central</id>
         <username>YOUR_USER_TOKEN_USERNAME</username>
         <password>YOUR_USER_TOKEN_PASSWORD</password>
       </server>
     </servers>
     <profiles>
       <profile>
         <id>gpg</id>
         <properties>
           <gpg.executable>gpg</gpg.executable>
           <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
         </properties>
       </profile>
     </profiles>
     <activeProfiles>
       <activeProfile>gpg</activeProfile>
     </activeProfiles>
   </settings>
   ```

Publishing a release (from `<repo>/jneuroformats/`):

```shell
mvn -Pcentral clean deploy
```

This runs the full lifecycle (tests + QA checks), attaches the sources and javadoc jars, signs everything with GPG, uploads to the Central Portal, and publishes automatically after validation. You can watch the deployment status on [central.sonatype.com/publishing/deployments](https://central.sonatype.com/publishing/deployments).

#### Publishing from CI (GitHub Actions)

The workflow [publish.yml](../.github/workflows/publish.yml) does the same automatically whenever a tag `v*` is pushed. To use it, add these repository secrets (Settings -> Secrets and variables -> Actions):

| Secret | Value |
| --- | --- |
| `CENTRAL_TOKEN_USERNAME` | your Central user token username |
| `CENTRAL_TOKEN_PASSWORD` | your Central user token password |
| `GPG_PRIVATE_KEY` | ASCII-armored private key: `gpg --armor --export-secret-keys <KEY_ID>` |
| `GPG_PASSPHRASE` | passphrase of the GPG key (empty if none) |

Then release by tagging:

```shell
git tag v1.2.1
git push origin v1.2.1
```

### Publishing to GitHub packages

Make sure the `pom.xml` is setup correctly, including new package version etc. Then, make sure your Maven config file on your local computer in your home, `~/.m2/settings.xml`, has a section like this:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">

<servers>
  <server>
    <id>github</id>
    <username>your-github-username</username>
    <password>your-personal-access-token</password>
  </server>
</servers>

</settings>
```

The `id`, here `github`, must match the `<distributionManagement>` section in your `pom.xml` file. Note that the `github` server is only needed for this optional GitHub Packages deploy; publishing to Maven Central uses the `central` profile and a separate `central` server instead (see above).

The token you will have to create in your GitHub profile, login and click the User picture, then `Settings` -> `Developer Settings` -> `Personal Access Tokens`. Create one that has the permission "Write packages". This will automatically add required other permissions (e.g., read permissions). Generate and save the token in the `settings.xml` file.

Note that if you already have a token for login to GitHub, it most likely does not have the permission "Write packages", unless you created if after GitHub packages was started and explicitely enabled that for the token.

Note that you can check the validity of your token by running `curl -H "Authorization: token <your-personal-access-token>" https://api.github.com/user/packages`.

Then run `mvn clean deploy`.

### Making a new Release -- Checklist

* Add your new code and unit tests for it
* Add a description of the changes since last release to the file [CHANGES](../CHANGES) (always do this after every change, on the go).
* Run the tests with `mvn test` and ensure everything is green
* Build the docs locally, and ensure there are no warnings about undocumented code, via `mvn javadoc:javadoc`.
* Publish the API documentation to GitHub pages: this is automatic now. The GitHub Action [javadocs.yml](../.github/workflows/javadocs.yml) builds and deploys the docs to the `gh-pages` branch on every push to `main` and whenever a `v*` tag is pushed. Just push the release tag (see below); no manual deployment needed. You can also trigger it manually from the "Actions" tab.
* Bump the version in [pom.xml](./pom.xml)
* Publish the package to Maven Central as described above in the section `Publishing to Maven Central` (via `mvn -Pcentral clean deploy`, or by pushing a `v*` tag, which triggers the GitHub Action [publish.yml](../.github/workflows/publish.yml))
* Tag the commit hash of the version you published with the version, e.g., `git log --oneline` to see last commits, then `git tag v1.2.1 asfjhjs`, where asfjhjs is your commit hash from the git log command. Then upload the tag via `git push --tags`.


### Contributions

We are happy to accept pull requests. It's best to first open an issue here on GitHub to discuss your plans, especially if you intend to do larger changes.


### Credits


* Thanks to all the authors of the dependencies (and their dependencies), as well as our toolchain.
* This project was bootstrapped using the [ModiTect OSS Quickstart](https://github.com/moditect/oss-quickstart) archetype. At the time when I used it (July 2023), it required some manual updates to package versions before it worked though, it was a bit outdated.
