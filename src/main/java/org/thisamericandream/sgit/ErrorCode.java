package org.thisamericandream.sgit;


enum ErrorCode {
  Ok(0),
  Error(-1),
  NotFound(-3),
  Exists(-4),
  Ambiguous(-5),
  BUFS(-6),
  User(-7),
  BareRepo(-8),
  OrphanedHead(-9),
  Unmerged(-10),
  NonFastForward(-11),
  InvalidSpec(-12),
  MergeConflict(-13),
  Passthrough(-30),
  IterOver(-31);
  
  public final int id;
  
  ErrorCode(int id) {
    this.id = id;
  }
  
  public static ErrorCode forId(int id) {
    for(ErrorCode lvl: ErrorCode.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
  
}
