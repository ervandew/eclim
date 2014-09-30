package org.eclim.plugin.adt.command.complete;

import static com.android.SdkConstants.ATTR_CONTEXT;
import static com.android.SdkConstants.VIEW_FRAGMENT;
import static com.android.SdkConstants.VIEW_TAG;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.ui.EclimEditorSite;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

import org.eclipse.wst.sse.ui.StructuredTextEditor;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.android.ide.eclipse.adt.internal.editors.AndroidContentAssist;
import com.android.ide.eclipse.adt.internal.editors.AndroidXmlEditor;

import com.android.ide.eclipse.adt.internal.editors.common.CommonXmlEditor;

import com.android.ide.eclipse.adt.internal.editors.layout.LayoutContentAssist;

import com.android.ide.eclipse.adt.internal.editors.layout.gle2.DomUtilities;

import com.android.ide.eclipse.adt.internal.editors.uimodel.UiElementNode;
import com.android.ide.eclipse.adt.internal.editors.uimodel.UiResourceAttributeNode;

import com.android.ide.eclipse.adt.internal.editors.values.ValuesContentAssist;

import com.android.utils.Pair;

@Command(
name = "android_xml_complete",
options =
"REQUIRED p project ARG," +
"REQUIRED f file ARG," +
"REQUIRED o offset ARG," +
"REQUIRED e encoding ARG," +
"REQUIRED l layout ARG"
)
public class AdtXmlCodeCompleteCommand extends AbstractCodeCompleteCommand {
  private static final Logger logger =
    Logger.getLogger(AdtXmlCodeCompleteCommand.class);

  /** Regexp to detect a full attribute after an element tag.
   * <pre>Syntax:
   *    name = "..." quoted string with all but < and "
   * or:
   *    name = '...' quoted string with all but < and '
   * </pre>
   */
  private static Pattern sFirstAttribute = Pattern.compile(
      "^ *[a-zA-Z_:]+ *= *(?:\"[^<\"]*\"|'[^<']*')");  //$NON-NLS-1$
  /** Regexp to detect an element tag name */
  private static Pattern sFirstElementWord = Pattern.compile("^[a-zA-Z0-9_:.-]+"); //$NON-NLS-1$
  /** Regexp to detect whitespace */
  private static Pattern sWhitespace = Pattern.compile("\\s+"); //$NON-NLS-1$

