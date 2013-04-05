package org.thisamericandream.sgit;

public enum StatusCode {
  Current(0),
  IndexNew(1 << 0),
  IndexModified(1 << 1),
  IndexDeleted(1 << 2),
  IndexRenamed(1 << 3),
  IndexTypeChange(1 << 4),
  WorkingTreeNew(1 << 7),
  WorkingTreeModified(1 << 8),
  WorkingTreeDeleted(1 << 9),
  WorkingTreeTypeChange(1 << 10),
  Ignored(1 << 14);
  
  public final int id;
  
  StatusCode(int id) {
    this.id = id;
  }
  
  public static StatusCode forId(int id) {
    for(StatusCode lvl: StatusCode.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
}
