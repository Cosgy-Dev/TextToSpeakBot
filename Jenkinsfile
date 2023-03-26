pipeline {
   environment {
      QODANA_TOKEN=credentials('qodana-token')
      QODANA_REMOTE_URL="${GIT_URL}"
      QODANA_BRANCH="${GIT_BRANCH}"
      QODANA_REVISION="${GIT_COMMIT}"
      QODANA_JOB_URL="${JOB_DISPLAY_URL}"
   }
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
            sh '''qodana'''
         }
      }
   }
}