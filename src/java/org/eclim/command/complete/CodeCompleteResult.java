/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.command.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a code completion result.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCompleteResult
{
  private static final Pattern FIRST_LINE =
    Pattern.compile("(\\.\\s|\\.<|<br|<BR|<p|<P)");
  private static final int MAX_SHORT_DESCRIPTION_LENGTH = 74;

  private String completion;
  private String description;
  private String shortDescription;

  /**
   * Constructs a new instance.
   *
   * @param _completion The completion string.
   * @param _description Description of the completion.
   * @param _shortDescription Short descriptoin of the completion.
   */
  public CodeCompleteResult (
      String _completion, String _description, String _shortDescription)
  {
    completion = _completion;
    description = _description;
    if(description != null){
      description = StringUtils.replace(description, "\n", "<br/>");
    }
    shortDescription = _shortDescription;
    if(shortDescription != null){
      shortDescription = StringUtils.replace(shortDescription, "\n", "<br/>");
    }

    if(description != null && shortDescription == null){
      shortDescription = createShortDescription(description);
    }
  }

  /**
   * Gets the completion string.
   *
   * @return The completion.
   */
  public String getCompletion ()
  {
    return completion;
  }

  /**
   * Sets the completion for this instance.
   *
   * @param completion The completion.
   */
  protected void setCompletion (String completion)
  {
    this.completion = completion;
  }

  /**
   * Gets the completion description.
   *
   * @return The completion description.
   */
  public String getDescription ()
  {
    return description;
  }

  /**
   * Sets the description for this instance.
   *
   * @param description The description.
   */
  protected void setDescription (String description)
  {
    this.description = description;
  }

  /**
   * Gets the short description.
   *
   * @return The short description.
   */
  public String getShortDescription ()
  {
    return shortDescription;
  }

  /**
   * Sets the shortDescription for this instance.
   *
   * @param shortDescription The shortDescription.
   */
  protected void setShortDescription (String shortDescription)
  {
    this.shortDescription = shortDescription;
  }

  /**
   * Creates a short description based on the supplied full description.
   *
   * @param _description The description.
   * @return The short description.
   */
  public static String createShortDescription (String _description)
  {
    if(_description == null){
      return null;
    }

    String shortDesc = _description;
    Matcher matcher = FIRST_LINE.matcher(shortDesc);
    if(shortDesc.length() > 1 && matcher.find(1)){
      shortDesc = shortDesc.substring(0, matcher.start() + 1);
      if(shortDesc.endsWith("<")){
        shortDesc= shortDesc.substring(0, shortDesc.length() - 1);
      }
    }
    shortDesc = shortDesc.replaceAll("\n", StringUtils.EMPTY);
    shortDesc = shortDesc.replaceAll("<.*?>", StringUtils.EMPTY);
    return StringUtils.abbreviate(shortDesc, MAX_SHORT_DESCRIPTION_LENGTH);
  }

  /**
   * Determines if this object is equal to the supplied object.
   *
   * @param _other The object to test equality with.
   * @return true if the objects are equal, false otherwise.
   */
  public boolean equals (Object _other)
  {
    if (!(_other instanceof CodeCompleteResult)) {
      return false;
    }
    if (this == _other) {
      return true;
    }
    CodeCompleteResult result = (CodeCompleteResult)_other;
    boolean equal = new EqualsBuilder()
      .append(getCompletion(), result.getCompletion())
      .isEquals();

    return equal;
  }

  /**
   * Gets the hash code for this object.
   *
   * @return The hash code for this object.
   */
  public int hashCode ()
  {
    return new HashCodeBuilder(18, 38)
      .append(completion)
      .toHashCode();
  }
}
