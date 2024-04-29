# Contributing

## Quick Note
Ever since Moulberry has stopped working on NEU, other contributors have been working on new features and fixes for the mod. If you are interested in contributing yourself, make a pull request to [NotEnoughUpdates/NotEnoughUpdates](https://github.com/NotEnoughUpdates/NotEnoughUpdates) to contribute to the prereleases, which eventually will be merged in bulk to [Moulberry/NotEnoughUpdates](https://github.com/Moulberry/NotEnoughUpdates) for major releases. 

## Before you contribute

- Please check your feature / bug wasn't already fixed in one of our pre-releases, on the [development branch](https://github.com/NotEnoughUpdates/NotEnoughUpdates/tree/master/) or in an open [pull request](https://github.com/NotEnoughUpdates/NotEnoughUpdates/pulls)  
- Consider joining our [Discord](https://discord.gg/moulberry) to check in on the newest developments by other people, or to get help with problems you encounter.
- Please check that your feature idea complies with the [Hypixel Rules](https://hypixel.net/rules). (See these Hypixel forum posts for extra information: [Mods in SkyBlock](https://hypixel.net/threads/regarding-the-recent-announcement-with-mods-in-skyblock.4045481/), [QoL Modifications](https://hypixel.net/threads/update-to-disallowed-modifications-qol-modifications.4043482/), [Modifications Sending Invalid Clicks](https://hypixel.net/threads/update-regarding-modifications-sending-invalid-clicks.5130489/)) 
- Make sure that your feature idea is not already implemented in another non-paid mod. (E.g. Dungeon Solver)

## Setting up a development environment

### Software prerequisites

- Install a Java Development Kit (You will need both version 8 and version 17) [Eclipse Temurin Download](https://adoptium.net/temurin/releases) for convenience, however, any JDK will do.
- Install Git. [Windows download](https://git-scm.com/download/win)
- Install an IDE, such as [Jetbrains IntelliJ IDEA](https://www.jetbrains.com/idea/download).

### Software configuration

- Fork the NEU repository using the fork button on top right of the page and name the repo NotEnoughUpdates.
- Clone the forked repository using `git clone https://github.com/<YourUserName>/NotEnoughUpdates`.
- Make sure to create new branches for features you are working on and not commit to the master branch of your repository.
- After you have committed all the necessary changes, make a pull request on that branch.
- Use the master branch as a way to pull the latest changes from the NEU repo.
- Import that folder as a Gradle Project in your IDE (IntelliJ should autodetect it as Gradle if you select the `NotEnoughUpdates` folder in the Open dialog)
- Set your project SDK to your 1.8 JDK. This can be done in the modules settings (CTRL+ALT+SHIFT+S) in IntelliJ.
- Set your gradle JVM to your 1.17 JDK. This can be done by searching for `gradle jvm` in the CTRL+SHIFT+A dialog in IntelliJ.
- Run the `gen<IntelliJ/Eclipse>Runs` gradle task. In IntelliJ that can be done in the Gradle tab on the right side of your IDE.
- Optionally, run the `genSources` gradle task.
- Run the `Minecraft Client` to make sure that everything works.
  - Note: if you are using macOS, remove the `XstartOnFirstThread` JVM option

## Logging into Hypixel in a development environment

Use [DevAuth](https://github.com/DJtheRedstoner/DevAuth). You do **not** need to download the jar, just follow the configuration instructions in the [DevAuth README](https://github.com/DJtheRedstoner/DevAuth#configuration-file)

## Hot Reloading

Hot Reloading is possible by launching using the IntelliJ debugger and having [DCEVM 1.8](https://dcevm.github.io/) installed to your JVM. Then you can run a regular build and confirm the reload prompt. This can cause issues (especially with commands), so restarting is sometimes still necessary.

> Warning: Depending on your system configuration, you may need to install DCEVM onto an existing JVM. In that case, you need to install a Java 1.8 JVM, specifically version 1.8u181 (although some newer versions up until 1.8u265 *may* work). These old JVM versions may require an Oracle account to download, and you can find them [here](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html).

For quicker hot swapping or if the above does not work, you can install [Single Hotswap](https://plugins.jetbrains.com/plugin/14832-single-hotswap). With this, you can hot swap a single java class instead of rebuilding the entire project. This still requires DCEVM.

## Creating a new Release
<details>
<summary>Minimized, for your convenience</summary>

### Preparing a release

To prepare a release, first merge all the PRs that you want, and then tag that resulting merge commit using `git tag <version>`.
Do *not* use a `vX.X.X` prefix, just raw-dog the `X.X.X` version. If you want this to be a pre-release set the patch version
to something `!= 0`. Note that we follow normal semver rules here, so `3.1.1 > 3.1.0`.

GitHub actions will automatically build a JAR and generate a changelog and upload both to a draft release. Now you rally
the troups and get your fellow contributors to sign this JAR.

### Signing a release

The generated draft release should contain a sha256 hash sum. Copy that hash sum for later.

Make sure you have [generated a key](#generating-a-key).

Run `./gradlew signRelease`. Paste in the sha256 hash from earlier. It will generate a `.asc` signature for every 
`secret/` you have.

Copy those secrets into the draft release.

### Publishing a release

Once all relevant personnel have signed off on the release, the release can be published. It should be automatically
available to all people with an auto updater, and be automatically published on modrinth too. The release needs to be
manually uploaded to discord.

### Generating a key

If you haven't generated a key yet, and you have been told to get one, this is how.

For your first key generation, you will need to use openssl.

```bash
# Generate an RSA private key
openssl genpkey -out id_rsa.pem -algorithm RSA # This step can be skipped, if you want to re-use an existing *RSA* key.

# Convert your RSA key to pkcs8, without a password protection
openssl pkcs8 -in id_rsa.pem -outform DER -out myname.der -topk8 -nocrypt

# Generate a public key from your pkcs8 private key
openssl rsa -pubout -in id_rsa.pem -outform der -out myname.key
```

Now you have 3 files:

- `id_rsa.pem` is your base private key. Store it safely somewhere else (maybe on a USB stick). Never share this one.
- `myname.der` is your secret. Put it in the `secrets/` folder in your NEU repo. Never share this one.
- `myname.key` is your public key. Put it in the `src/main/resources/trusted_team_members` folder.

Make sure that the names of the `.der` and the `.key` file match.

</details>
