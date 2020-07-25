def remote = [:]
remote.name = "raspberrypi"
remote.host = "ssautohome.hopto.org"
remote.allowAnyHosts = true
def oldImageId = ""
def dockerUsername = ""
def dockerPassword = ""
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
            	echo "*****************Build*****************"
            	dir("${WORKSPACE}"){
	                echo "Gradle build started"
	                sh "./gradlew build -x test"
	                echo "Gradle build completed"
            	}
            }
        }
        stage('Setting Credentials') {
            steps {
            	echo "**********Fetching Credentials**********"
            	withCredentials([usernamePassword(credentialsId: 'Rpi-ssh-cred', passwordVariable: 'RPI_PASSWORD', usernameVariable: 'RPI_USERNAME')]) {
            		script{
	            		remote.user = "$RPI_USERNAME"
						remote.password = "$RPI_PASSWORD"
            		}
			    }
			    echo "Setting Rpi SSH credentials completed"
			    withCredentials([usernamePassword(credentialsId: 'Docker-cred', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
            		script{
	            		dockerUsername = "$DOCKER_USERNAME"
						dockerPassword = "$DOCKER_PASSWORD"
            		}
			    }
            }
        }
        stage('Copying JAR To Server') {
            steps {
            	echo "**********Copying JAR To Server**********"
            	echo "Copying JAR started"
	            sshPut remote: remote, from: 'build/libs/AdminPortal.jar', into: './springAdmin/jar/'
			    echo "Copying JAR completed"
            }
        }
        stage('Docker Image Creation'){
        	steps{
        		echo "**********Docker Image Creation**********"
	    		echo "Copying Dockerfile"
	            sshPut remote: remote, from: 'Dockerfile', into: './springAdmin'
	            echo "Building docker image"
	            sshCommand remote: remote, command: 'docker build /home/jenkins/springAdmin -t sethusuresh/spring_admin'
	            echo "Stopping the old container"
	            sshCommand remote: remote, command: 'docker stop spring_admin || true'
	            echo "Removing the old container"
	            sshCommand remote: remote, command: 'docker rm spring_admin || true'
	            script{
		            oldImageId = sshCommand remote: remote, command: 'docker images -qa -f "dangling=true" || true'
		            if(oldImageId != null && !oldImageId.trim().isEmpty()){
		            	echo "Removing the old docker image:- ${oldImageId}"
			            sshCommand remote: remote, command: "docker rmi ${oldImageId}"
                  	}
	            }
        	}
        }
        stage('Pushing Image to DockerHub') {
            steps {
            	echo "**********Pushing Image to DockerHub**********"
            	echo "Logging into DockerHub"
	            sshCommand remote: remote, command: "docker login -u ${dockerUsername} -p ${dockerPassword}"
	            echo "Pushing latest image to DockerHub"
	            sshCommand remote: remote, command: 'docker push sethusuresh/spring_admin'
	            echo "Logging out from DockerHub"
	            sshCommand remote: remote, command: 'docker logout'
            }
        }
        stage('Deploy') {
            steps {
            	echo "**********Deploy**********"
	            echo "Starting the latest docker container"
	            sshCommand remote: remote, command: 'docker run --name spring_admin -p 8081:8081 -d sethusuresh/spring_admin'
            }
        }
    }
    post { 
        always { 
            cleanWs()
        }
    }
}







