pipeline {
    agent any
    //{
    //  label 'jenkins_slave'
    //}
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
            }
        }
        stage("Test de vulnerabilidades") {
            steps {
                echo "escaneando vulnerabilidades"
            }
        }
    }
}
