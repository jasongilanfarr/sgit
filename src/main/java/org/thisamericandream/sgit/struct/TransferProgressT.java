package org.thisamericandream.sgit.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

public class TransferProgressT extends Structure {
  public int totalObjects;
  public int indexedObjects;
  public int receivedObjects;
  public NativeLong receivedBytes;
  
  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("totalObjects", "indexedObjects", "receivedObjects", "receivedBytes");
  }
}
