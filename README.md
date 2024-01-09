# Modern-DevOps-Practices-Project

This is a project made by me for Modern DevOps Practices course in
Faculty Of Matematics and Informatics at Sofia University St. Kliment Ohridski.

In this project i use some of the best practices widely spread nowadays,
that we have learned during the semester, to build a complete automated
software delivery process using pipelines.

## Application - Cryptocurrency Wallet Manager 💰💶

- This is a `client-server` application,
  which simulates a virtual cryptocurrency wallet.
- The server must respond to multiple requests,
  from many clients, at the same time.
- Information about the cryptocurrencies,
  which is necessary for the server, is available
  from a public `REST API` - [CoinAPI](https://www.coinapi.io/)
- To use the `REST API` you will have to take your personal
  `API key` for authentication -
  [APIkey](https://www.coinapi.io/pricing?apikey) 🔑
- After getting the information from the API,
  it is cached for the next 30 minutes, so within this time
  no requests to API are required. If the current information
  is older than 30 minutes, it is considered invalid and is updated.  
- The server saves user's and crypto information into `JSON` format
  on the file system, so every time after rebooting, the information is loaded.
- User's sensitive information, such as password,
  is hashed for security reasons.

### Supported commands

- help - Show instructions.
- register ` <username> <password> ` - Create an account.
- login ` <username> <password> ` - Log into your profile.
- list_offerings - Shows information about the currently
  available cryptocurrencies.
- buy <offering_code> <amount_money> - Buy cryptocurrency,
  where <offering_code> is the code of wanted cryptocurrency
  and <amount_money> are invested money.

- sell <offering_code> - Sell cryptocurrency, where
  <offering_code> is the code of the crypto you want to sell.
- get_wallet_summary - Gives you information about your profile
  (such as available money and currently active investments).
- get_wallet_overall_summary - Provides the full information
  about the profit/loss of your investments. The application
  compares the price for each cryptocurrency from the time of
  purchase and its current price to get complete information.

- disconnect - Save current session and exit.

## Branching Strategy

I decided to create a feature branch in which, i develop the whole
automated software delivery process. When different stages of the
solution are ready, and the required tests from the workflow
passes successfully, i create a pull request and merge the following
changes into the main branch.

I chose this branching strategy, because
it is really good for small projects with 1-2 people working on it.
Before merging into the main branch, feature branches
undergo code review to maintain code quality.

## About the CI/CD process

![Workflow](https://github.com/nnniki/Modern-DevOps-Practices-Project/assets/94651604/75875952-b4e9-4ffb-8ddd-e5c06d615b05)

### CheckCodeFormat Job:

- Checks out code.
- Uses checkstyle action.
- Checks if the code meets our custom code style [requirments](https://github.com/nnniki/Modern-DevOps-Practices-Project/blob/feature/app/checkstyle.xml)

### EditorConfigCheck Job:

- Checks out code.
- Installs editorconfig-checker.
- Checks if the project files meets the defined [rules](https://github.com/nnniki/Modern-DevOps-Practices-Project/blob/feature/.editorconfig)

### MarkdownFilesCheck Job:

- Checks out code.
- Installs markdown-lint.
- Checks all .md files if following certain rules.

### HardcodedSecretsCheck Job:

- Checks out code.
- Uses gitleaks action.
- Checks code for security vulnerabilities, such as
  hardcoded API keys, passwords and other sensitive information.

### BuildAppWithGradle Job:

- Depends on the CheckCodeFormat, EditorConfigCheck,
  MarkdownFilesCheck, HardcodedSecretsCheck jobs.
- Checks out code.
- Set up JDK 17 and Gradle.
- Build the application.
- Upload the built application as an artifact.

### SAST Job:

- Depends on BuildAppWithGradle job.
- Download the artifact, uploaded in previous job.
- Uses sonarcloud action to scan the code for
  potential problems and vulnerabilities.

### SnykTest Job:

- Depends on BuildAppWithGradle job.
- Download the artifact.
- Install Snyk.
- Checks project's dependencies for vulnerabilities.

### UnitTests Job:

- Depends on BuildAppWithGradle job.
- Uses container with pre-installed JDK17
  and Gradle.
- Runs the unit tests.

### TrivyScanServerImage Job:

- Depends on SAST, SnykTest, UnitTests jobs.
- Download the artifact.
- Build the docker image from ServerDockerfile.
- Scan the server image for vulnerabilities.

### TrivyScanClientImage Job:

- Depends on SAST, SnykTest, UnitTests jobs.
- Download the artifact.
- Build the docker image from ServerClientfile.
- Scan the client image for vulnerabilities.

###  BuildAndPushImages Job:

- Depends on TrivyScanServerImage, TrivyScanClientImage jobs.
- Download the artifact.
- Login to DockerHub.
- Build server and client images.
- Tag server and client images with sha-sum.
- Push both images into the docker repository.

## Docker

### Dockerfiles

- Create separate dockerfiles for server and client.
- Use multi stage builds to reduce the size of the final images.
- Build the application, without running the unit tests again,
  to be more efficient.
- Expose server port.
- Run the executable fat jar files.

### Docker compose

- First run server container from the built image.
- Set needed environment variables for the server.
- Client container depends on the Server.
- Run client container from the built image.
- Set needed environment variables for the client.

#### Commands used:

`docker compose -f DockerCompose.yaml up -d Server` -
Starts Server container in detached mode.

`docker compose -f DockerCompose.yaml run Client` -
Starts Client container.

## Kubernets

Manifests are available [here](https://github.com/nnniki/Modern-DevOps-Practices-Project/tree/feature/kubernets)

### Deployments

- Create server and client deployments.
- Label the deployments.
- Set the number of replicas for each deployment.
- Label the containers.
- Set container name and image.
- Expose container port for the server.
- Set needed environment variables -
  `API key is taken from a kubernets secret for security reasons.`

### Secret

- Create a secret for the API key.
- API key mustn't be in plain text for security reasons. 

### Service

- Create a service of type ClusterIP.
- Service expose port.
- Clients send requests to the service which
  redirects them to the server containers.
- Service acts like loadbalancer.
  
#### Commands used:

`minikube start` - Starts a kubernets cluster.

`kubectl apply -f kubernets/secrets.yaml` - Creates a secret.

`kubectl apply -f kubernets/service.yaml` - Creates a service.

`kubectl apply -f kubernets/deployments.yaml` - Creates deployments.






