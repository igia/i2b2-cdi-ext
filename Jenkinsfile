
@Library('ppm-shared-jenkins-library') _

standardDockerBuildPipeline {
        dockerImageName = "igia/i2b2-cdi-app"
        skipTest = false
        skipDeployment = true
        skipSonarQubeAnalysis = false
        skipLicenseHeaderCheck = false
        skipBuildDockerImage = false
        skipPushDockerImage = false
        deployArtifactToNexus = true
        dockerProjectDir = "./i2b2-cdi-app"
}
