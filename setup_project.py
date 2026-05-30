import os
import shutil

base_dir = r"c:\Users\sanan\OneDrive\Documentos\ciclo 8\ciclo 8\Curso Integrador 1 Sistemas Software\interfaces- aplicacion montero\interfaces de la aplicacion"
os.chdir(base_dir)

# 1. Create Java package structure
java_base = "src/main/java/com/montero/app"
packages = [
    "controller", "service", "service/impl", "repository",
    "model", "dto", "config", "exception", "util", "security"
]

for pkg in packages:
    os.makedirs(os.path.join(java_base, pkg), exist_ok=True)

# 2. Create Resources structure
res_base = "src/main/resources"
res_folders = [
    "templates", "static/css", "static/js", "static/img"
]

for fld in res_folders:
    os.makedirs(os.path.join(res_base, fld), exist_ok=True)

# 3. application.properties
props = """spring.datasource.url=jdbc:mysql://localhost:3306/montero_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
"""
with open(os.path.join(res_base, "application.properties"), "w", encoding="utf-8") as f:
    f.write(props)

# 4. pom.xml
pom = """<?xml version="1.0" encoding="UTF-8"?>
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
"""
with open("pom.xml", "w", encoding="utf-8") as f:
    f.write(pom)

# 5. Main Application Class
main_class = """package com.montero.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonteroApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonteroApplication.class, args);
    }
}
"""
with open(os.path.join(java_base, "MonteroApplication.java"), "w", encoding="utf-8") as f:
    f.write(main_class)

# 6. Move HTML files and format for Thymeleaf
import glob

exclude_dirs = ["src", ".git", ".idea", "target"]
for root, dirs, files in os.walk(base_dir):
    # Only look at top level dirs
    if root == base_dir:
        for d in dirs:
            if d not in exclude_dirs:
                code_path = os.path.join(root, d, "code.html")
                if os.path.exists(code_path):
                    # Destination filename
                    dest_name = d.lower() + ".html"
                    dest_path = os.path.join(res_base, "templates", dest_name)
                    
                    # Read, modify and write
                    with open(code_path, "r", encoding="utf-8") as infile:
                        content = infile.read()
                        
                        # Basic Thymeleaf preparation
                        content = content.replace('<html lang="es">', '<html lang="es" xmlns:th="http://www.thymeleaf.org">')
                        # We could also add generic th:href replacements but let's just do the xmlns first
                        
                    with open(dest_path, "w", encoding="utf-8") as outfile:
                        outfile.write(content)
        break # only process top level directories for folders

print("Setup Complete")
