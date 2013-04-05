package org.thisamericandream.sgit.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ErrorT extends Structure {
  public String message;
  public int klass;
  
  @Override
  public List<String> getFieldOrder() {
    return Arrays.asList("message", "klass");
  }
}
