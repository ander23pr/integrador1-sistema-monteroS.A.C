$ErrorActionPreference = "Stop"
$baseDir = "c:\Users\sanan\OneDrive\Documentos\ciclo 8\ciclo 8\Curso Integrador 1 Sistemas Software\interfaces- aplicacion montero\interfaces de la aplicacion"
Set-Location $baseDir

# 1. Create Java package structure
$javaBase = "src/main/java/com/montero/app"
$packages = @("controller", "service", "service/impl", "repository", "model", "dto", "config", "exception", "util", "security")

foreach ($pkg in $packages) {
    $path = Join-Path $javaBase $pkg
    New-Item -ItemType Directory -Force -Path $path | Out-Null
}

# 2. Create Resources structure
$resBase = "src/main/resources"
$resFolders = @("templates", "static/css", "static/js", "static/img")

foreach ($fld in $resFolders) {
    $path = Join-Path $resBase $fld
    New-Item -ItemType Directory -Force -Path $path | Out-Null
}

# 3. application.properties
$props = @"
spring.datasource.url=jdbc:mysql://localhost:3306/montero_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
"@
Set-Content -Path "$resBase/application.properties" -Value $props -Encoding UTF8

# 4. pom.xml
$pom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.montero</groupId>
    <artifactId>montero-app</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>montero-app</name>
    <description>Aplicacion Movil de Administracion de Viajes para Transportes MONTERO S.A.C.</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <!-- Spring Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"@
Set-Content -Path "pom.xml" -Value $pom -Encoding UTF8

# 5. Main Application Class
$mainClass = @"
package com.montero.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonteroApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonteroApplication.class, args);
    }
}
"@
Set-Content -Path "$javaBase/MonteroApplication.java" -Value $mainClass -Encoding UTF8

# 6. Move HTML files and format for Thymeleaf
$excludeDirs = @("src", ".git", ".idea", "target")

# Get all directories in the base dir
$dirs = Get-ChildItem -Path $baseDir -Directory

foreach ($dir in $dirs) {
    if ($excludeDirs -notcontains $dir.Name) {
        $codePath = Join-Path $dir.FullName "code.html"
        if (Test-Path $codePath) {
            $destName = $dir.Name.ToLower() + ".html"
            $destPath = Join-Path "$baseDir/$resBase/templates" $destName
            
            $content = Get-Content -Path $codePath -Raw -Encoding UTF8
            $content = $content -replace '<html lang="es">', '<html lang="es" xmlns:th="http://www.thymeleaf.org">'
            
            Set-Content -Path $destPath -Value $content -Encoding UTF8
        }
    }
}

Write-Host "Setup Complete"
