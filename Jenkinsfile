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
                sh 'chmod +x ./gradlew'
                sh './gradlew clean bootJar'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        
        stage('Deploy') {
            steps {
                // 백업 생성
                sh 'mkdir -p ${DOCKER_COMPOSE_DIR}/backups'
                sh 'cp -f ${DOCKER_COMPOSE_DIR}/build/libs/*.jar ${DOCKER_COMPOSE_DIR}/backups/backup-$(date +%Y%m%d%H%M%S)-${BRANCH_NAME}.jar || true'
                
                // 새 빌드 파일 복사
                sh 'cp -f build/libs/*.jar ${DOCKER_COMPOSE_DIR}/build/libs/'
                
                // 도커 컨테이너 재시작
                sh 'cd ${DOCKER_COMPOSE_DIR} && docker-compose stop backend'
                sh 'cd ${DOCKER_COMPOSE_DIR} && docker-compose rm -f backend'
                sh 'cd ${DOCKER_COMPOSE_DIR} && docker-compose build backend'
                sh 'cd ${DOCKER_COMPOSE_DIR} && docker-compose up -d backend'
                
                // 사용하지 않는 이미지 정리
                sh 'docker image prune -f'
                
                echo "배포 완료: ${BRANCH_NAME} 브랜치"
            }
        }
    }
    
    post {
        success {
            echo '파이프라인이 성공적으로 완료되었습니다!'
        }
        failure {
            echo '파이프라인 실행 중 오류가 발생했습니다!'
        }
    }
}