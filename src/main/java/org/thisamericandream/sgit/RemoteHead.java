package org.thisamericandream.sgit;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

class RemoteHead extends Structure {
  public boolean local;
  public Oid oid;
  public Oid loid;
  public String name;
  
  @Override
  public List<String> getFieldOrder() {
    return Arrays.asList("local", "oid", "loid", "name");
  }
}