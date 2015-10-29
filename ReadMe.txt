GATK/Queue plugin

The GATK's pipelining system (Queue - https://www.broadinstitute.org/gatk/guide/topic?name=queue) uses QScripts for describing the flow of data through the pipeline and processing done on it. This application is used for converting a QScript into an abstract directed acyclic graph (adag). From the ADAG that contains the input, output and bash command for each node, the functionality is automatically extracted into a shell script with the same functionality as the original qscript. The shell script has been obtained through a topological sort of the ADAG, hence by transforming the graph into a set of steps, to be executed serially.

The ADAG that results follows practically the internal representation that Queue uses for the graph of jobs: nodes represent a state described by the bunch of files, edges represent transitions between the states - the next state is obtained by running a command.

The bash commands in the shell scripts represent the components of the pipeline that was originally described in the qscript. Basically the components of the pipeline are to be run sequentially, by invoking GATK jar.



Usage:
The ShellPlugin is to be run with the same parameters as when running Queue itself - for more details see GATK/Queue official documentation. A basic run would take the following mandatory parameters:

java -jar ShellPlugin.jar -S <path_to_scala_QScript> -R <path_to_reference_file> -I <path_to_bam_file> -tempDir <path_to_temp_directory> -run

Note: it was implemented and ran using Java 1.6.

Input:
- reference file
- bam file
- QScript

Output:
- adag.xml - xml file containing the graph
- shellScript.sh - shell script that the QScript has been transcripted into

Example of run:
java -jar ShellPlugin.jar -S /path/ExampleUnifiedGenotyper.scala -R /path/exampleFASTA.fasta -I /path/exampleBAM.bam -tempDir /path/tmp -run


Implementation details:
The GATK/Queue has been used as a library for the implementation of this application that takes a QScript and converts it into a runnable shell script. The plugin's functionality has been obtained by extending the classes of interest from Queue (inheritance).

Observations: 

The program should be run at least with the parameters mentioned above. A complete list of parameters can be obtained with:
	ShellPlugin -h 
	
Please note that these parameters are the same that Queue takes at a normal run. Some of them might not have any impact on the result. 

The intermediary files (files passed between the pipeline's components) are stored in the current directory as hidden files during a run, and must be deleted by hand before the next run.
