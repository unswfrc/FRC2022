package frc.robot.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import frc.robot.utils.logger.Logger;
import frc.robot.Constants;

/**
 * Utility class for handling all auto command files.
 */
public class AutoFiles {
  public static final Constants constants = Constants.getInstance();

  /**
   * Updates internal auto command files from USB and parses them.
   * 
   * @return The parsed auto command data
   */
  public static ArrayList<AutoCommand> updateAndGetAuto() {
    updateInternalAuto();
    return readInternalAuto();
  }

  /**
   * Updates the internal auto commands file from the USB.
   */
  public static void updateInternalAuto() {
    //fetches correct paths for the specific auto to be run
    String[] pathUSB = getAutoFilePaths("USB");
    String[] pathInternalT = getAutoFilePaths("Internal");
    String pathInternal;
    if(pathUSB != null && pathInternalT != null && pathInternalT.length >= 1){
      pathInternal = pathInternalT[0];
    } else {return;}
    
    for (String pathString : pathUSB) {
      Path usbPath = Paths.get(pathString);
      Path internalPath = Paths.get(pathInternal);
      try {
        Files.copy(usbPath, internalPath, StandardCopyOption.REPLACE_EXISTING);
        break;
      } catch (IOException e) {
        Logger.warn("AutoFiles : USB -> Internal : Copy Failed : " + e.toString());
        continue;
      }
    }
  }

  /**
   * Reads and parses the internal auto command file.
   * 
   * @return The parsed auto command data.
   */
  public static ArrayList<AutoCommand> readInternalAuto() {return readInternalAuto(constants.AutoReadLim);}
  public static ArrayList<AutoCommand> readInternalAuto(int retries) {
    try {
      String[] pathInternalT = getAutoFilePaths("Internal");
      String pathInternal;
      if(pathInternalT != null && pathInternalT.length >= 1){
        pathInternal = pathInternalT[0];
      } else {throw new IOException("File Path Not Found");}

      BufferedReader br = new BufferedReader(new FileReader(pathInternal));
      String line;
      ArrayList<AutoCommand> commands = new ArrayList<>();

      while ((line = br.readLine()) != null) {
        line.strip();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        commands.add(new AutoCommand(line));
      }
      br.close();
      return commands;
    } catch (FileNotFoundException e) {
      if(retries > 0){
        Logger.error("AutoFiles : Internal Storage Auto File Not Found : " + e + 
            "reattemping : Try-" + (retries-constants.AutoReadLim) + " Of-" + constants.AutoReadLim);
        return readInternalAuto(retries-1);
      } else {
        Logger.error("AutoFiles : Internal Storage Auto File Not Found : " + e + "Terminating : use Backup Auto");
        return readHardCoded();
      }
    } catch (IOException e) {
      if(retries > 0){ 
        Logger.error("AutoFiles : Internal Storage Auto Read Error : " + e+ 
        "reattemping : Try-" + (retries-constants.AutoReadLim) + " Of-" + constants.AutoReadLim);
        return readInternalAuto(retries-1);
      } else {
        Logger.error("AutoFiles : Internal Storage Auto Read Error : " + e + "Terminating : use Backup Auto");
        return readHardCoded();
      }
    }
  }

  /**
   * Gets the hardcoded auto commands. Only to be used if all else fails.
   * 
   * @return The hardcoded auto command data.
   */
  public static ArrayList<AutoCommand> readHardCoded() {
    return new ArrayList<>(
        Arrays.asList(
            "driveArcade -0.7 0",
            "intakeRun 0.5",
            "wait 1000",
            "storageRun 0.5",
            "wait 700",
            "storageRun 0",
            "intakeRun 0",
            "driveArcade 0.7 0",
            "wait 2800",
            "storageRun 1",
            "wait 1000",
            "driveOff",
            "storageRun 0")
            .stream()
            .map(line -> new AutoCommand(line))
            .collect(Collectors.toList()));

  }

  /**
   * gets current use auto and adds appropriate filepath.
   * Todo : add systems for multiple auto
   * @param filePathLocation : which file path location to use, currently supports "USB", "Internal"
   * @return array of filepaths appended with autofile name
   */
  private static String[] getAutoFilePaths(String filePathLocation){
    String[] AutoUsbPaths;
    String fileName = constants.AutoFileName;

    if(filePathLocation == "USB"){
      AutoUsbPaths = new String[constants.UsbPaths.length];
      for(int i = 0; i < AutoUsbPaths.length && i < constants.UsbPaths.length; i++){
        AutoUsbPaths[i] = constants.UsbPaths[i] + fileName;
      }
    } else if (filePathLocation == "Internal"){
      AutoUsbPaths = new String[1];
      AutoUsbPaths[0] = constants.InternalPath + fileName;
    } else {
      Logger.error("Auto Command FilePath : FilePath does not exist : "+ filePathLocation);
      return null;
    }
    return AutoUsbPaths;
  }

}
