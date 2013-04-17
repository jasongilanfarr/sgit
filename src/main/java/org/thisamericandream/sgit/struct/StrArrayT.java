package org.thisamericandream.sgit.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class StrArrayT extends Structure {
  public Pointer strings;
  public NativeLong length;
  
  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("strings", "length");
  }
}
