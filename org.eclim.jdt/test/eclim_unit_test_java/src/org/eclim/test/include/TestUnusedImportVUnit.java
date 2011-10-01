package org.eclim.test.include;

import java.lang.Math;
import static java.lang.Math.PI;
import static java.io.File.separator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUnusedImportVUnit
{
  private Map map = new HashMap();
  private String SEP = separator;

  public void test(){
    Math.max(1, 0);
  }
}
