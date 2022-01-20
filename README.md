# cinema-app

# How to run

```
./gradlew build
./gradlew jibDockerBuild -Djib.to.image=cinema:latest
cd docker
OMDB_APIKEY=xxxx docker-compose up
```

# API docs
http://localhost:8080/docs/index.html

# Things I focused on
- delivering concise solution that fulfills requirements
- separating domains (movies & ratings)
- using separate models objects for the different layers
- docker setup, github actions
- integration tests and api docs using spring rest docs (I use tests to generate docs)
- choosing right database (went for mongoDB, I decided not to have many relations but instead keep whole movie object in one document)
- how to store ratings both in user and movie domain
- I used spring webflux, but it would be ok to take regular servlet approach

# Things to improve
- cover more corner cases
- refactor tests a little
- use resilience4j for cache and retries