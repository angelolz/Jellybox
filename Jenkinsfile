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
                            configName: "HomeServer",
                            verbose: true,
                            transfers: [
                                sshTransfer(execCommand: 'cd Desktop/Jukebox; if (Test-Path -Path target) {rm -R ./target -ErrorAction SilentlyContinue}'),
                                sshTransfer(execCommand: 'cd Desktop/Jukebox; if (Test-Path Dockerfile) {rm Dockerfile -Force -Confirm:$false -ErrorAction SilentlyContinue}'),
                                sshTransfer(execCommand: 'cd Desktop/Jukebox; if (Test-Path config.properties) {rm config.properties -Force -Confirm:$false -ErrorAction SilentlyContinue}'),
                                sshTransfer(execCommand: 'if(docker ps --filter status=running | findstr "jukebox") {docker stop jukebox; docker rm jukebox}'),
                                sshTransfer(execCommand: 'if(docker image list | findstr "jukebox") {docker image rm jukebox:latest}'),
                                sshTransfer(sourceFiles: 'target/Jukebox-1.0.1.jar'),
                                sshTransfer(sourceFiles: 'config.properties'),
                                sshTransfer(sourceFiles: 'Dockerfile'),
                                sshTransfer(execCommand: 'cd Desktop/Jukebox; docker build -t jukebox .'),
                                sshTransfer(execCommand: 'cd Desktop/Jukebox; docker run -d --network host --restart always --name jukebox jukebox:latest')
                            ]
                        ),
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
                                sshTransfer(execCommand: 'docker build -t jukebox ./Desktop/jukebox'),
                                sshTransfer(execCommand: 'docker run -d --network host --restart always --name jukebox jukebox:latest')
                            ]
                        )
                    ]
                )
            }
        }
    }
}
