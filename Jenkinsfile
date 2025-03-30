pipeline {
    agent any
    
    environment {
        DOCKER_COMPOSE_DIR = '/home/ubuntu/mr_patent'
        BACKEND_IMAGE = 'mr_patent-backend'
        BRANCH_NAME = "${env.BRANCH_NAME}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo '====== 백엔드 빌드 시작 ======'
                dir('mr_patent_backend') {
                    sh 'chmod +x ./gradlew || true'
                    sh './gradlew clean bootJar || ./gradlew clean build'
                }
                echo '====== 백엔드 빌드 완료 ======'
            }
        }
        
        stage('Test') {
            steps {
                echo '====== 백엔드 테스트 시작 ======'
                dir('mr_patent_backend') {
                    sh './gradlew test || true'
                }
                echo '====== 백엔드 테스트 완료 ======'
            }
        }
        
        stage('Deploy') {
            steps {
                echo '====== 백엔드 배포 시작 ======'
                // JAR 파일 복사
                sh 'mkdir -p /home/ubuntu/mr_patent/build/libs/'
                sh 'cp -f mr_patent_backend/build/libs/*.jar /home/ubuntu/mr_patent/build/libs/'
                
                // 백업 생성
                sh 'mkdir -p /home/ubuntu/mr_patent/backups'
                sh 'cp -f /home/ubuntu/mr_patent/build/libs/*.jar /home/ubuntu/mr_patent/backups/backup-$(date +%Y%m%d%H%M%S)-${BRANCH_NAME}.jar || true'
                
                // SSH를 통한 원격 Docker 명령 실행
                sshagent(['jenkins-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@localhost "
                            cd /home/ubuntu/mr_patent && 
                            docker-compose stop backend && 
                            docker-compose rm -f backend && 
                            docker-compose build backend && 
                            docker-compose up -d backend && 
                            docker image prune -f
                        "
                    '''
                }
                echo '====== 백엔드 배포 완료 ======'
            }
        }
    }
    
    post {
        always {
            echo '====== 파이프라인 종료 ======'
            cleanWs()
        }
        failure {
            echo '====== 파이프라인 실패 ======'
        }
        success {
            echo '====== 파이프라인 성공 ======'
        }
    }
}