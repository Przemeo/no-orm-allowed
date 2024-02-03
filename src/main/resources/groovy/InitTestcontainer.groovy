package groovy

import org.testcontainers.containers.PostgreSQLContainer

def container = new PostgreSQLContainer<>("postgres:latest")
        .withUsername(project.properties.getProperty("db.username"))
        .withDatabaseName("querydsl")
        .withPassword(project.properties.getProperty("db.password"))
container.start()

project.properties.setProperty('db.url', container.getJdbcUrl())
project.properties.setProperty('testcontainer.containerid', container.getContainerId())
project.properties.setProperty('testcontainer.imageName', container.getDockerImageName())