# Description
The EDISON COmpetencies ClassificatiOn (E-CO-2) service is a distributed automated service designed to enable Data Science gap analysis. It can identify the similarity of a document against a set of predefined categories. It can therefore be used to perform a gap analysis based on the EDISON taxonomy to identify mismatches between education and industry.  Students, data analysts, educators and other stakeholders can use these tools to identify the gaps in their skills and competences. Moreover, by constantly collecting data from sources like job ads and postgraduate programs we will be able to identify trends from both the job market and education. 

that allow an API consumer (in this case the EDISON community portal) to analyze documents and retrieve results. The Local Catalog Manager plugs in into multiple data sources giving the opportunity to have a unified view of various data sources. Connecting to a larger overlay network it is possible to discover and request multiple datasets that can be used to perform more analytics. The focused crawler is used to collect documents from the web related with the job market and education of data science. The database is used to efficiently store and query input documents, analysis results, a context corpus and category vectors. The task scheduler queries the database in regular time intervals for new documents that need to be analyzed or updates in the context corpus and schedules tasks for the E-CO-2 analyzer. The E-CO-2 analyzers is the main analysis component that is responsible for providing a similarity matrix of an input document against the EDISON taxonomy and generate category vectors based on a context corpus.

# Use
The E-CO-2 service is dockerised. To deploy download the Docker file from: https://github.com/skoulouzis/E-CO-2/releases/download/v0.0.2/Dockerfile.

Build the image:
```
docker build -t e-co-2-docker .
```
Run the service:
```
docker run -e RELESE_VERSION=0.0.2 --name e-co-2-inst -p 127.0.0.1:9999:9999 -d e-co-2-docker
```

The REST API documentation can be found in:
```
http://localhost:9999/doc/
```
# Acknowledgment
This work was funded by the [EDISON](http://edison-project.eu) projects  

  

