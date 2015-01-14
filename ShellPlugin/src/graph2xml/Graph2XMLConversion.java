package graph2xml;

import org.broadinstitute.sting.queue.engine.FunctionEdge;
import org.broadinstitute.sting.queue.engine.QNode;

import javax.xml.stream.*;

import java.io.*;
//import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import scala.collection.JavaConversions.*;


/**
 * Created with IntelliJ IDEA.
 * User: irina
 * Date: 08/10/12
 * Time: 13:58
 * To change this template use File | Settings | File Templates.
 */
public class Graph2XMLConversion {


  /*  public void traverseBreadthFirst(String startName){
        int current, item, startindex = 0;
        boolean finished = false;
        Queue myQueue = new Queue();
        System.out.println(startName);

        for (int i = 0; i < 7; i++)
            visited[i] = false;
        while (Vertex[startindex] != startName)
            // search array from subscript 0
            startindex++;
        current = startindex;
        visited[startindex] = true;             // OK, we've been there!
        do {
            for (int column= 0; column < 7; column++) {
                // enqueue its neighbours
                if (Edge[current][column] != 0 && !visited[column])
                    myQueue.enqueue(column);
            }
            item = myQueue.dequeue();      // remove head of queue
            if (item == -1)                // if empty, dequeue returns -1
                finished = true;
            else {
                System.out.println(Vertex[item]);  // write out the name
                visited[item] = true;             // register it as visited
                current = item;
            }
        } while (!finished);
    }
*/


    public List<String> readXml(String filePath) {
        List<String> cmdList = new ArrayList<String>();
        try {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);

            while(xmlStreamReader.hasNext()) {
                int eventCode = xmlStreamReader.next();
                if(eventCode == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("cmd")) {
                    String cmd = xmlStreamReader.getElementText();
                 //   System.out.println("Command read --------"+cmd);
                   cmdList.add(cmd);
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(XMLStreamException e) {
            e.printStackTrace();
        }
        return cmdList;
    }

    public void writeShellScript(List<String> cmdList, String fileName) {
        try {
               FileWriter outFile = new FileWriter(fileName);
               PrintWriter out = new PrintWriter(outFile);
               for(String cmd : cmdList) {
                   out.write(cmd+"\n");
               }
               out.close();
           } catch (IOException e){
              e.printStackTrace();
           }

        for(String cmd : cmdList) {

        }
    }

    public void write(List<GraphNode> edgesSet) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(new java.io.FileWriter("adag.xml"));

            writer.writeStartDocument();

            for(GraphNode edge : edgesSet) {
                writer.writeStartElement("node");
                writer.writeAttribute("name", edge.name);

                writer.writeStartElement("cmd");
                String cmdRefined = edge.cmd.replace("&& \\ 'java'", "&& 'java'");
            //    writer.writeCharacters(edge.cmd);
                writer.writeCharacters(cmdRefined);
                writer.writeEndElement();

                writer.writeStartElement("inputs");
            //    QNode qNode = edge.inputs;

          //      scala.collection.Iterator iterIn = qNode.files().iterator();
         //       while(iterIn.hasNext()){
                for(File file : edge.inputs) {
                    writer.writeStartElement("inputFile");
                  //  File file = (File)iterIn.next();

                    writer.writeCharacters(file.getName());
                    writer.writeEndElement();
                }
                writer.writeEndElement();                 // end of inputs

                //start of outputs:
                writer.writeStartElement("outputs");
                writer.writeStartElement("outputFile");
            //    QNode qNodeOut = edge.outputs;
            //    scala.collection.Iterator iterOut = qNode.files().iterator();
            //    while(iterOut.hasNext()){
                for(File file : edge.outputs) {
                    writer.writeStartElement("outputFile");
                   // File file = (File)iterOut.next();

                    writer.writeCharacters(file.getName());
                    writer.writeEndElement();
                }
                writer.writeEndElement();

                writer.writeEndElement();
            }

            writer.writeEndDocument();

            writer.flush();
            writer.close();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void write1(Set<FunctionEdge> edgesSet) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(new java.io.FileWriter("/home/irina/Work/GATK-Queue/QueuePlugin/output2.xml"));

            writer.writeStartDocument();

            for(FunctionEdge edge : edgesSet) {
                writer.writeStartElement("node");
                writer.writeAttribute("name", edge.shortDescription());

                writer.writeStartElement("cmd");
                writer.writeCharacters(edge.function().description());
                writer.writeEndElement();

                writer.writeStartElement("inputs");
                QNode qNode = edge.inputs();

                scala.collection.Iterator iterIn = qNode.files().iterator();
                while(iterIn.hasNext()){
                    writer.writeStartElement("inputFile");
                    File file = (File)iterIn.next();

                    writer.writeCharacters(file.getName());
                    writer.writeEndElement();
                }
                writer.writeEndElement();                 // end of inputs

                //start of outputs:
                writer.writeStartElement("outputs");
                writer.writeStartElement("outputFile");
                QNode qNodeOut = edge.outputs();
                scala.collection.Iterator iterOut = qNode.files().iterator();
                while(iterOut.hasNext()){
                    writer.writeStartElement("outputFile");
                    File file = (File)iterOut.next();

                    writer.writeCharacters(file.getName());
                    writer.writeEndElement();
                }
                writer.writeEndElement();

                writer.writeEndElement();
            }

            writer.writeEndDocument();

            writer.flush();
            writer.close();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


