pipeline
{
    agent any
    tools
    {
        maven 'Team23 - Maven'
        jdk 'Team23 - OpenJDK'
    }
    options
    {
        disableConcurrentBuilds()
    }
    stages
    {
        stage('Clean')
        {
            when
            {
                expression
                {
                    return fileExists ('config.properties')
                }   
            }
            
            steps
            {
                sh 'rm $WORKSPACE/config.properties'
            }
        }
        stage('Build')
        {
            environment
            {
                SECRET_FILE_ID = credentials('production-config-keys')
            }
            steps
            {
                echo 'Building..'
                echo 'Insert maven commands here..'
                sh 'cp \$SECRET_FILE_ID $WORKSPACE'
                sh 'mvn clean install'
            }
        }

        stage('Deploy')
        {
            when
            {
                expression
                {
                    return env.BRANCH_NAME == 'master'
                }
            }
            steps([$class: 'BapSshPromotionPublisherPlugin'])
            {
                sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                        sshPublisherDesc(
                            configName: "Oracle",
                            verbose: true,
                            transfers: [
                                sshTransfer(execCommand: 'rm -rf /home/angelolz/jukebox/target; rm -rf /home/angelolz/jukebox/Dockerfile; rm -rf /home/angelolz/jukebox/config.properties'),
                                sshTransfer(execCommand: 'if docker ps -a | grep jukebox; then docker rm $(docker stop $(docker ps -a -q --filter name="jukebox" --format="{{.ID}}")); fi'),
                                sshTransfer(execCommand: 'if docker image list | grep jukebox; then docker image rm jukebox:latest; fi'),
                                sshTransfer(sourceFiles: 'target/Jukebox-1.0.1.jar'),
                                sshTransfer(sourceFiles: 'config.properties'),
                                sshTransfer(sourceFiles: 'Dockerfile'),
                                sshTransfer(execCommand: 'docker build -t jukebox ./jukebox'),
                                sshTransfer(execCommand: 'docker run -d --network host --restart always --name jukebox jukebox:latest')
                            ]
                        )
                    ]
                )
            }
        }
    }
}
