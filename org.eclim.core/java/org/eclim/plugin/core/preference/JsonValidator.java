/**
 * Copyright (C) 2011 - 2021  Eric Van Dewoestine
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
package org.eclim.plugin.core.preference;

import org.eclim.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * Option validator that validates that the option value is valid json.
 *
 * @author Eric Van Dewoestine
 */
public class JsonValidator
  implements Validator
{
  private static final Logger logger = Logger.getLogger(JsonValidator.class);

  private static final Gson GSON = new Gson();

  private Class<?> type;
  private Validator itemValidator;

  public JsonValidator(Class<?> type, Validator itemValidator)
  {
    this.type = type;
    this.itemValidator = itemValidator;
  }

  @Override
  public boolean isValid(Object value)
  {
    if (value != null){
      try{
        Object result = GSON.fromJson((String)value, type);
        if (result != null && type.isArray() && itemValidator != null){
          Object[] results = (Object[])result;
          for(Object v : results){
            if (!itemValidator.isValid(v)){
              return false;
            }
          }
        }
      }catch(JsonParseException jpe){
        return false;
      }
    }
    return true;
  }

  @Override
  public String getMessage(String name, Object value)
  {
    if (value != null) {
      try{
        Object result = GSON.fromJson((String)value, type);
        if (type.isArray() && itemValidator != null){
          Object[] results = (Object[])result;
          for(Object v : results){
            if (!itemValidator.isValid(v)){
              return itemValidator.getMessage(name, v);
            }
          }
        }
      }catch(JsonParseException jpe){
        Throwable cause = jpe.getCause();
        if (cause != null && cause.getMessage() != null){
          return cause.getMessage();
        }
        return jpe.getMessage();
      }
    }
    return null;
  }

  @Override
  public OptionInstance optionInstance(Option option, String value)
  {
    try{
      Object result = GSON.fromJson(value, type);
      return new OptionInstance(option, result);
    }catch(JsonSyntaxException jse){
      logger.error("Malformed json for option: " + option.getName(), jse);
      return new OptionInstance(option, value);
    }
  }
}
