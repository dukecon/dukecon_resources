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
                    // TODO add clean as soon as we have archived most of the "old" data and/or have a minimum size for data files or other error check
                    sh 'mvn verify -Pdocker'
                }
            }
        }
    }
    post {
        always {
            sendNotification currentBuild.result
        }
        post {
            failure {
                mail to: 'gerd@aschemann.net',
                        subject: "Failed DukeCon resources Pipeline: ${currentBuild.fullDisplayName}",
                        body: "Something is wrong with ${env.BUILD_URL}"
            }
        }
    }
}
