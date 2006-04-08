/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.jdt.command.complete;

import org.eclim.util.file.Position;

/**
 * Represents a proposed code completion result.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteResult
{
  private String filename;
  private int type;
  private String completion;
  private String signature;
  private int replaceStart;
  private int replaceEnd;

  /**
   * Constructs a new instance.
   */
  public CodeCompleteResult (
      int _type,
      String _filename,
      String _completion,
      String _signature,
      int _replaceStart,
      int _replaceEnd)
  {
    type = _type;
    filename = _filename;
    completion = _completion;
    signature = _signature;
    replaceStart = _replaceStart;
    replaceEnd = _replaceEnd;
  }

  /**
   * Get completion element type..
   *
   * @return The completion element type.
   */
  public int getType ()
  {
    return this.type;
  }

  /**
   * Get filename.
   *
   * @return filename as String.
   */
  public String getFilename ()
  {
    return this.filename;
  }

  /**
   * Get completion.
   *
   * @return completion as String.
   */
  public String getCompletion ()
  {
    return this.completion;
  }

  /**
   * Get signature.
   *
   * @return signature as String.
   */
  public String getSignature ()
  {
    return this.signature;
  }

  /**
   * Get replaceStart.
   *
   * @return replaceStart as int.
   */
  public int getReplaceStart ()
  {
    return this.replaceStart;
  }

  /**
   * Get replaceEnd.
   *
   * @return replaceEnd as int.
   */
  public int getReplaceEnd ()
  {
    return this.replaceEnd;
  }
}
