# Spark Distributed Similarity Group-by with VP-Tree (DSG-VPTree)
Java implementation of Distributed Similarity Group-by with VP-Tree for Apache Spark

### Prerequisites

To run the algorithms you will need Java version 8, Spark version 2.3.2, and the latest version of Eclipse.   
Spark 2.3.2 is incompatible with newer versions of Java, i.e. 9 >, so the use of Java 8 is a strict requirement.  
 
### Local Mode Walkthrough
1. Download both the java files and the dimension 200, SF1 through SF5 files.
2. Create a new project in Eclipse and place the java files in the ```src``` foler.
3. Add the Spark jars to your project.
4. Because the algorithm is running in Eclipes (local mode), in the main method replace line 29 with
```java
SparkConf conf = new SparkConf().setAppName("SparkSimGroupBy").setMaster("local[*]");
```
5. As part of your run configuration, copy and paste the following parameter values.    

Parameters:   
```
80
200
200
50000
0
100
path/to/the/input
path/to/the/output
```
To see what different parameters these  values correspond to, refer to lines 39-46.  
 
Since Spark is running locally, simply specify the input path as the location where you choose to store the files on your system. The output path can be whatever you specify given that the directory does not already exist.

6. Hit run in Eclipse.

### Cluster Mode Walkthrough
1. Download both the java files and the dimension 200, SF1 through SF5 files.
2. Create a new project in Eclipse and place the java files in the ```src``` foler.
3. Add the Spark jars to your project.
4. Export the project to a jar and place the jar in your cluster.  
5. As part of the submission, copy and paste the following parameter values.   

Parameters:  Splits dimension numberOfPivots threshold seed epsilon pathToRead pathToWrite 
```
80
200
200
50000
0
100
path/to/the/input
path/to/the/output
```
To see what these different parameters values correspond to refer to lines 39-46.  
 
Since Spark is running in distributed mode, place the data files into your cluster and specficy the location as part of your input values. The output path can be whatever you specify given that the directory does not already exist.

6. Submit the job.

command: spark-submit --class SparkSimGroupBy  --master yarn --deploy-mode client ~/SparkVpTree.jar 80 200 200 50000 0 100 mrsimjoindsg saidaSpark   

### How to generate data sets
1. Download groupingDataGenerator.java java files.
2. Create a new project in Eclipse and place the java files in the ```src``` folder.
3. To Run, simply compile then execute, afterwhich prompts will guide you in 
the creation of data set.
4. The output is a collection of files, one per scaleFactor, with the naming convention of outputFileName_SF_ScaleFactorNumber_DIM_nimDim_EPS_epsilon_PPSF_numberOfPointsPerSF_Min_minPoints_Max_maxPoints

Parameters:  
```
Enter the Scale Factor
10
Number Of Points Per Scale Factor 
200000
Enter the number of Dimensions
64
Enter the minimum points per group
50
Enter the maxium points per group
100
Enter the Epsilon
100
Enter the file name --No extension
data
```   

The files will be generated with the pattern below:

data_SF_1_DIM_64_EPS_100_PPSF_200000_MIN_50_MAX_100_PIVOTS_2698.csv 
data_SF_2_DIM_64_EPS_100_PPSF_200000_MIN_50_MAX_100_PIVOTS_5391.csv 
data_SF_3_DIM_64_EPS_100_PPSF_200000_MIN_50_MAX_100_PIVOTS_8077.csv 
....
data_SF_10_DIM_64_EPS_100_PPSF_200000_MIN_50_MAX_100_PIVOTS_26861.csv 

