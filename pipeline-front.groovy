pipeline {
    agent {
        label 'jenkins_slave'
    }
    tools {
        nodejs 'node20'
    }
    environment {
        workspace = "/data/"
    }
    stages {
        stage("Clean") {
            steps {
                echo "Limpiando el escenario"
                cleanWs()
            }
        }
        stage("Download proyecto") {
            steps {
                echo "Descargando proyecto"
                git credentialsId: 'git_credentials', branch: "dev", url: "https://github.com/Camilojavier/vue-tarea-crud.git"
                echo "Proyecto descargado"
            }
        }
        stage("Build proyecto") {
            steps {
                echo "Iniciando build"
                sh "npm install"
                sh "npm run build"
                sh "tar -czf project_files.tar.gz node_modules *.json"
                stash includes: 'project_files.tar.gz', name: 'frontartifact'
                archiveArtifacts artifacts: 'project_files.tar.gz', onlyIfSuccessful:true
                sh "cp project_files.tar.gz /tmp/"
            }
        }
        stage("Test de vulnerabilidades") {
            steps {
                echo "escaneando vulnerabilidades"
                sh "ls"
                sh "/grype /tmp/project_files.tar.gz > informe-scan.txt"
                sh "pwd"
                archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful:true
            }
        }
    }
}