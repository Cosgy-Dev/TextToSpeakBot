pipeline {
  agent {
    docker {
      args '''
              -v "${WORKSPACE}":/data/project
              --entrypoint=""
              '''
      image 'jetbrains/qodana-jvm'
    }

  }
  stages {
    stage('Qodana') {
      steps {
        sh 'qodana'
      }
    }

    stage('Build') {
      agent {
        docker {
          image 'maven:3.9.3-eclipse-temurin-17-alpine'
          args '-v /root/.m2:/root/.m2'
        }

      }
      steps {
        sh 'mvn -B -DskipTests clean package'
      }
    }

  }
  environment {
    QODANA_TOKEN = credentials('qodana-token')
    QODANA_REMOTE_URL = "${GIT_URL}"
    QODANA_BRANCH = "${GIT_BRANCH}"
    QODANA_REVISION = "${GIT_COMMIT}"
    QODANA_JOB_URL = "${JOB_DISPLAY_URL}"
  }
}