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
    stage('Clean / Build / Deploy') {
       steps {
         withMaven {
           sh 'mvn clean deploy'
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
