# Contributing

## Quick Note
Ever since Moulberry has stopped working on NEU, other contributors have been working on new features and fixes for the mod. If you are interested in contributing yourself, make a pull request to [NotEnoughUpdates/NotEnoughUpdates](https://github.com/NotEnoughUpdates/NotEnoughUpdates) to contribute to the prereleases, which eventually will be merged in bulk to [Moulberry/NotEnoughUpdates](https://github.com/Moulberry/NotEnoughUpdates) for major releases. 

## Before you contribute

- Please check your feature / bug isn't already fixed in one of our pre-releases, on the [development branch](https://github.com/NotEnoughUpdates/NotEnoughUpdates/tree/master/) or in an open [pull request](https://github.com/NotEnoughUpdates/NotEnoughUpdates/pulls)  
- Consider joining our [Discord](https://discord.gg/moulberry) to check in on newest developments by other people, or to get help with problems you encounter.
- Please check that your feature idea complies with the [Hypixel Rules](https://hypixel.net/rules). (See these Hypixel forum posts for extra information: [Mods in SkyBlock](https://hypixel.net/threads/regarding-the-recent-announcement-with-mods-in-skyblock.4045481/), [QoL Modifications](https://hypixel.net/threads/update-to-disallowed-modifications-qol-modifications.4043482/), [Modifications Sending Invalid Clicks](https://hypixel.net/threads/update-regarding-modifications-sending-invalid-clicks.5130489/)) 
- Make sure that your feature idea is not already implemented in another non-paid mod. (E.g. Dungeon Solver)

## Setting up a development environment

### Software prerequisites

- Install a Java Development Kit (You will need both version 8 and version 17) [Eclipse Temurin Download](https://adoptium.net/temurin/releases) for convenience, however any JDK will do.
- Install Git. [Windows download](https://git-scm.com/download/win)
- Install an IDE, such as [Jetbrains IntelliJ IDEA](https://www.jetbrains.com/idea/download).

### Software configuration

- Fork the NEU repository using the fork button on top right of the page and name the repo NotEnoughUpdates.
- Clone the forked repository using `git clone https://github.com/<YourUserName>/NotEnoughUpdates`.
- Make sure to create new branches for features you are working on and not commit to the master branch of your repository.
- After you have committed all of the necessary changes make a pull request on that branch.
- Use the master branch as a way to pull the latest changes from the NEU repo.
- Import that folder as a Gradle Project in your IDE (IntelliJ should autodetect it as gradle if you select the `NotEnoughUpdates` folder in the Open dialog)
- Set your project SDK to your 1.8 JDK. This can be done in the modules settings (CTRL+ALT+SHIFT+S) in IntelliJ.
- Set your gradle JVM to your 1.17 JDK. This can be done by searching for `gradle jvm` in the CTRL+SHIFT+A dialog in IntelliJ.
- Run the `gen<IntelliJ/Eclipse>Runs` gradle task. In IntelliJ that can be done in the gradle tab on the right side of your IDE.
- Optionally, run the `genSources` gradle task.
- Run the `Minecraft Client` to make sure that everything works.
  - Note: if you are using OSX, remove the `XstartOnFirstThread` JVM option

## Logging into Hypixel in a development environment

Use [DevAuth](https://github.com/DJtheRedstoner/DevAuth). You do **not** need to download the jar, just follow the configuration instructions in the [DevAuth README](https://github.com/DJtheRedstoner/DevAuth#configuration-file)

## Hot Reloading

Hot Reloading is possible by launching using the IntelliJ debugger and having [DCEVM 1.8](https://dcevm.github.io/) installed to your JVM. Then you can run a regular build and confirm the reload prompt. This can cause issues (especially with commands), so restarting is sometimes still necessary.

> Warning: Depending on your system configuration, you may need to install DCEVM onto an existing JVM. In that case you need to install a Java 1.8 JVM, specifically version 1.8u181 (although some newer versions up until 1.8u265 *may* work). These old JVM versions may require an Oracle account to download and you can find them [here](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html).

For quicker hot swapping or if the above does not work you can install [Single Hotswap](https://plugins.jetbrains.com/plugin/14832-single-hotswap). With this you can hot swap a single java class instead of rebuilding the entire project.This still requires DCEVM.

## Creating a new Release
<details>
<summary>Minimized, for your convenience</summary>

> **Release Types**
> 
> Right now we can create Full Releases, Pre Releases and Hotfixes.
> 
>  - A Full Release is sent to all users, regardless of update stream.
>  - A Pre Release is only sent to users who have opted into receiving beta updates.
>  - A Hotfix is only sent to users who have *not* opted into receiving beta updates. 
>       - Therefore when a bug is fixed in a hotfix update, it should *also* be fixed in a separate prerelease update.
>         On the other hand, not all bugs fixed in a prerelease update need to be also dispatched in a hotfix.

### Creating a new Full Release

> Full Releases should be bug free, feature complete, and ideally checked by not only the community, but also by Moulberry himself, if he so desires.

- Edit `NotEnoughUpdates.java` and change

```java
public static final String VERSION = "2.2.0-REL"; /* Update the VERSION name */
public static final int VERSION_ID = 20200; /* Set the VERSION_ID to match the version name like so: MAJOR * 10000 + MINOR * 100 + PATCH */
public static final int PRE_VERSION_ID = 0; /* Reset the PRE_VERSION_ID back to 0 */
public static final int HOTFIX_VERSION_ID = 0; /* Reset the HOTFIX_VERSION_ID back to 0 */
```

- Build a jar from this, either using the CI in github actions, or using `gradle remapJar` directly.
  - If building locally, make sure that all your changes are in version control so that the commit hash is set correctly (A non `dirty` jar)
- Create a github release (marked as full release). This should also simultaneously create a tag on which to base future hotfixes. 
- Edit the `update.json` in the repository and change

```json5
{
  "version": "2.1.0-REL", /* Update to match the VERSION name in java */
  "version_id": 20100, /* Update to match the VERSION_ID in java */
  "update_msg": "§7§m§l--------------------§6§l[§c§lNEU§6§l]§7§m§l--------------------\n\n§7A new version, v§6{version}§7, is now available!\n ", /* Update the version name. Remove old patch notes; Optionally add in a short new patch note. */
  "pre_version": "0.0", /* Reset to 0.0 */
  "pre_version_id": 0, /* Reset to 0 */
  "update_link": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/tag/<VERSIONNAME>", /* Change download link to the github release */
  "update_direct": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/download/<VERSIONNAME>/NotEnoughUpdates-<VERSIONNAME>.jar", /* Change direct link to a direct download link */
}
```

- Launch the game in an older version with this new repo locally to test the messages look first, then push to the central NEU repo (both `master` and `dangerous`)
- Create an announcement in discord [#neu-download](https://discord.com/channels/516977525906341928/693586404256645231).

### Creating a pre release

> Pre Releases are intended to be mostly feature complete, mostly bug free releases that either don't have enough changes to justify a new Full Release, or have outstanding PRs that are probably merged soon.

- Edit `NotEnoughUpdates.java` and change

```java
public static final String VERSION = "2.2.0-REL"; /* The VERSION name should still be the same as the latest previously released FULL release */
public static final int VERSION_ID = 20200; /* Same as VERSION name */
public static final int PRE_VERSION_ID = 1; /* Increment the PRE_VERSION_ID */
```

- Build a jar from this, either using the CI in github actions, or using `gradle remapJar` directly.
    - If building locally, make sure that all your changes are in version control so that the commit hash is set correctly (A non `dirty` jar)
- Create a github release (marked as pre release)
- Edit the `update.json` in the repository and change

```json5
{
  "version": "2.1.0-REL", /* The VERSION name should still be the same as the latest previously released FULL release */
  "version_id": 20100, /* Same as VERSION name */
  "pre_update_msg": "§7§m§l--------------------§5§l[§c§lNEU§5§l]§7§m§l--------------------\n\n§7A new pre-release, v§52.0-PRE{pre_version}§7, is now available!\n ", /* Update the version name. Remove old patch notes; Optionally add in a short new patch note. */
  "pre_version": "0.0", /* Set to a new string (preferably increase the major version every time, except for hotfixes on the prerelease stream) */
  "pre_version_id": 0, /* Set to PRE_VERSION_ID from java */
  "pre_update_link": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/tag/<VERSIONNAME>", /* Change download link to the github release */
  "pre_update_direct": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/download/<VERSIONNAME>/NotEnoughUpdates-<VERSIONNAME>.jar", /* Change direct link to a direct download link */
}
```

- Launch the game in an older version with this new repo locally to test the messages look first, then push to the central NEU repo (both `master` and `dangerous`, as some prerelease people sadly don't know how to change repo branches)
- Create an announcement in discord [#unofficial-prereleases](https://discord.com/channels/516977525906341928/837679819487313971).

### Creating a Hotfix

> Hotfixes spring off of a Full Release and intend to fix bugs and security flaws. They can, but ideally shouldn't, contain features from pre releases and are intended as a drop in replacement of the current full release of NEU. These bug fixes should ideally also be released as a prerelease in tandem with the hotfix.

- Edit `NotEnoughUpdates.java` and change

```java
public static final String VERSION = "2.2.0-REL"; /* The VERSION name should still be the same as the latest previously released FULL release */
public static final int VERSION_ID = 20200; /* Same as VERSION name */
public static final int PRE_VERSION_ID = 0; /* The PRE_VERSION_ID should still be 0 (as this is based off a full release) */
public static final int HOTFIX_VERSION_ID = 1; /* Increment the HOTFIX_VERSION_ID */
```

- Build a jar from this, either using the CI in github actions, or using `gradle remapJar` directly.
    - If building locally, make sure that all your changes are in version control so that the commit hash is set correctly (A non `dirty` jar)
- Create a github release (marked as full release)
- Edit the previous FULL release on github with a link to the new release.
- Edit the `update.json` in the repository and change

```json5
{
  "version": "2.1.0-REL", /* This version should still remain the same as the last full release */
  "version_id": 20100, /* Same as version */
  "update_msg": "§7§m§l--------------------§6§l[§c§lNEU§6§l]§7§m§l--------------------\n\n§7A new version, v§6{version}§7, is now available!\n ", /* Update the version name. Don't  remove old patch notes; Optionally add in a short new patch note. Indicate that there is a hotfix present */
  "update_link": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/tag/<VERSIONNAME>", /* Change download link to the github release */
  "update_direct": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/download/<VERSIONNAME>/NotEnoughUpdates-<VERSIONNAME>.jar", /* Change direct link to a direct download link */
}
```

- Launch the game in an older version with this new repo locally to test the messages look first, then push to the central NEU repo (both `master` and `dangerous`)
- Create an announcement in discord [#neu-download](https://discord.com/channels/516977525906341928/693586404256645231).
</details>
