/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.dependency;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclim.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * The {@code SimpleClasspathFileManipulator} manipulates the .classpath file of an
 * eclipse project.
 *
 * It allows a user to
 * - add a dependency file
 * - remove a dependency file
 *
 * @author Lukas Roth
 *
 */
public class SimpleClasspathFileManipulator implements ClasspathFileManipulator
{
  private final Logger logger = Logger
      .getLogger(SimpleClasspathFileManipulator.class);
  private static final String ERROR_LOG_ENTRY =
      "Error while manipulating the eclipse classpath";
  private static final String CLASS_PATH_NODE_NAME = "classpath";
  private static final String KIND_ATTRIBUTE = "kind";
  private static final String KIND_ATTRIBUTE_LIB = "lib";
  private static final String PATH_ATTRIBUTE = "path";
  private static final String ELEMENT_NAME_CLASS_PATH_ENTRY = "classpathentry";

  /**
   * Adds a jar dependency entry for the jar at position
   * <code>dependencyFilePath</code> to the .classpath file (which is at path
   * <code>classPathFilePath</code>). After this you need to ensure that eclipse
   * refreshes the dependencies!
   */
  @Override
  public void addJarDependency(String dependencyFilePath, String classPathFilePath)
      throws ClasspathFileManipulatorException
  {
    Document doc = parseXML(classPathFilePath);
    Node classPathNode = getClassPathNode(doc, CLASS_PATH_NODE_NAME);

    List<Node> foundNodes = findChildNodes(dependencyFilePath, classPathNode);
    if (!foundNodes.isEmpty()) {
      logger.debug("There is already an entry '" + dependencyFilePath +
          "' --> it will not be added a second time.");
      return;
    }
    Element classPathEntry = doc.createElement(ELEMENT_NAME_CLASS_PATH_ENTRY);
    classPathEntry.setAttribute(KIND_ATTRIBUTE, KIND_ATTRIBUTE_LIB);
    classPathEntry.setAttribute(PATH_ATTRIBUTE, dependencyFilePath);
    classPathNode.appendChild(classPathEntry);

    writeXmlBackToFile(doc, classPathFilePath);
  }

  /**
   * Removes all jar dependency entries for the jar at position
   * <code>dependencyFilePath</code> from the .classpath file (which is at path
   * <code>classPathFilePath</code>). After this you need to ensure that eclipse
   * refreshes the dependencies!
   */
  @Override
  public void removeJarDependency(String dependencyFilePath,
      String classPathFilePath)
      throws ClasspathFileManipulatorException
  {
    Document document = parseXML(classPathFilePath);
    Node classPathNode = getClassPathNode(document, CLASS_PATH_NODE_NAME);
    List<Node> childesToRemove = findChildNodes(dependencyFilePath, classPathNode);
    if (childesToRemove.size() != 1) {
      logger.debug("There were " + childesToRemove.size() +
          " nodes which all fit to the dependency entry which gets removed now");
    }
    removeChildNodes(classPathNode, childesToRemove);
    writeXmlBackToFile(document, classPathFilePath);
  }

  private Node getClassPathNode(Document doc, String tagName)
      throws ClasspathFileManipulatorException
  {
    if (doc != null) {
      NodeList temp = doc.getElementsByTagName(tagName);
      if (temp != null) {
        Node retNode = temp.item(0);
        if (retNode != null) {
          return retNode;
        }
      }
    }
    throw new ClasspathFileManipulatorException(
        "Could not find the node: " + tagName);
  }

  private List<Node> findChildNodes(String dependencyFilePath, Node classPathNode)
  {
    NodeList childes = classPathNode.getChildNodes();
    List<Node> childesToRemove = new ArrayList<Node>();
    for (int i = 0; i < childes.getLength(); i++) {
      Node child = childes.item(i);
      if (child.getNodeName().equals(ELEMENT_NAME_CLASS_PATH_ENTRY) &&
          child.hasAttributes())
      {
        NamedNodeMap attributeMap = child.getAttributes();
        Node kind = attributeMap.getNamedItem(KIND_ATTRIBUTE);
        Node path = attributeMap.getNamedItem(PATH_ATTRIBUTE);
        if (kind != null && path != null &&
            kind.getNodeValue().equals(KIND_ATTRIBUTE_LIB) &&
            path.getNodeValue().equals(dependencyFilePath))
        {
          childesToRemove.add(child);
        }
      }
    }
    return childesToRemove;
  }

  private void removeChildNodes(Node classpath, List<Node> childesToRemove)
  {
    for (Node child : childesToRemove) {
      classpath.removeChild(child);
    }
  }

  private void writeXmlBackToFile(Document document, String filePath)
      throws ClasspathFileManipulatorException
  {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer;
      transformer = transformerFactory.newTransformer();

      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(new File(filePath));
      transformer.transform(source, result);
    } catch (Exception e) {
      throw new ClasspathFileManipulatorException(ERROR_LOG_ENTRY, e);
    }
  }

  private Document parseXML(String document)
      throws ClasspathFileManipulatorException
  {
    try {
      logger.debug("Parsing XML file: " + document);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new File(document));
      if (doc != null) {
        return doc;
      }
      throw new ClasspathFileManipulatorException(
          "Error parsing XML: doc is null after parsing");
    } catch (Exception e) {
      throw new ClasspathFileManipulatorException(ERROR_LOG_ENTRY, e);
    }
  }
}
