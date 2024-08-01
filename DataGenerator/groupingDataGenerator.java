import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.io.*; 

/**
 *
 * @author http://www.public.asu.edu/~ynsilva/SimCloud
 */

/*
The groupingDataGenerator file outputs data files for clustering analysis. 
To Run, simply compile then execute, afterwhich prompts will guide you in 
the creation of data set. The output is a collection of files, one per scaleFactor, 
with the naming convention of 
outputFileName_SF_ScaleFactorNumber_DIM_nimDim_EPS_epsilon_PPSF_numberOfPointsPerSF_Min_minPoints_Max_maxPoints

The rows of each record are randomized data for each nth dimension, whereas 
each column is a diffrent dimension. 
The first two values of a record are the recordID Number and the group they belong to.

*/
public class groupingDataGenerator {
	public static void main(String[] args) throws Exception {
		//Prompts to generate sets
		int scaleFactor, numberOfClustersPerSF;
		int numberOfPointsPerSF, epsilon, numDim,shift;
		Random rand = new Random();

		Scanner keyboard = new Scanner(System.in);
                System.out.println("Enter the Scale Factor");
		scaleFactor =  keyboard.nextInt();
                System.out.println("Number Of Points Per Scale Factor ");
		numberOfPointsPerSF =  keyboard.nextInt();
                System.out.println("Enter the number of Dimensions");
                numDim=keyboard.nextInt(); 
		System.out.println("Enter the minimum points per group");
		int minPoints = keyboard.nextInt();
		System.out.println("Enter the maxium points per group");
		int maxPoints = keyboard.nextInt();                
		System.out.println("Enter the Epsilon");
		epsilon =  keyboard.nextInt() ;
                System.out.println("Enter the file name --No extension");
		String outputFileName = keyboard.next();

		
		
		//Shift is the space between two clusters of data 
		//This spacing allows for groups to identified more easliy
		shift = epsilon * 2;
		int numberGroupsInSF;
		//This prevents points from being genrated outside of the nth  dimensional 
		//Space specified. 
		double sideLength = epsilon / (Math.sqrt(numDim));
		int currentGroupInDataset = 0;
		long recordID = 0;
		//Scale Factor Loop
		for (int currentSF = 1; currentSF < scaleFactor+1; currentSF++){
			String scaleFactorFileName = outputFileName.concat("_SF_"+currentSF+"_DIM_"+numDim+"_EPS_"+epsilon+
			"_PPSF_"+numberOfPointsPerSF+"_MIN_"+minPoints+"_MAX_"+maxPoints); 
            File file = new File(scaleFactorFileName); 
			PrintWriter fileWriter = new PrintWriter(file);
            int currentPointsInSF = 0;
			numberGroupsInSF = 0;
			//Grouping Loop 
			while (true) {
				int pointsPerGroup = rand.nextInt(maxPoints-minPoints) + minPoints;
				if (pointsPerGroup + currentPointsInSF > numberOfPointsPerSF)
					pointsPerGroup = numberOfPointsPerSF - currentPointsInSF;
				int currentPointsInGroup = 0;
				if (pointsPerGroup == 0)
					break;
				//Record Generation
				while(currentPointsInGroup<pointsPerGroup) {
						int numberOfDup = rand.nextInt(3) + 1;
						numberOfDup = Math.min(numberOfDup, pointsPerGroup - currentPointsInGroup );
						double xCoordinate = rand.nextDouble() * sideLength;
						xCoordinate += shift * numberGroupsInSF;
						double yCoordinate = rand.nextDouble() * sideLength;
						yCoordinate += shift * currentSF;
						//for each dimension
                        double[] zCoordinates = new double[0]; 
                        if (numDim>2){
						zCoordinates=new double[numDim-2]; 
                            for (int d =0; d <numDim-2; d++)
                            {
                            zCoordinates[d] = rand.nextDouble() * sideLength;
                            }
                        }
                        String zOutPut = Arrays.toString(zCoordinates).replace("[", "");
                        zOutPut=zOutPut.replace("]", ""); 
						zOutPut=zOutPut.replace(" ","");
						//duplication to create strong groups
                        for (int d = 0; d < numberOfDup; d++) {
							fileWriter.write(recordID+1 + "," + currentGroupInDataset + "," + xCoordinate + "," + yCoordinate +","+ zOutPut+"\n");
							currentPointsInSF++;
							recordID++;
							currentPointsInGroup++;
						}
				}
				numberGroupsInSF++;
				currentGroupInDataset++;
			}
			fileWriter.close();
		    File file2 = new File(scaleFactorFileName+"_PIVOTS_"+(currentGroupInDataset-1)+".csv");
		    file.renameTo(file2);
		    System.out.println(file2.getName()+" complete");
        }
  		   
	System.out.println("Complete");
    }
}
