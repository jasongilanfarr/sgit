package org.thisamericandream.sgit;

public enum FileMode {
  New(0000000),
  Tree(0040000),
  Blob(0100644),
  Executable(0100755),
  Link(0120000),
  Commit(0160000);
  
  public final int id;
  
  FileMode(int id) {
    this.id = id;
  }
  
  public static FileMode forId(int id) {
    for(FileMode lvl: FileMode.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
}
