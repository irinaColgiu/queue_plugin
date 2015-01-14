package graph2xml;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: irina
 * Date: 08/10/12
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
public class GraphNode {

    public String name;
    public List<File> inputs;
    public List<File> outputs;
    public String cmd;

    public GraphNode() {
       // this.name = name;
       // this.cmd = cmd;
    }

    public void fillInputs(scala.collection.Iterator<File> iter) {
        while(iter.hasNext()) {
            inputs.add((File)iter.next());
        }
    }

    public void fillOutputs(scala.collection.Iterator<File> iter) {
        while(iter.hasNext()) {
            outputs.add((File)iter.next());
        }
    }
}
