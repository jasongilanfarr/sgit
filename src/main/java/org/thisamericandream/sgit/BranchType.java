package org.thisamericandream.sgit;


enum BranchType {
  Local(1),
  Remote(2),
  LocalOrRemote(Local.id | Remote.id);

  
  public final int id;
  
  BranchType(int id) {
    this.id = id;
  }
  
  public static BranchType forId(int id) {
    for(BranchType lvl: BranchType.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
  
}
