# PK-Tutorial
Welcome to my PK addon ability tutorial. Here you will learn how to create ProjectKorra addon abilities. I've enclosed four abilities in this package, one for each element. I'm hoping it will help some people to understand how to make abilities and what tools are available to them.
Please read this README if you don't know where to start. If you're looking for example abilities, go right in the package.

## Why I made this tutorial
A lot of people have asked me how to make an ability, and there is a dearth of really helpful PK tutorials out there. I thought it would be useful for people to see examples with extensive documentation. I didn't make this tutorial to teach people Java, so if you don't know Java at all, you should learn at least the basics. There are plenty of Java tutorials, IDE tutorials, and even Bukkit/Spigot/Paper API tutorials. I'm here to show you what lacks sufficient tutorials.

## Getting started
First, you need to have some amount of Java knowledge. If you don't, then please watch a YouTube tutorial or something.
Second, you need an IDE, or Integrated Development Environment, software that helps you write software. I use and recommend Eclipse. Another popular choice is IntelliJ.
Third, you need to have an idea for an ability you want to make.

1. Open your IDE and create a new Java project with the name of your ability. Technically, it doesn't need to match the name of your ability, but it should.
2. Add external JARS to your build path. I don't know how one does this in other IDEs, but in Eclipse, you right click on the project, Configure > Build Path > Add external jars. You need to add two jar files: a Spigot jar file and a ProjectKorra jar file. These are the same files you would put in a server directory, and you can find them online. This effectively imports those files into your project so you can use classes from them.
3. Create a new package in your project, preferably by following a package naming convention. I used me.aztl.pktutorial for this. ProjectKorra's base package is com.projectkorra.projectkorra. Technically this step is optional, but it's not recommended to use the default package.
4. Create a new class in your package. It should extend an ability class such as AirAbility and implement at least AddonAbility. If you can't import those, fix your build path. Use Ctrl + Shift + O in Eclipse to automatically fix imports (add new ones, remove old ones).
5. Generate a constructor and unimplemented methods.

## Following one of the examples
If you're new to PK, I'd recommend familiarizing yourself with the PK API by going through my examples in the following order:

1. AirJump
2. WaterTendril
3. FireShots
4. EarthRidge

Air and fire abilities are typically easier to make, water is not as easy, and earth is usually the hardest.
These abilities just scratch the surface of what you're able to do with ProjectKorra. For more example abilities (but less documentation), you can study the code of a number of addons and side plugins, including my plugin,

[Azutoru](https://github.com/Aztlon/Azutoru).

Also see:
* [ProjectKorra](https://github.com/ProjectKorra/ProjectKorra)
* [Hiro3's addons](https://github.com/EmreNtm/ProjectKorra-Addon-Abilities)
* Moros's side plugin [Hyperion](https://github.com/PrimordialMoros/Hyperion)
* Simplicitee's side plugin [ProjectAddons](https://github.com/Simplicitee/ProjectAddons)
* [JedCore](https://github.com/Aztlon/JedCore)

## Additional help
If you have a question that isn't answered anywhere in the repository, you can ask the question in [ProjectKorra's Discord](https://discord.gg/pPJe5p3) in the #development-support channel.
If you have a question specifically for me, or you have no one else to turn to, you can ask DM me on Discord: Aztl#0001.