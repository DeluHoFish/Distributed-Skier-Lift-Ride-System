### How To Run the Client

If you want to run the client with the default setting, you can just directly open class MultithreadedClient, slide to the bottom to find the main function, and run the program.

**URL setting:** URL can be set in class SendRequest, by change the parameter of apiClient.setBasePath()(line 28).

**Threads configuration:** The number of threads and the requests each thread processes can be reset in class MultithreadedClient by changing the parameter of latch and the bound of for loop in this class. Calculate the numbers carefully to avoid any errors.

**Requests number configuration:** Class SkierLiftRideEventGenerator is called in class MultithreadedClient. You can reset the number of total requests by changing the parameter where the generator is created(line 30 in class MultithreadedClient).

**CSV filepath:** The default filepath is "data/request_record.csv". You can change it in class CreateCSVRecord.