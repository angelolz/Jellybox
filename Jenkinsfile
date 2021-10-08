pipeline
{
    agent any

    stages
    {
        stage('Build')
        {
            environment
            {
                SECRET_FILE_ID = credentials('production-config-keys')
            }
            steps
            {
                sh 'curl \"https://api.github.com/repos/BoiseState/CS471-F21-Team23/statuses/$GIT_COMMIT\" -H \"Content-Type: application/json\" -u \"shadowbladerx1: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\" -X POST -d "{\\"state\\": \\"pending\\",\\"context\\": \\"continuous-integration/jenkins\\", \\"description\\": \\"Jenkins\\", \\"target_url\\": \\"https://jenkins.testground.dev/job/Team23-Jenkins-Builder-Test/$BUILD_NUMBER/console\\" }\"'
                echo 'Building..'
                echo 'Insert maven commands here..'
                sh 'rm $WORKSPACE/\$SECRET_FILE_ID '
                sh 'cp \$SECRET_FILE_ID $WORKSPACE'
                sh 'mvn clean install'
            }
        }

        stage('Test')
        {
            steps
            {
                echo 'Testing..'
            }
        }

        stage('Deploy')
        {
            steps
            {
                echo 'Deploying..'
            }
        }
    }

    post
    {
        success
        {
            sh 'curl \"https://api.github.com/repos/BoiseState/CS471-F21-Team23/statuses/$GIT_COMMIT\" -H \"Content-Type: application/json\" -u \"shadowbladerx1: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\" -X POST -d "{\\"state\\": \\"success\\",\\"context\\": \\"continuous-integration/jenkins\\", \\"description\\": \\"Jenkins\\", \\"target_url\\": \\"https://jenkins.testground.dev/job/Team23-Jenkins-Builder-Test/$BUILD_NUMBER/console\\" }\"'
        }
        failure
        {
            sh 'curl \"https://api.github.com/repos/BoiseState/CS471-F21-Team23/statuses/$GIT_COMMIT\" -H \"Content-Type: application/json\" -u \"shadowbladerx1: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\" -X POST -d "{\\"state\\": \\"failure\\",\\"context\\": \\"continuous-integration/jenkins\\", \\"description\\": \\"Jenkins\\", \\"target_url\\": \\"https://jenkins.testground.dev/job/Team23-Jenkins-Builder-Test/$BUILD_NUMBER/console\\" }\"'
        }
    }
}