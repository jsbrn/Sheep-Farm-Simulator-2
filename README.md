# Sheep Farm Simulator 2.0 (Working Title) #

On a whim I decided to start fresh and redo the original project, Sheep Farm Simulator, to run extremely well and not crash every 30 seconds. Also completely revamping the game mechanics and making it fun for once.

Right now, I've got a couple other projects on my plate, so I've put this one on hold. It's a tech demo of what I learned doing it, basically.

### What makes this project cool? ###

1. This project features large-scale terrain generation using a procedural algorithm. It uses simplex noise to scatter the world with overlapping biomes. See `com/bitbucket/computerology/world/terrain`.

2. I wrote a custom GUI framework to use in the game. I think it looks pretty nice, visually, but what I am most proud of is the ability to anchor components to each other and add subcomponents to panels. All of it can be done in a fairly straightforward way, making it easy and quick to make some pretty complex menu systems. See `com/bitbucket/computerology/gui`.

### How do I try it? ###

1. Pull the project, and import into your favorite IDE (I recommend IntelliJ IDEA).

2. Install slick.jar in lib/ locally (Maven).

3. Make some changes.

4. Use Maven commands or your IDE to build/deploy/run the game. In IntelliJ, the 'install' task can be found in View -> Tool Windows -> Maven.

### What libraries do I need? ###

This game has the following requirements:

1. Java 6+
2. LWJGL 2.9.3 (via Maven)
3. Slick2D (unidentified version, included in repo for convenience)
