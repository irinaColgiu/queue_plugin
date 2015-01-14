import graph2xml.Graph2XMLConversion
import java.io.File
import org.apache.commons.io.FilenameUtils
import org.broadinstitute.sting.commandline.CommandLineProgram
import org.broadinstitute.sting.queue.engine.QGraph
import org.broadinstitute.sting.queue.util.{QJobReport, Logging}
import org.broadinstitute.sting.queue.{QCommandLine, QScript, QScriptManager}
import org.broadinstitute.sting.utils.classloader.PluginManager
import org.broadinstitute.sting.utils.io.IOUtils
import collection.JavaConversions._
//import scala.collection.JavaConverters._
import org.broadinstitute.sting.utils.exceptions.UserException
import org.broadinstitute.sting.utils.io.IOUtils
import java.util.{ResourceBundle, Arrays}
import org.broadinstitute.sting.utils.classloader.PluginManager



object QCommandLineChild extends Logging {
  def main(argv: Array[String]) {

   // val args = new Array[String](9)

    if(argv.length == 1 && (argv(0).equals("--help") || (argv(0).equals("-h")))) {
      print(" This is a plugin for GATK/Queue. The running of this plugin results in 2 output files:\n - a shell script \n - an xml containing the elements of the pipeline as collection of nodes from a directed acyclic graph\n")
      print(" This tool takes the same parameters as GATK/Queue, which are explained below.\n\n")
      print("Example of run: java -jar ShellPlugin.jar -S /path/ExampleUnifiedGenotyper.scala -R /path/exampleFASTA.fasta -I /path/exampleBAM.bam -tempDir /path/tmp -run")
    }
  /*
    args(0) = "-S"
  //  args(1) = "/home/irina/Work/GATK-Queue/Sting/public/scala/qscript/org/broadinstitute/sting/queue/qscripts/examples/ExampleCountReads.scala"
    args(1) = "/home/irina/Work/GATK-Queue/Sting/public/scala/qscript/org/broadinstitute/sting/queue/qscripts/examples/ExampleUnifiedGenotyper.scala"
  //  args(1) = "/home/irina/Work/GATK-Queue/Sting/public/scala/qscript/org/broadinstitute/sting/queue/qscripts/examples/ExampleCountLoci.scala"
    args(2) = "-R"
    args(3) = "/home/irina/Work/GATK-Queue/Sting/public/testdata/exampleFASTA.fasta"
    args(4) = "-I"
    args(5) = "/home/irina/Work/GATK-Queue/Sting/public/testdata/exampleBAM.bam"
    args(6) = "-tempDir"
  //  args(7) = "/home/irina/Work/GATK-Queue/Sting/tmp"
    args(7) = "/home/irina/Work/GATK-Queue/QueuePlugin/tmp"
    args(8) = "-run"
      */

    //  args(10)= "/home/irina/Work/GATK-Queue/Sting/gv_results/ExUnified.gv"

    val qCommandLine = new QCommandLineChild

    val shutdownHook = new Thread {
      override def run() {
        logger.info("Shutting down jobs. Please wait...")
        qCommandLine.shutdown()
      }
    }

    Runtime.getRuntime.addShutdownHook(shutdownHook)

    try {
      CommandLineProgram.start(qCommandLine, argv)

      try {
        Runtime.getRuntime.removeShutdownHook(shutdownHook)
        qCommandLine.shutdown()
      } catch {
        case _ => /* ignore, example 'java.lang.IllegalStateException: Shutdown in progress' */
      }
      if (CommandLineProgram.result != 0)
        System.exit(CommandLineProgram.result);
    } catch {
      case e: Exception => CommandLineProgram.exitSystemWithError(e)
    }
  }


}


class QCommandLineChild extends QCommandLine {


  private val qScriptManager = new QScriptManager
  private val qGraph = new QGraphChild
  private var qScriptClasses: File = _
  private var shuttingDown = false

  private lazy val pluginManager = {
    qScriptClasses = IOUtils.tempDir("Q-Classes-", "", settings.qSettings.tempDirectory)
    qScriptManager.loadScripts(scripts, qScriptClasses)
    new PluginManager[QScript](classOf[QScript], Seq(qScriptClasses.toURI.toURL))
  }


  override def execute = {

    if (settings.qSettings.runName == null)
      settings.qSettings.runName = FilenameUtils.removeExtension(scripts.head.getName)


    qGraph.settings = settings

    val allQScripts = pluginManager.createAllTypes();
      val script = allQScripts(0)

      logger.info("Scripting " + pluginManager.getName(script.getClass.asSubclass(classOf[QScript])))
      loadArgumentsIntoObject(script)
      script.qSettings = settings.qSettings
      try {
        script.script()
      } catch {
        case e: Exception =>
          throw new UserException.CannotExecuteQScript(script.getClass.getSimpleName + ".script() threw the following exception: " + e, e)
      }
      script.functions.foreach(qGraph.add(_))
      logger.info("Added " + script.functions.size + " functions")

       qGraph.run()


      val functionsAndStatus = qGraph.getFunctionsAndStatus
      val success = qGraph.success

      // walk over each script, calling onExecutionDone
      for (script <- allQScripts) {
        val scriptFunctions = functionsAndStatus.filterKeys(f => script.functions.contains(f))
        script.onExecutionDone(scriptFunctions, success)
      }

      logger.info("Script %s with %d total jobs".format(if (success) "completed successfully" else "failed", functionsAndStatus.size))

      if (!qGraph.success) {
        logger.info("Done with errors")
        qGraph.logFailed()
        1
      } else {
        0
      }

  }

}
