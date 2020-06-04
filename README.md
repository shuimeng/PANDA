# PANDA
The source codes of PANDA algorithm in the paper: Xie, M., Bhowmick, S.S., Cong, G. et al. PANDA: toward partial topology-based search on large networks in a single machine. The VLDB Journal 26, 203–228 (2017). https://doi.org/10.1007/s00778-016-0447-0

# How to use the codes
"Panda" is the source codes.
“example” is an example of the input files.

1. Import the source code to Eclipse. 
2. Confirm the JRE on or above 1.7 version
3. Build and Run as JAVA Application (OR by command line).

To build JAR file to Run on Linux workstation:
1. Right click on Start.java 
2. Select export menu command.
3. Select Runnable JAR File and click on next command.
4. select Extract required libraries into generated JAR.
5. Generated and you can get jar runnable file.

To Run the software:
1. [important]configure the directory in the “config.properities”:some important configurations are listed here:
(1) “inputDirectory” is the data input directory,
(2) “outputDirectory” is the result output directory, 
(3) “dataSetName” is the graph file.
(4) “labelFileName” is the graph’s label file.
(5) “queryFileName” is the query graph file. The system supports to run many queries at the same time, where you can add the index following the query file name.

2. Open terminal to run java -jar XX/Panda.jar. Pls make sure the “config.properities” is located in the same directory as that of Panda.jar.

3. You will see some abstract information in the output of terminal.

4. Open "outputDirectory" folder and get the detailed information of matches in output files: one is for the running time and other measurements; the other is about the matched graph information including the merged subgraph.

 
In the main function of Start.java, you can change the number of results k and desired algorithm.
