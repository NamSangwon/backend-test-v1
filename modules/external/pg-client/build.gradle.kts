val springCloudVersion by extra("2024.0.2")
tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(projects.modules.application)
    implementation(projects.modules.domain)
    implementation(libs.spring.boot.starter.web)
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}
