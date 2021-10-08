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
            sh 'echo "hello world"'
        }
    }
}