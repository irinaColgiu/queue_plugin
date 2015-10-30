GATK/Queue plugin

The Genome Analysis Toolkit (or GATK - https://www.broadinstitute.org/gatk/) has a pipelining system called Queue (https://www.broadinstitute.org/gatk/guide/topic?name=queue) which uses QScripts for describing the flow of data that goes through a pipeline, together with the processing done on it. The QScprits are meant to be executed in parallel in a distributed environment. The current application (Queue_plugin) is used for converting a QScript into a shell script so that the pipeline can be executed serially as well, not only in parallel as done by GATK Queue.

The way Queue_plugin achieves this is by parsing the QScript and constructing an abstract directed acyclic graph (ADAG). An ADAG follows the internal representation that Queue uses for the graph of jobs: nodes represent a state described by the bunch of files, edges represent transitions between the states - the next state is obtained by running a command. The ADAG is afterwards transformed into a shell script with the same functionality as the original QScript. The shell script is obtained by topologically sorting the ADAG, hence the result is a sequence of steps(shell commands usually involving GATK) to be executed serially.

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
The GATK/Queue has been used as a library for the implementation of this application that takes a QScript and converts it into a shell script. The plugin's functionality has been obtained by extending the classes of interest from Queue (inheritance).

Observations: 

The program should be run at least with the parameters mentioned above. A complete list of parameters can be obtained by consulting the help. 
	
Please note that these parameters are the same that Queue takes at a normal run. Some of them might not have any impact on the result. 

The intermediary files (files passed between the pipeline's components) are stored in the current directory as hidden files during a run, and should deleted by hand before the next run.
