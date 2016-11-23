
package eu.edisonproject.utility.file;

/*
 * @author Michele Sparamonti
 */
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class WriterFile extends FileIO {

  public WriterFile(String filePath) {
    super(filePath);
  }

  public void writeFile(String text) {
    try {
      try (Writer writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(this.getFilePath()), "UTF-8"))) {
        writer.write(text);

        writer.flush();
      }
    } catch (IOException e) {
      System.out.println("Can't write the following file: " + this.getFilePath());
    }
  }

}
