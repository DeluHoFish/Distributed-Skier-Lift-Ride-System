package client.parttwo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CreateCSVRecord {

  private final File file = new File("data/request_record.csv");

  public CreateCSVRecord() {
    this.createFile();
  }

  private void createFile() {
    File parentDir = this.file.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        System.out.println("Failed to create directory: " + parentDir.getAbsolutePath());
        return;
      }
    }
    String[] titles = new String[]{"start_time", "request_type", "latency", "response code"};
    try(FileWriter writer = new FileWriter(this.file)){
      writer.append(String.join(",", titles));
      writer.append("\n");
      System.out.println("The file was created. Filepath: data/request_record.csv");
    } catch (IOException e){
      System.out.println("An error occurred when creating the file.");
      e.printStackTrace();
    }
  }

  public synchronized void writeFile(List<String[]> inputs){

    File parentDir = this.file.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        System.out.println("Failed to create directory: " + parentDir.getAbsolutePath());
        return;
      }
    }

    try(FileWriter writer = new FileWriter(this.file, true)){
      for(String[] row: inputs) {
        writer.append(String.join(",", row));
        writer.append("\n");
      }
    } catch (IOException e) {
      System.out.println("An error occurred when writing the file.");
      e.printStackTrace();
    }
  }
}
