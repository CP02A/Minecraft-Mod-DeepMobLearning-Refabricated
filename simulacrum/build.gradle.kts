val modmenuVersion: String by project

dependencies {
    api(project(":base"))

    compileOnly("com.terraformersmc:modmenu:${modmenuVersion}")
}
