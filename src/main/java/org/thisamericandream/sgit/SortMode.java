package org.thisamericandream.sgit;


enum SortMode {
  None(0),
  Topological(1 << 0),
  Time(1 << 1),
  Reverse(1 << 2);
  
  public final int id;
  
  SortMode(int id) {
    this.id = id;
  }
  
  public static SortMode forId(int id) {
    for(SortMode lvl: SortMode.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
  
}
