package org.thisamericandream.sgit;


enum ReferenceType {
  Invalid(0),
  Oid(1),
  Symbolic(2),
  ListAll(Oid.id|Symbolic.id);

  public final int id;
  
  ReferenceType(int id) {
    this.id = id;
  }
  
  public static ReferenceType forId(int id) {
    for(ReferenceType lvl: ReferenceType.values()) {
      if (lvl.id == id) {
        return lvl;
      }
    }
    return null;
  }
  
}
