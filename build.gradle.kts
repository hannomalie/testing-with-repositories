plugins {
    id("java")
}

group = "de.hanno.testing-repositories"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:6.1.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.6.0")

    implementation("org.jdbi:jdbi3-core:3.45.1")
    implementation("org.jdbi:jdbi3-sqlobject:3.45.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("org.mockito:mockito-core:5.11.0")

    testImplementation("org.testcontainers:postgresql:1.19.3")

    testImplementation("io.javalin:javalin-bundle:6.1.3")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("test.prefer-in-memory", project.findProperty("test.prefer-in-memory")?.toString() ?: "true")
}