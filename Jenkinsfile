#!/usr/bin/env groovy
@Library('jenkins-library@master') _

pipeline {
    agent {
        node {
            label 'docker'
        }
    }

    triggers {
        pollSCM('* * * * *')
    }

    stages {
        stage('Clean / Build / Deploy (on master)') {
            when {
                branch 'master'
            }
            steps {
                withMaven {
                    sh 'mvn clean deploy'
                }
            }
        }
        stage('Clean / Build / Verify (on feature branch(es)') {
            when {
                branch 'feature/*'
            }
            steps {
                withMaven {
                    // TODO Move the docker:push to the master branch once it is merged
                    sh 'mvn clean verify -Pdocker docker:push'
                }
            }
        }
    }
    post {
        always {
            sendNotification currentBuild.result
        }
        failure {
            mail to: 'gerd@aschemann.net',
                    subject: "Failed DukeCon resources Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
}
