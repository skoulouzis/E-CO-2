package eu.edisonproject.classification.prepare.controller;

import eu.edisonproject.classification.prepare.model.DocumentObject;
/*
 * @author Michele Sparamonti
 */
public interface IDataPrepare {

	public void execute();
	
	public void extract(DocumentObject jp, String path);
	//public void clean(DocumentObject jp);
        public void clean(String description);
	
	
}
