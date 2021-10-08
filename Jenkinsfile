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
                    curl "https://api.github.com/repos/octocat/hello-world/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d/status" \
                    -H "Content-Type: application/json" "Authorization: token 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" \
                    -X POST \
                    -d "{\"state\": \"success\",\"context\": \"continuous-integration/jenkins\", \"description\": \"Jenkins\", \"target_url\": \"https://jenkins.testground.dev/job/Team23-Jenkins-Builder-Test/$BUILD_NUMBER/console\" }"
               '''
        }
    }
}