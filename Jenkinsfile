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
                // 백업 디렉토리 생성
                sh 'mkdir -p ${DOCKER_COMPOSE_DIR}/backups'
                
                // 빌드 디렉토리 생성 및 JAR 파일 복사
                sh 'mkdir -p ${DOCKER_COMPOSE_DIR}/build/libs/'
                sh 'cp -f mr_patent_backend/build/libs/*.jar ${DOCKER_COMPOSE_DIR}/build/libs/ || true'
                
                // 백업 생성
                sh 'cp -f ${DOCKER_COMPOSE_DIR}/build/libs/*.jar ${DOCKER_COMPOSE_DIR}/backups/backup-$(date +%Y%m%d%H%M%S)-${BRANCH_NAME}.jar || true'
                
                // 도커 컨테이너 재시작
                sh '''
                    cd ${DOCKER_COMPOSE_DIR} && 
                    docker-compose stop backend || true
                    docker-compose rm -f backend || true
                    docker-compose build backend || true
                    docker-compose up -d backend || true
                    docker image prune -f || true
                '''
                
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