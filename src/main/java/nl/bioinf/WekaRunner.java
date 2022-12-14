package nl.bioinf;

import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Weka Class with WekaRunner class builds model from input file
 * saves model to a file and use that file to classify new instances.
 */
public class WekaRunner {
    private final String modelFile = "testdata/smo.model";


    /**
     * start - starting Algorithm, building the model, classify new instances
     */
    public void start(String datafile) {
        String unknownFile = "testdata/Unknown_data_pdac.arff";
        try {
            System.out.println("\n---------------------------------------------------------------------------------\n\n");
            // Building the model
            Instances instances = loadArff(datafile);
            // J48 is public class weka.classifiers.trees
            SMO smo = buildClassifier(instances);
            saveClassifier(smo);
            SMO fromFile = loadClassifier();

            // Using the model
            Instances unknownInstances = loadArff(unknownFile);
            classifyNewInstance(fromFile, unknownInstances);

            System.out.println("\nThe file is stored in the testdata folder." +
                    "\nThe classified instances are in the last column of the newly created file. \n" +
                    "If there is a 1, it means the patient probably has PDAC. With a 2, the patient has \n" +
                    "pancreatic conditions that are non-cancerous. And with a 3, the person seems to be healthy.\n" +
                    "(Not 100% guaranteed)\n\n");
            System.out.println("----------------------------------------------------------------------------------");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * classifyNewInstance - using the model printing the instances classified by the model
     * @param tree - the model
     * @param unknownInstances - data with to be classified instances with the model
     */
    private void classifyNewInstance(SMO tree, Instances unknownInstances) throws Exception {
        // create copy
        Instances labeled = new Instances(unknownInstances);
        // label instances
        for (int i = 0; i < unknownInstances.numInstances(); i++) {
            double clsLabel = tree.classifyInstance(unknownInstances.instance(i));
            labeled.instance(i).setClassValue(clsLabel);
        }

        System.out.println("Please, provide a name for the output file where the classified data will be stored");
        Scanner scanner = new Scanner(System.in);
        String outputFile = "testdata/" + scanner.next();

        try {
            int index = outputFile.lastIndexOf('.');
            while (index <= 0) {
                System.out.println("Please enter a filename with .arff extension. Example: Labeled.arff");
                outputFile = "testdata/" + scanner.next();

                index = outputFile.lastIndexOf('.');
            }


            String extension = outputFile.substring(index + 1);
            while (!extension.equals("arff")) {
                System.out.println("Please enter a filename with .arff extension. Example: Labeled.arff");
                outputFile = "testdata/" + scanner.next();
            }

            File classifiedFile = new File(outputFile);
            if (classifiedFile.createNewFile()) {
                System.out.println("File created: " + classifiedFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(outputFile);
            myWriter.write(String.valueOf(labeled));
            myWriter.close();
            System.out.println("Successfully exported classified instances to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }


    /**
     * loadClassifier - reads saved classifier
     * @return Class with model from file
     */
    private SMO loadClassifier() throws Exception {
        // deserialize model
        return (SMO) weka.core.SerializationHelper.read(modelFile);
    }

    /**
     * saveClassifier - serialize tree to the modelFile
     * @param smo - built tree
     */
    private void saveClassifier(SMO smo) throws Exception {
        //post 3.5.5
        // serialize model
        weka.core.SerializationHelper.write(modelFile, smo);
    }

    /**
     * buildClassifier
     * @param instances - data
     * @return tree - fully tree build by Classifier in Weka
     */
    private SMO buildClassifier(Instances instances) throws Exception {
        String[] options = new String[1];       // Use supervised discretization to process numeric attributes
        SMO tree = new SMO();            // set the options
        tree.buildClassifier(instances);        // build classifier
        return tree;
    }


    /**
     * Instances loadArff - load file
     * @param datafile - String name of datafile from input
     * @return data
     */
    private Instances loadArff(String datafile) throws IOException {
        try {
            // loading data from files
            DataSource source = new DataSource(datafile);
            // returns full dataset, can be null in case of an error
            Instances data = source.getDataSet();

            // setting class attribute if the data format does not provide this information
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);
            return data;
        } catch (Exception e) {
            throw new IOException("could not read from file");
        }
    }
}
