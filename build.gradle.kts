plugins {
  id("org.springframework.boot") version "3.3.2"
  id("io.spring.dependency-management") version "1.1.6"
  java
}
group = "com.estapar"
version = "1.0.0"
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
repositories { mavenCentral() }
dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  compileOnly ("org.projectlombok:lombok")
  annotationProcessor ("org.projectlombok:lombok")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("com.mysql:mysql-connector-j:9.0.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
  testImplementation("org.mockito:mockito-core:5.12.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
  testImplementation("org.assertj:assertj-core:3.26.0")
}
tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed","skipped","failed")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showCauses = true
    showExceptions = true
    showStackTraces = true
  }
}