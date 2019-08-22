#!/usr/bin/env groovy
def config = [
        credentialsId: "github",
        deployTo: 'maven-central',
        openShiftBuild: false,
        scriptVersion : 'v6',
        javaVersion : '8',
        sonarQube: false,
        pipelineScript : 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git',
        versionStrategy : [
                [branch: 'master', versionHint: '1']
        ]
]
fileLoader.withGit(config.pipelineScript, config.scriptVersion) {
   jenkinsfile = fileLoader.load('templates/leveransepakke')
}

jenkinsfile.run(config.scriptVersion, config)
