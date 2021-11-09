pipeline
{
    agent any
    tools
    {
        maven 'Team23 - Maven'
        jdk 'Team23 - OpenJDK'
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
                            configName: "MacMini",
                            verbose: true,
                            transfers: [
                                sshTransfer(execCommand: 'rm -rf /home/team23/target; rm -rf /home/team23/Dockerfile; rm -rf /home/team23/config.properties'),
                                sshTransfer(execCommand: 'if docker ps -a | grep jukebox; then docker rm $(docker stop $(docker ps -a -q --filter name="jukebox" --format="{{.ID}}")); fi'),
                                sshTransfer(execCommand: 'if docker image list | grep jukebox; then docker image rm jukebox:latest; fi'),
                                sshTransfer(sourceFiles: 'target/Jukebox-1.0.jar'),
                                sshTransfer(sourceFiles: 'config.properties'),
                                sshTransfer(sourceFiles: 'Dockerfile'),
                                sshTransfer(execCommand: 'docker build -t jukebox .'),
                                sshTransfer(execCommand: 'docker run -d --network host --restart always --name jukebox jukebox:latest')
                            ]
                        )
                    ]
                )
            }
        }
    }
}
