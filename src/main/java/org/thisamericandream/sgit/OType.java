package org.thisamericandream.sgit;

public enum OType {
  Any(-2),
  Bad(-1),
  _Ext1(0),
  Commit(1),
  Tree(2),
  Blob(3),
  Tag(4),
  _Ext2(5),
  OffsetDelta(6),
  RefDelta(7);
  
  public final int id;
  
  OType(int id) {
    this.id = id;
  }
}
