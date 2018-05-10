#!/usr/bin/env groovy

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
           sh 'mvn clean deploy -Pgenerate-all-public'
        }
      }
    }
  }
  post {
    failure {
      mail to: 'gerd@aschemann.net',
        subject: "Failed DukeCon resources Pipeline: ${currentBuild.fullDisplayName}",
        body: "Something is wrong with ${env.BUILD_URL}"
    }
  }
}
