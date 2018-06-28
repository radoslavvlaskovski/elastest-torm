    node('TESTDOCKER'){
        
        stage "CI Container setup"

            try {

                echo("the node is up")
                def mycontainer = docker.image('elastest/ci-docker-compose-siblings:node7-npm4')
                mycontainer.pull() // make sure we have the latest available from Docker Hub
                mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw -v ${WORKSPACE}:/home/jenkins/.m2 -v /home/ubuntu/.gnupg:/home/jenkins/.gnupg") {
                def epmClientJavaDirectory = 'epm-client-java'
                //withMaven(maven: 'mvn3.3.9', mavenSettingsConfig: '0e7fd7e6-77b0-4ea2-b808-e8164667a6eb') {
                    stage "Install et-epm-client-java"
                        def epmClientDirectoryExists = fileExists epmClientJavaDirectory
                        if (epmClientDirectoryExists) {
                            echo 'EPM client directory exists'
                        } else {
                            echo 'There isn not EPM client directory'
                            sh 'mkdir ' + epmClientJavaDirectory
                        }
                        
                        dir(epmClientJavaDirectory) {
                            echo 'Existing files before cloning the git repository'
                            git 'https://github.com/franciscoRdiaz/epm-client-java.git'
                        }
                        
                        echo 'Installing epm-client-java'
                            sh 'export PATH=$MVN_CMD_DIR:$PATH;ls -lrt; cd epm-client-java; mvn clean install -Dmaven.test.skip=true'
                        

                    git url: 'https://github.com/elastest/elastest-torm.git', branch: 'et-mini'

                    // stage "Test and deploy epm-client"
                    //     echo ("Test and deploy epm-client")
                    //     sh 'cd ./epm-client; mvn deploy -Djenkins=true;'
                        
                    
                    stage "Build elastest-torm-gui"
                        echo ("Build elastest-torm-gui")                        
                        sh 'export PATH=$MVN_CMD_DIR:$PATH;cd ./elastest-torm-gui; npm install; mvn package;'
                        
                    
                    // stage "Build elastest-torm"
                    //     echo ("Build elastest-torm")
                    //     sh 'cd ./elastest-torm; mvn -Pci package;'
                        
                    
                    //  stage "Unit Test elastest-torm"
                    //     echo ("Starting TORM unit tests")
                    //     withMaven(maven: 'mvn3.3.9', mavenSettingsConfig: '0e7fd7e6-77b0-4ea2-b808-e8164667a6eb') {    
                    //         sh 'cd ./elastest-torm; mvn -Pci-no-it-test test;'
                    //         step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
                    //     }
                        
                    // stage "Test DB changes"
                    //     echo ("Test DB changes")
                    //     sh 'cd ./scripts/db; ls -lrt; chmod +777 test-liquibase-changelogs.sh;./test-liquibase-changelogs.sh;' 
                        
                    // stage ("IT Test elastest-torm")
                    //     echo ("Starting TORM integration tests")
                    //     try{
                    //         sh 'cd ./scripts; ./it.sh'
                    //         step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
                    //     } catch (err) {
                    //         currentBuild.result = "UNSTABLE"
                    //         throw err
                    //     }
                //    }
                stage "Build elastest-torm"
                echo ("Build elastest-torm")
                sh 'export PATH=$MVN_CMD_DIR:$PATH; cd ./elastest-torm; mvn -Pci package;'


                stage "Create etm docker image"
                    echo ("Creating elastest/etm image..")                
                    sh 'export PATH=$MVN_CMD_DIR:$PATH; cd ./docker/elastest-torm; ./build-image.sh;'

                stage "Publish etm docker image"
                    echo ("Publish elastest/etm image")
                    def myimage = docker.image('elastest/etm:mini_with-master-slave-mode')
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'elastestci-dockerhub',
                        usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                        sh 'docker login -u "$USERNAME" -p "$PASSWORD"'
                        myimage.push()
                    }
                }


                    
            } catch (err) {
                if (currentBuild.result != "UNSTABLE") {
                  currentBuild.result = "FAILURE"
                }
                echo 'Error!!! Send email to the people responsible for the builds.'
                emailext body: 'Please go to  ${BUILD_URL}  and verify the build',
                replyTo: '${BUILD_USER_EMAIL}', 
                subject: 'Job ${JOB_NAME} - ${BUILD_NUMBER} RESULT: ${BUILD_STATUS}', 
                to: '${MAIL_LIST}'

                throw err
            }
    }

    def containerIp(service) {
        containerIp = sh (
            script: "docker inspect --format=\"{{.NetworkSettings.Networks."+env.COMPOSE_PROJECT_NAME+"_elastest.IPAddress}}\" "+env.COMPOSE_PROJECT_NAME+"_"+service+"_1",
            returnStdout: true
        ).trim()
        
        echo service+" IP = " + containerIp;
        return containerIp;
    }