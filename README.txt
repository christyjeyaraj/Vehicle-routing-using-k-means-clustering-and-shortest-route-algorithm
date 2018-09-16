README File

1. ASSUMPTIONS:
- Data is in the correct format as given in the input file with 4 columns
- There are at least 2 or more kiosks.
- Both the drivers do one-trip per day compulsorily.
- Vehicle capacity is infinity.
- This implementation is for solving the shortest route. So, traffic,time and capacity constraints are not taken into account. 


2. ALGORITHM DETAILS:
- The algorithm I have used is "Cluster and Route".

A. CLUSTERING TECHNIQUE:
- I have "K-Means" Clustering technique to cluster the kiosk locations based on the geographical co-ordinates and each cluster is 
  assigned to a driver.
  
B. ROUTING TECHNIQUE:
- I have written the code for the "Shortest Path for Traveling Salesman Problem" algorithm for finding the shortest routes in each cluster.
- The algorithm will find all the possible routes in a cluster.
- Out of all the possible routes, the route with the shortest distance will be chosen as the route for the driver.


3. IMPLEMENTATION DETAILS:
- Language Used: JAVA
- Tools Used: NetBeans IDE
- Package Used: JAVAML (for K-Means Clustering)
