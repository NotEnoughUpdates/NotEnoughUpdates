## Before you contribute

 - Please check your feature / bug isn't already fixed in one of our pre-releases or on [the development branch](https://github.com/NotEnoughUpdates/NotEnoughUpdates/tree/master/).
 - Consider joining our [Discord](https://discord.gg/moulberry) to check in on newest developments by other people, or to get help with problems you encounter.
 - Please check that your feature idea complies with the [Hypixel Rules](https://hypixel.net/rules)
 - Check that your feature idea isn't already done in other mods. (E.g. Dungeon Solver)
 
## Setting up a development environment

### Software prerequisites

 - Install a Java Development Kit (You will need both version 8 and version 17) [Eclipse Temurin Download](https://adoptium.net/temurin/releases) for convenience, however any JDK will do.
 - Install Git. [Windows download](https://git-scm.com/download/win)
 - Install an IDE, such as [Jetbrains IntelliJ IDEA](https://www.jetbrains.com/idea/download).

### Software configuration

 - Clone the NEU repository using `git clone https://github.com/NotEnoughUpdates/NotEnoughUpdates`.
 - Import that folder as a Gradle Project in your IDE (IntelliJ should autodetect it as gradle if you select the `NotEnoughUpdates` folder in the Open dialog)
 - Set your project SDK to your 1.8 JDK. This can be done in the modules settings (CTRL+ALT+SHIFT+S) in IntelliJ.
 - Set your gradle JVM to your 1.17 JDK. This can be done by searching for `gradle jvm` in the CTRL+SHIFT+A dialog in IntelliJ.
 - Run the `gen<IntelliJ/Eclipse>Runs` gradle task. In IntelliJ that can be done in the gradle tab on the right side of your IDE.
 - Optionally, run the `genSources` gradle task.
 - Run the `Minecraft Client` to make sure that everything works.


## Logging into Hypixel in a development environment.

Use [DevAuth](https://github.com/DJtheRedstoner/DevAuth). Download the `forge-legacy` version, and put it into the  `run/mods` folder. Then follow the configuration instructions in the [DevAuth README](https://github.com/DJtheRedstoner/DevAuth#configuration-file)

## Hot Reloading

Hot Reloading is possible by first launching using the IntelliJ debugger with [DCEVM 1.8](https://dcevm.github.io/). Then running a regular build and confirming the reload prompt. This can cause issues (especially with commands), so restarting is sometimes still necessary. 

