package groovy

import org.testcontainers.utility.ResourceReaper

String containerId = project.properties.getProperty("testcontainer.containerid")
String imageName = project.properties.getProperty("testcontainer.imageName")

log.info("Stopping Testcontainer: $containerId - $imageName")
//noinspection GrDeprecatedAPIUsage
ResourceReaper.instance()
        .stopAndRemoveContainer(containerId, imageName)