pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        WORKSPACE = "${env.WORKSPACE}"
        // List of services without test folders
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
        // GitHub configuration
        GITHUB_APP_CREDENTIALS_ID = credentials('GITHUB_APP_CREDENTIALS_ID')
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // print branch name
                    echo "Running pipeline for Branch : ${env.BRANCH_NAME}"

                    // Get changed files between current and previous commit
                    def changedFiles = ""
                    if (env.CHANGE_ID) {
                        // This is a PR, get PR changes
                        changedFiles = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET} HEAD", returnStdout: true).trim()
                    } else {
                        // Normal branch build
                        changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                    }
                    
                    // Enhanced debug output
                    echo "All changed files:"
                    echo "${changedFiles}"
                    
                    // Define service directories to monitor
                    def services = [
                        'spring-petclinic-admin-server',
                        'spring-petclinic-api-gateway',
                        'spring-petclinic-config-server',
                        'spring-petclinic-customers-service',
                        'spring-petclinic-discovery-server',
                        'spring-petclinic-genai-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]
                    
                    // Identify which services have changes
                    env.CHANGED_SERVICES = ""
                    for (service in services) {
                        if (changedFiles.contains(service)) {
                            env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                        }
                    }

                    echo "Detected changes in services: ${env.CHANGED_SERVICES}"
                    
                    // If no specific service changes detected, check for common changes
                    if (env.CHANGED_SERVICES == "") {
                        if (changedFiles.contains("pom.xml") || 
                            changedFiles.contains(".github") || 
                            changedFiles.contains("docker-compose") ||
                            changedFiles.contains("Jenkinsfile")) {
                            echo "Common files changed, will build all services"
                            env.CHANGED_SERVICES = services.join(" ")
                        } else {
                            echo "No relevant changes detected"
                        }
                    }
                    
                    echo "Services to build: ${env.CHANGED_SERVICES}"
                }
            }
        }
        
        stage('Run Tests') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    echo "Services to be tested: ${serviceList}"
                    
                    // Create a GitHub check for the test stage
                    checkout([$class: 'GitSCM', branches: [[name: '${GIT_COMMIT}']], extensions: [[$class: 'CloneOption', depth: 0, noTags: false, reference: '', shallow: false]]])
                    
                    def checkRunName = "Run Tests"
                    def checkRun = githubChecks(
                        name: checkRunName,
                        status: 'in_progress',
                        summary: "Running tests for services: ${env.CHANGED_SERVICES}",
                        title: "Testing Services",
                        repository: "${env.GIT_URL.tokenize('/')[3].split('\\.')[0]}/${env.GIT_URL.tokenize('/')[4].split('\\.')[0]}"
                    )
                    
                    try {
                        def testFailures = []
                        
                        for (service in serviceList) {
                            echo "Testing service: ${service}"
                            if (!env.SERVICES_WITHOUT_TESTS.contains(service)) {
                                dir(service) {
                                    try {
                                        // Run tests with JaCoCo coverage
                                        sh 'mvn clean verify'
                                        
                                        // Publish test results
                                        junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                    } catch (Exception e) {
                                        echo "Warning: Tests failed for ${service}: ${e.message}"
                                        testFailures.add(service)
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                }
                            } else {
                                echo "Skipping tests for ${service} as it is in the SERVICES_WITHOUT_TESTS list"
                            }
                        }
                        
                        if (testFailures.size() > 0) {
                            checkRun.conclusion = 'failure'
                            checkRun.summaryText = "Tests failed for services: ${testFailures.join(', ')}"
                        } else {
                            checkRun.conclusion = 'success'
                            checkRun.summaryText = "All tests completed successfully"
                        }
                    } catch (Exception e) {
                        checkRun.conclusion = 'failure'
                        checkRun.summaryText = "Tests failed: ${e.message}"
                        throw e
                    } finally {
                        checkRun.status = 'completed'
                        checkRun.text = "Detailed test results for the following services: ${env.CHANGED_SERVICES}"
                        checkRun.publish()
                        
                        // Update PR status
                        if (env.CHANGE_ID) {
                            if (currentBuild.result == 'UNSTABLE' || currentBuild.result == 'FAILURE') {
                                githubPRStatusPublisher(
                                    statusVerifier: {
                                        return 'FAILURE'
                                    },
                                    unstableAs: 'FAILURE'
                                )
                            } else {
                                githubPRStatusPublisher(
                                    statusVerifier: {
                                        return 'SUCCESS'
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    
                    // Create a GitHub check for the build stage
                    def checkRunName = "Build Services"
                    def checkRun = githubChecks(
                        name: checkRunName,
                        status: 'in_progress',
                        summary: "Building services: ${env.CHANGED_SERVICES}",
                        title: "Building Services",
                        repository: "${env.GIT_URL.tokenize('/')[3].split('\\.')[0]}/${env.GIT_URL.tokenize('/')[4].split('\\.')[0]}"
                    )
                    
                    try {
                        def buildFailures = []
                        
                        for (service in serviceList) {
                            echo "Building service: ${service}"
                            dir(service) {
                                try {
                                    // We can skip tests here since we already ran them
                                    sh 'mvn package -DskipTests'
                                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                                } catch (Exception e) {
                                    echo "Error building ${service}: ${e.message}"
                                    buildFailures.add(service)
                                    currentBuild.result = 'FAILURE'
                                }
                            }
                        }
                        
                        if (buildFailures.size() > 0) {
                            checkRun.conclusion = 'failure'
                            checkRun.summaryText = "Build failed for services: ${buildFailures.join(', ')}"
                        } else {
                            checkRun.conclusion = 'success'
                            checkRun.summaryText = "All services built successfully"
                        }
                    } catch (Exception e) {
                        checkRun.conclusion = 'failure'
                        checkRun.summaryText = "Build failed: ${e.message}"
                        throw e
                    } finally {
                        checkRun.status = 'completed'
                        checkRun.text = "Build results for services: ${env.CHANGED_SERVICES}"
                        checkRun.publish()
                        
                        // Update PR status
                        if (env.CHANGE_ID) {
                            if (currentBuild.result == 'FAILURE') {
                                githubPRStatusPublisher(
                                    statusVerifier: {
                                        return 'FAILURE'
                                    }
                                )
                            } else {
                                githubPRStatusPublisher(
                                    statusVerifier: {
                                        return 'SUCCESS'
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        stage('Publish Coverage Reports') {
            steps {
                script {
                    // Define all services to test (excluding those without tests)
                    def services = [
                        'spring-petclinic-api-gateway',
                        'spring-petclinic-config-server',
                        'spring-petclinic-customers-service',
                        'spring-petclinic-discovery-server',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]
                    
                    echo "Publishing coverage reports for services..."
                    
                    for (service in services) {
                        dir(service) {
                            echo "Publishing coverage for ${service}"
                            
                            // Process JaCoCo reports
                            jacoco(
                                execPattern: 'target/jacoco.exec',
                                classPattern: 'target/classes',
                                sourcePattern: 'src/main/java',
                                exclusionPattern: 'src/test*,**/*DTO*,**/*Config*',
                                changeBuildStatus: true,
                                minimumInstructionCoverage: '30',
                                maximumInstructionCoverage: '100'
                            )
                            
                            // Publish HTML report
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/site/jacoco',
                                reportFiles: 'index.html',
                                reportName: "${service} - JaCoCo Coverage Report"
                            ])
                        }
                    }
                }
            }
        }
        
        stage('Verify Changes') {
            steps {
                script {
                    echo "Verification Report:"
                    echo "============================"
                    echo "Changed Services: ${env.CHANGED_SERVICES}"
                    
                    if (env.CHANGED_SERVICES.contains("spring-petclinic-visits-service")) {
                        echo "✓ Visits Service changes detected correctly"
                    }
                    
                    if (env.CHANGED_SERVICES.contains("spring-petclinic-api-gateway")) {
                        echo "✓ API Gateway changes detected correctly"
                    }
                    
                    // Add checks for other services if needed
                }
            }
        }
    }
    
    post {
        success {
            script {
                if (env.CHANGE_ID) {
                    // This is a PR, update status
                    githubPRStatusPublisher(
                        statusVerifier: {
                            return 'SUCCESS'
                        }
                    )
                }
                
                echo "Build completed successfully!"
            }
        }
        failure {
            script {
                if (env.CHANGE_ID) {
                    // This is a PR, update status
                    githubPRStatusPublisher(
                        statusVerifier: {
                            return 'FAILURE'
                        }
                    )
                }
                
                echo "Build failed!"
            }
        }
        always {
            cleanWs()
        }
    }
}