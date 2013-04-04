package org.thisamericandream.sgit.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ConfigEntryT extends Structure {
  public String name;
  public String value;
  public int level;
  
  public static enum Level {
    System(1),
    XDG(2),
    Global(3),
    Local(4),
    Highest(-1);
    
    public final int id;
    
    Level(int id) {
      this.id = id;
    }
    
    public static Level forId(int id) {
      for(Level lvl: Level.values()) {
        if (lvl.id == id) {
          return lvl;
        }
      }
      return null;
    }
  }
  
  public Level getLevel() {
    return Level.forId(level);
  }
  
  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("name", "value", "level");
  }

}
