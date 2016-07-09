package eu.edisonproject.classification.flow.model;



import eu.edisonproject.classification.prepare.controller.DataPrepare;
import eu.edisonproject.classification.prepare.controller.IDataPrepare;

public class DataFlow implements IDataFlow {

	@Override
	public void dataPreProcessing(String inputPath, String outputPath) {
		IDataPrepare dp = new DataPrepare(inputPath, outputPath);
		dp.execute();
	}

	
}
