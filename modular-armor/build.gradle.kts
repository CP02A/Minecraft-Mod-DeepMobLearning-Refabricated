val palVersion: String by project

dependencies {
    api(project(":base"))

    api("maven.modrinth:pal:${palVersion}")
    include("maven.modrinth:pal:${palVersion}")
}
