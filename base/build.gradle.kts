val libguiVersion: String by project
val energyVersion: String by project
val reiVersion: String by project
val jeiVersion: String by project

dependencies {
    // LibGui 26.3 is not on a reachable Maven: the official release jar from GitHub, in the local repository libs/maven
    api("io.github.cottonmc:LibGui:${libguiVersion}")
    include("io.github.cottonmc:LibGui:${libguiVersion}")

    api("teamreborn:energy:${energyVersion}")
    include("teamreborn:energy:${energyVersion}")

    compileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${reiVersion}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${reiVersion}")
    compileOnly("mezz.jei:jei-26.3-fabric-api:${jeiVersion}")
    // Point/Rectangle of the REI API (only a runtime dependency of the API jar)
    compileOnly("me.shedaniel.cloth:basic-math:0.6.1")
}
