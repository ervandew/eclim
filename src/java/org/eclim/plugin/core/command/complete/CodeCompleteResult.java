/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.core.command.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a code completion result.
 *
 * @author Eric Van Dewoestine
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
   * @param completion The completion string.
   * @param description Description of the completion.
   * @param shortDescription Short descriptoin of the completion.
   */
  public CodeCompleteResult (
      String completion, String description, String shortDescription)
  {
    this.completion = completion;
    this.description = description;
    this.shortDescription = shortDescription;

    if(this.description != null){
      this.description = StringUtils.replace(this.description, "\n", "<br/>");
    }

    if(this.shortDescription != null){
      this.shortDescription =
        StringUtils.replace(this.shortDescription, "\n", "<br/>");
    }

    if(this.description != null && this.shortDescription == null){
      this.shortDescription = createShortDescription(this.description);
    }
  }

  /**
   * Gets the completion string.
   *
   * @return The completion.
   */
  public String getCompletion()
  {
    return completion;
  }

  /**
   * Sets the completion for this instance.
   *
   * @param completion The completion.
   */
  protected void setCompletion(String completion)
  {
    this.completion = completion;
  }

  /**
   * Gets the completion description.
   *
   * @return The completion description.
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Sets the description for this instance.
   *
   * @param description The description.
   */
  protected void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Gets the short description.
   *
   * @return The short description.
   */
  public String getShortDescription()
  {
    return shortDescription;
  }

  /**
   * Sets the shortDescription for this instance.
   *
   * @param shortDescription The shortDescription.
   */
  protected void setShortDescription(String shortDescription)
  {
    this.shortDescription = shortDescription;
  }

  /**
   * Creates a short description based on the supplied full description.
   *
   * @param description The description.
   * @return The short description.
   */
  public static String createShortDescription(String description)
  {
    if(description == null){
      return null;
    }

    String shortDesc = description;
    Matcher matcher = FIRST_LINE.matcher(shortDesc);
    if(shortDesc.length() > 1 && matcher.find(1)){
      shortDesc = shortDesc.substring(0, matcher.start() + 1);
      if(shortDesc.endsWith("<")){
        shortDesc = shortDesc.substring(0, shortDesc.length() - 1);
      }
    }
    shortDesc = shortDesc.replaceAll("\n", StringUtils.EMPTY);
    shortDesc = shortDesc.replaceAll("<.*?>", StringUtils.EMPTY);
    return StringUtils.abbreviate(shortDesc, MAX_SHORT_DESCRIPTION_LENGTH);
  }

  /**
   * Determines if this object is equal to the supplied object.
   *
   * @param other The object to test equality with.
   * @return true if the objects are equal, false otherwise.
   */
  public boolean equals(Object other)
  {
    if (!(other instanceof CodeCompleteResult)) {
      return false;
    }
    if (this == other) {
      return true;
    }
    CodeCompleteResult result = (CodeCompleteResult)other;
    boolean equal = new EqualsBuilder()
      .append(getCompletion(), result.getCompletion())
      .append(getShortDescription(), result.getShortDescription())
      .isEquals();

    return equal;
  }

  /**
   * Gets the hash code for this object.
   *
   * @return The hash code for this object.
   */
  public int hashCode()
  {
    return new HashCodeBuilder(18, 38)
      .append(completion)
      .append(shortDescription)
      .toHashCode();
  }
}
