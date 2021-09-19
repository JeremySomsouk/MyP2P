# MyP2P - Let's start streaming !
## Introduction

I used [Spring Initializr](https://start.spring.io/) to start my project.
It is a Java 11 project, using docker to spawn a PostgreSQL and Redis database instances.

Since this is a project that needs both instances to run, I made a dockerfile to 'dockerize' this project to launch the 3 components altogether.

Using gradle, you can build the project with the following command :
> $ ./gradlew build 
 
And then we can launch all three components, at the root of this project :
> $ cp build/libs/myp2p-1.0.0.jar .
> 
> $ docker-compose up --build

After start up, see the [OpenApi documentations](http://localhost:8080/swagger-ui.html) for more information about the endpoint.

## Project design

![Alt text](assets/myp2pDesign.png)

The solution revolves around using a cache, here Redis, to accumulates data, in order to save it once when the time window is up. 

I followed this logic : 
- [1] Data are and received through the '/stats' endpoints 
- [2] The application is accumulating all stats to the Redis cache in order to be later summed when the time window is up for a specific video session.
- [3] Each time a new video session is received on our side, we start timer. When the time is up for this video, we sum all received data in this time window looking at the Redis cache.
- [4] While getting the data from the Redis cache, we delete previous recording of statistics. We'll then start a new time window when we receive a new update of this video session and start over.

## Development details

### Step 1

### Step 2

### Step 3


### Misc
- Using TestContainers for the tests is not optimal since it is required to have docker on the local machine that runs the tests. But it simplifies a bit the configuration since we don't need embedded databases for both Redis and Postgres ; 
- I try not to comment my code that much, but instead having meaningful method names ;
- I used lombok for convenience, JUnit 5 for testing ;

Thank you if you went that far on the review, I will gladly take any comment on my code to improve myself.