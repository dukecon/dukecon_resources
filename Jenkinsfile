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
        stage('Clean / Build / Deploy (on develop)') {
            when {
                branch 'develop'
            }
            steps {
                withMaven {
                    sh 'mvn clean deploy -Pdocker docker:push'
                    build 'docker_restart_conference-archive'
                }
            }
        }
        stage('Clean / Build / Verify (on feature branch(es)') {
            when {
                branch 'feature/*'
            }
            steps {
                withMaven {
                    sh 'mvn clean verify'
                }
            }
        }
        stage('Publish documentation') {
            steps {
                publishHTML target: [allowMissing         : false,
                                     alwaysLinkToLastBuild: false,
                                     keepAll              : true,
                                     reportDir            : 'target/generated-docs/',
                                     reportFiles          : 'index.html',
                                     reportName           : 'Documentation']
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
