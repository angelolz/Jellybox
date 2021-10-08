pipeline
{
    agent any

    stages
    {
        stage('Build')
        {
            steps
            {
                echo 'Building..'
                echo 'Insert maven commands here..'
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
            sh '''
                    curl \"https://api.github.com/repos/BoiseState/CS471-F21-Team23/statuses/$GIT_COMMIT\" \
                    -H \"Content-Type: application/json\" \
                    -u \"shadowbladerx1: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\" \
                    -X POST \
                    -d \"{\"state\": \"success\",\"context\": \"continuous-integration/jenkins\", \"description\": \"Jenkins\", \"target_url\": \"https://jenkins.testground.dev/job/Team23-Jenkins-Builder-Test/$BUILD_NUMBER/console\" }"
               '''
        }
    }
}