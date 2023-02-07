pipeline {
    agent any
    environment {
        // Setup Ruby to PATH
        RUBY_HOME = "/usr/local/opt/ruby"
        PATH = "$RUBY_HOME/bin:$PATH"
        LANG = "en_US.UTF-8"
    }

    options {
        // Stop the build early in case of compile or test failures
        skipStagesAfterUnstable()
    }
    stages{
        stage('Setup') {
            steps {
                script {
                    sh 'gem install bundler'
                    sh 'bundle install'
                    // Copy node env file to export environment variables
                    withCredentials([file(credentialsId: 'env-default', variable: 'env')]) {
                        sh 'rm -f .env.default'
                        sh 'cp $env .env.default'
                    }
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh 'bundle exec fastlane build'
                }
            }
        }
        stage('Parallel Stage') {
            failFast true
            parallel {
                stage('Test') {
                    steps {
                        script {
                            sh 'bundle exec fastlane unit_test'
                        }
                    }
                }
                stage('Check') {
                    steps {
                        script {
                            sh 'bundle exec fastlane check'
                        }
                    }
                }
            }
        }
    }
    post {
        cleanup {
            cleanWs()
        }
    }
}