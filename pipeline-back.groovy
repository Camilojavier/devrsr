pipeline {
    agent {
        label 'jenkins_slave'
    }
    tools {
        maven 'maven-394'
        jdk 'jdk21'
    }
    environment {
        workspace="/data/"
    }
    stages {
        stage("Clean") {
            steps {
                echo "limpiando escenario"
                cleanWs()
            }
        }
        stage("Download proyect") {
            steps {
                echo "descargando proyecto"
                git credentialsId: 'git_credentials', branch: "main", url: "https://github.com/Camilojavier/proyectoFinal.git"
                echo "proyecto descargado"
            }
        }
        stage("Build proyect") {
            steps {
                echo "iniciando build"
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package -Dmaven.test.skip=true -U"
                sh "pwd"
                sh "mv target/*.jar target/app.jar"
                stash includes: 'target/app.jar', name: "backartifact"
                archiveArtifacts artifacts: 'target/app.jar', onlyIfSuccessful:true
                sh "cp target/app.jar /tmp/"
            }
        }
        stage("Test vulnerability") {
            steps {
                sh "/grype /tmp/app.jar > informe-scan.txt"
                sh "pwd"
                archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful:true
            }
        }
         stage('sonarqube analysis'){
            steps{
               script{
                   sh "pwd"
						writeFile encoding: 'UTF-8', file: 'sonar-project.properties', text: """sonar.projectKey=openid-back
						sonar.projectName=openid-back
						sonar.projectVersion=openid-back
						sonar.sourceEncoding=UTF-8
						sonar.sources=src/main/
						sonar.java.binaries=target/
						sonar.java.libraries=target/classes
						sonar.language=java
						sonar.scm.provider=git
						"""
                        // Sonar Disabled due to we don't have a sonar in tools account yet
						withSonarQubeEnv('Sonar_CI') {
						     def scannerHome = tool 'Sonar_CI'
						     sh "${tool("Sonar_CI")}/bin/sonar-scanner -X"
						}
               
                   
               }
        
            }
        }
        stage("Image push artifactory") {
            agent any
            steps{
                script{
                    unstash 'backartifact'
                    sh "sshpass -p password scp /data/jenkins_home/workspace/APP-DEV/build_app-back/target/app.jar userver@192.168.138.3:/home/userver/"
                }
            }
        }

    }
}
