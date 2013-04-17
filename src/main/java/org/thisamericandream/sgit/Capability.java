package org.thisamericandream.sgit;


enum Capability {
  Threads(1 << 0),
  Https(1 << 1);
  
  public final int id;
  
  Capability(int id) {
    this.id = id;
  }
  
  public static Capability forId(int id) {
    for(Capability lvl: Capability.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
  
}