  @Override
  protected Object getResponse(List<CodeCompleteResult> results)
  {
    logger.info("Code completion! {}", results.size());
    // TODO clean this up
    HashMap<String, Object> response = new HashMap<String, Object>();
    response.put("completions", results);
    return response;
  }

  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String projectName, String file, int offset)
    throws Exception
  {
    final IProject project = ProjectUtils.getProject(projectName);
    final IFile ifile = ProjectUtils.getFile(project, file);

    // pre-init the Model; ADT only looks for an existing one
    final IModelManager man = StructuredModelManager.getModelManager();
    IStructuredModel model = man.getModelForRead(ifile);
    IStructuredDocument doc = model.getStructuredDocument();

    // FIXME remove; experiments only
    Pair<Node, Node> context = DomUtilities.getNodeContext(doc, offset);
    Node current = context.getSecond();
    if (current != null) {
      logger.info("current={}; isElement={}", 
          current, current.getNodeType() == Node.ELEMENT_NODE);
    }

    // we need this Dummy to ensure it can get `doc`
    //  in all cases
    final StructuredTextViewer viewer = new DummyStructuredTextViewer(doc, offset, 1);
    CommonXmlEditor editor = new CommonXmlEditor();
    IEditorInput input = new FileEditorInput(ifile);
    editor.init(new EclimEditorSite(), input);
    logger.info("Root={}; delegate={}", editor.getUiRootNode(), editor.getDelegate());

    // init this field as well, since it uses this to 
    //  get the StructuredTextViewer to get `doc`
    Field mTextEditor = AndroidXmlEditor.class.getDeclaredField("mTextEditor");
    mTextEditor.setAccessible(true);
    mTextEditor.set(editor, new StructuredTextEditor() {

      @Override
      public StructuredTextViewer getTextViewer() {
        logger.info("Getting textViewer: {}", viewer);
        return viewer;
      }

    @Override
    protected void setSourceViewerConfiguration(
      SourceViewerConfiguration config) {
      // nop it
      }

    });

    // let the right one in
    final AndroidContentAssist ca = getContentAssist(project, file);

    // set this the hard way so it doesn't try to query the UI
    final Field mEditor = AndroidContentAssist.class.getDeclaredField("mEditor");
    mEditor.setAccessible(true);
    mEditor.set(ca, editor);

    // make sure it worked
    final IStructuredModel existing = man.getExistingModelForRead(doc);
    if (existing == null) {
      logger.warn("Couldn't create existing model for doc {}", doc); return new ICompletionProposal[0];
    }

    // FIXME testing
    final String wordPrefix = extractElementPrefix(viewer, offset);
    String parent = current.getNodeName();
    AttribInfo info = parseAttributeInfo(viewer, offset, offset - wordPrefix.length());
    logger.info("prefix=`{}`; info={}", wordPrefix, info);

    // computeAttributeProposals(proposals, viewer, offset, wordPrefix, currentUiNode,
    // parentNode, currentNode, parent, info, nextChar);
    // (computeAttributeValues(proposals, offset, parent, info.name, currentNode,
    //                 wordPrefix, info.skipEndTag, info.replaceLength)) {
    logger.info("parent={}; attributeName={}", parent, info.name);
    logger.info("VIEW_TAG={}; VIEW_FRAGMENT={}; ATTR_CONTEXT={}", 
        VIEW_TAG, VIEW_FRAGMENT, ATTR_CONTEXT);
    if (info.isInValue) {
      String[] choices = UiResourceAttributeNode.computeResourceStringMatches(
          editor, null /*attributeDescriptor*/, info.correctedPrefix);
      logger.info("res-choices={}", Arrays.asList(choices));
    }

    // execute
    logger.info("complete @{}", offset);
    ICompletionProposal[] props = ca.computeCompletionProposals(viewer,
        offset);
    logger.info("props ={}", (Object) props);

    if (props.length == 0) {
      PublicValuesContentAssist ca2 = new PublicValuesContentAssist();
      mEditor.set(ca2, editor);

      List<ICompletionProposal> propsList = new ArrayList<ICompletionProposal>();
      Node grandParentNode = new DummyElement("style"); // FIXME const plz
      Element parentNode = new DummyElement("item");
      parentNode.setAttribute("name", info.name);
      parentNode.setTextContent(info.valuePrefix);

      grandParentNode.appendChild(parentNode);
      logger.info("Falling back to ValuesContentAssist({})", parentNode);
      logger.info("wordPrefix={}; valuePrefix={}", wordPrefix, info.valuePrefix);

      Node currentNode = null;
      ca2.computeTextValues(propsList, offset, parentNode, currentNode,
          null, wordPrefix);
      logger.info("Values={}", propsList.size());
      return propsList.toArray(new ICompletionProposal[propsList.size()]);
    }

    // release after, to ensure it exists
    model.releaseFromRead();

    return props;
  }

  static class PublicValuesContentAssist extends ValuesContentAssist {

    @Override
    public boolean computeAttributeValues(List<ICompletionProposal> proposals, int offset,
            String parentTagName, String attributeName, Node node, String wordPrefix,
            boolean skipEndTag, int replaceLength) {
      return super.computeAttributeValues(proposals, offset, parentTagName, attributeName, node,
                wordPrefix, skipEndTag, replaceLength);
    }

    @Override
    public void computeTextValues(List<ICompletionProposal> proposals, int offset,
            Node parentNode, Node currentNode, UiElementNode uiParent,
            String prefix) {
        super.computeTextValues(proposals, offset, parentNode, currentNode, uiParent,
                prefix);
    }
    
  }

  /** FIXME from AndroidContentAsset, for testing */
  private String extractElementPrefix(StructuredTextViewer viewer,
      int offset) {
    int i = offset;
    IDocument document = viewer.getDocument();
    if (i > document.getLength()) return ""; //$NON-NLS-1$
    try {
      for (; i > 0; --i) {
        char ch = document.getChar(i - 1);
        // We want all characters that can form a valid:
        // - element name, e.g. anything that is a valid Java class/variable literal.
        // - attribute name, including : for the namespace
        // - attribute value.
        // Before we were inclusive and that made the code fragile. So now we're
        // going to be exclusive: take everything till we get one of:
        // - any form of whitespace
        // - any xml separator, e.g. < > ' " and =
        if (Character.isWhitespace(ch) ||
            ch == '<' || ch == '>' || ch == '\'' || ch == '"' || ch == '=') {
          break;
            }
      }
      return document.get(i, offset - i);
    } catch (BadLocationException e) {
      return ""; //$NON-NLS-1$
    }
  }

  private AttribInfo parseAttributeInfo(ITextViewer viewer, int offset, int prefixStartOffset) {
    AttribInfo info = new AttribInfo();
    int originalOffset = offset;
    IDocument document = viewer.getDocument();
    int n = document.getLength();
    if (offset <= n) {
      try {
        // Look to the right to make sure we aren't sitting on the boundary of the
        // beginning of a new element with whitespace before it
        if (offset < n && document.getChar(offset) == '<') {
          return null;
        }
        n = offset;
        for (;offset > 0; --offset) {
          char ch = document.getChar(offset - 1);
          if (ch == '>') break;
          if (ch == '<') break;
        }
        // text will contain the full string of the current element,
        // i.e. whatever is after the "<" to the current cursor
        String text = document.get(offset, n - offset);
        // Normalize whitespace to single spaces
        text = sWhitespace.matcher(text).replaceAll(" "); //$NON-NLS-1$
        // Remove the leading element name. By spec, it must be after the < without
        // any whitespace. If there's nothing left, no attribute has been defined yet.
        // Be sure to keep any whitespace after the initial word if any, as it matters.
        text = sFirstElementWord.matcher(text).replaceFirst("");  //$NON-NLS-1$
        // There MUST be space after the element name. If not, the cursor is still
        // defining the element name.
        if (!text.startsWith(" ")) { //$NON-NLS-1$
          return null;
        }
        // Remove full attributes:
        // Syntax:
        //    name = "..." quoted string with all but < and "
        // or:
        //    name = '...' quoted string with all but < and '
        String temp;
        do {
          temp = text;
          text = sFirstAttribute.matcher(temp).replaceFirst("");  //$NON-NLS-1$
        } while(!temp.equals(text));
        IRegion lineInfo = document.getLineInformationOfOffset(originalOffset);
        int lineStart = lineInfo.getOffset();
        String line = document.get(lineStart, lineInfo.getLength());
        int cursorColumn = originalOffset - lineStart;
        int prefixLength = originalOffset - prefixStartOffset;
        // Now we're left with 3 cases:
        // - nothing: either there is no attribute definition or the cursor located after
        //   a completed attribute definition.
        // - a string with no =: the user is writing an attribute name. This case can be
        //   merged with the previous one.
        // - string with an = sign, optionally followed by a quote (' or "): the user is
        //   writing the value of the attribute.
        int posEqual = text.indexOf('=');
        if (posEqual == -1) {
          info.isInValue = false;
          info.name = text.trim();
          // info.name is currently just the prefix of the attribute name.
          // Look at the text buffer to find the complete name (since we need
          // to know its bounds in order to replace it when a different attribute
          // that matches this prefix is chosen)
          int nameStart = cursorColumn;
          for (int nameEnd = nameStart; nameEnd < line.length(); nameEnd++) {
            char c = line.charAt(nameEnd);
            if (!(Character.isLetter(c) || c == ':' || c == '_')) {
              String nameSuffix = line.substring(nameStart, nameEnd);
              info.name = text.trim() + nameSuffix;
              break;
            }
          }
          info.replaceLength = info.name.length() - prefixLength;
          if (info.name.length() == 0 && originalOffset > 0) {
            // Ensure that attribute names are properly separated
            char prevChar = extractChar(viewer, originalOffset - 1);
            if (prevChar == '"' || prevChar == '\'') {
              // Ensure that the attribute is properly separated from the
              // previous element
              info.needTag = ' ';
            }
          }
          info.skipEndTag = false;
        } else {
          info.isInValue = true;
          info.name = text.substring(0, posEqual).trim();
          info.valuePrefix = text.substring(posEqual + 1);
          char quoteChar = '"'; // Does " or ' surround the XML value?
          for (int i = posEqual + 1; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
              quoteChar = text.charAt(i);
              break;
            }
          }
          // Must compute the complete value
          int valueStart = cursorColumn;
          int valueEnd = valueStart;
          for (; valueEnd < line.length(); valueEnd++) {
            char c = line.charAt(valueEnd);
            if (c == quoteChar) {
              // Make sure this isn't the *opening* quote of the value,
              // which is the case if we invoke code completion with the
              // caret between the = and the opening quote; in that case
              // we consider it value completion, and offer items including
              // the quotes, but we shouldn't bail here thinking we have found
              // the end of the value.
              // Look backwards to make sure we find another " before
              // we find a =
              boolean isFirst = false;
              for (int j = valueEnd - 1; j >= 0; j--) {
                char pc = line.charAt(j);
                if (pc == '=') {
                  isFirst = true;
                  break;
                } else if (pc == quoteChar) {
                  valueStart = j;
                  break;
                }
              }
              if (!isFirst) {
                info.skipEndTag = true;
                break;
              }
            }
          }
          int valueEndOffset = valueEnd + lineStart;
          info.replaceLength = valueEndOffset - (prefixStartOffset + prefixLength);
          // Is the caret to the left of the value quote? If so, include it in
          // the replace length.
          int valueStartOffset = valueStart + lineStart;
          if (valueStartOffset == prefixStartOffset && valueEnd > valueStart) {
            info.replaceLength++;
          }
        }
        return info;
      } catch (BadLocationException e) {
        // pass
      }
    }
    return null;
  }

  /**
   * FIXME
   * Extracts the character at the given offset.
   * Returns 0 if the offset is invalid.
   */
  protected char extractChar(ITextViewer viewer, int offset) {
    IDocument document = viewer.getDocument();
    if (offset > document.getLength()) return 0;
    try {
      return document.getChar(offset);
    } catch (BadLocationException e) {
      return 0;
    }
  }

  /** Pick the right ContentAssist based on resource type */
  AndroidContentAssist getContentAssist(IProject project, String file) {
    // TODO
    return new LayoutContentAssist();
  }

  /** FIXME
   * Information about the current edit of an attribute as reported by parseAttributeInfo.
   */
  protected static class AttribInfo {
    public AttribInfo() {
    }
    /** True if the cursor is located in an attribute's value, false if in an attribute name */
    public boolean isInValue = false;
    /** The attribute name. Null when not set. */
    public String name = null;
    /** The attribute value top the left of the cursor. Null when not set. The value
     * *may* start with a quote (' or "), in which case we know we don't need to quote
     * the string for the user */
    public String valuePrefix = null;
    /** String typed by the user so far (i.e. right before requesting code completion),
     *  which will be corrected if we find a possible completion for an attribute value.
     *  See the long comment in getChoicesForAttribute(). */
    public String correctedPrefix = null;
    /** Non-zero if an attribute value need a start/end tag (i.e. quotes or brackets) */
    public char needTag = 0;
    /** Number of characters to replace after the prefix */
    public int replaceLength = 0;
    /** Should the cursor advance through the end tag when inserted? */
    public boolean skipEndTag = false;

    @Override
    public String toString() {
      return name + "; valpref=" + valuePrefix + "; corr=" + correctedPrefix
        + "; isInValue=" + isInValue;
    }
  }
}
