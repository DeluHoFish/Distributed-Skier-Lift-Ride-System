# A Brief Description of Client Design

## Design Structure

My client design for cs6650 assignment 1 can be generally divided into four parts based on the requirement, which are <u>**partone package**</u>, <u>**parttwo package**</u>, and <u>**client.MultithreadedClient class**</u>.

### Package partone
This part is built upon the requirement of part 1. It includes one package called evengeneration and two classes, SendRequest and server.SkiersServlet.

**eventgeneration package:**
Inside eventgeneration package, there are two classes, SkierLiftRideEvent and SkierLiftRideEventGenerator. When class SkierLiftRideEvent is called, it generates a random skier lift ride event can be used to form a POST request.
Class SkierLiftRideGenerator creates new SkierLiftRideEvent objects based on the given number and stores them in a list.

**Class SendRequest:** 
This class implements Runnable and is used for sending POST requests. It takes the input "events" and forms POST requests. 
You can change the number of events you want to post inside each run() function by changing the input. 
In the meantime, it counts the numbers of successful and unsuccessful requests. In run() method, it also handles errors of unsuccessful requests. 
It records the information of each request in a list called latencyRecord.

### Package parttwo
This part is built upon the requirement of part 2. It includes two classes, CreateCSVRecord and CalculateData.

**Class CreateCSVRecord:**
This class is used for creating the CSV file and adding the data into the file. The file created last time will be overwritten each time re-running the program.

**Class CalculateData:**
This class is used for fetching the data in the CSV file and outputting mean response time, median response time, throughput, p99 response time, and min and max response time.

### Class MultithreadedClient
This class calls all the classes mentioned above, creates multiple threads to send the POST threads, and outputs the data.

In the class start() function, it creates a MultithreadedClient object first so the variables and functions in the class can be used later. 
Then it creates a SkierLiftRideEventGenerator object to create 200k random events. Two CountdownLatch objects are created later.
The first one is for the startup 32 threads. The second is for the threads needed to post the rest of the requests.
Before the threads, a CSV file is created first. I use two for loops to create and initiate threads. 
Inside the loop, a SendRequest object is created first to set the number of requests each thread should post.
Inside each thread, after the requests are sent, the numbers of successful and unsuccessful requests are counted.
Then the information of each request is recorded in the CSV file. Each time the main tasks of each thread are finished, latch.countdown() is called.
Before threads start, a timestamp is created to record the start time. After all the phases complete, another timestamp is created to record the end time.
Then by using all the information collected, we can display the data in the output window. A CalculateData object is created to show some more detailed information about the requests.

## Little's Law Throughput Prediction
N = Throughput * Response time

Throughput = N / Response time

In my client, I used 200 threads in total. It is the same as the server threads, so N is 200.
The throughput prediction is 200 / Response time. 
<img src="screenshots/client_one_and_two.jpeg">
So in this screenshot, we can get the prediction should be 200/0.00451789 ≈ 44268.45, which is close to 42744.


## *Filepath

### The Filepath of Screenshots and Plot
It's in the "screenshots" directory.


### The Filepath of war file
src/main/java/server/upic-ski-resorts-1.0-SNAPSHOT.war
