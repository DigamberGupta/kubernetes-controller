# Kubernetes controller service

- Kubernetes controller is a spring boot application which implement the custom kubernetes controller by using java extended client from io.kubernetes
- The Kubernetes controller service call the counter service to store the number of deployment
  count [counter-service](https://github.com/DigamberGupta/counter-service)
- This service demonstrate a way to generate client for OpenAPI specification YAML

## Requirements

- JAVA 11
- Gradle 7.1.1
- Docker
- Kubernetes

### Following useful command,

- ```gradlew clean build```  (to build the gradle project from the)
- ```gradlew bootRun``` (to run the application)
- ```gradlew docker``` (to build a docker image)
- ```kubectl apply -f src\main\k8s\deployment.yaml``` (to apply the kubernetes config to run your pods on kubernetes)


