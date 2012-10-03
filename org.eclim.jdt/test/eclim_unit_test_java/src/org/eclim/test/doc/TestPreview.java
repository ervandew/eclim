package org.eclim.test.doc;

import java.util.Map;

/**
 * A test class for javadoc previews.
 *
 * @author Eric Van Dewoestine
 */
public class TestPreview
{
  private List list;
  private Map map;

  public static final void main(String[] args)
  {
    TestPreview p = new TestPreview(args);
    p.test();
  }

  /**
   * Constructs a new instance from the supplied arguments.
   *
   * @param args The arguments.
   */
  public TestPreview(String[] args)
  {
  }

  /**
   * A test method.
   *
   * @return a test {@link String}
   */
  public String test()
  {
    map.put("test", "test");
  }
}
