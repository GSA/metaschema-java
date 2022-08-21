/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.model.common.datatype.markup;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IMarkupText {
  @NonNull
  Document getDocument();

  /**
   * Write HTML content to the provided {@code xmlStreamWriter} using the provided {@code namespace}.
   * 
   * @param writer
   *          the writer
   * @param namespace
   *          the XML namespace for the HTML
   * @throws XMLStreamException
   *           if an error occurred while writing
   */
  void writeHtml(@NonNull XMLStreamWriter2 writer, @NonNull String namespace) throws XMLStreamException;

  /**
   * Write HTML content to the provided {@code outputStream} using the provided {@code namespace}.
   * 
   * @param outputStream
   *          the stream to write to
   * @param namespace
   *          the XML namespace for the HTML
   * @param prefix
   *          the XML prefix for the HTML
   * @throws XMLStreamException
   *           if an error occurred while writing
   */
  void writeHtml(@NonNull OutputStream outputStream, @Nullable String namespace, @Nullable String prefix)
      throws XMLStreamException;

  @NonNull
  String toHtml();

  @NonNull
  String toMarkdown();

  String toMarkdown(Formatter formatter);

  @NonNull
  String toMarkdownYaml();

  /**
   * Retrieve all nodes contained within this markup text as a stream.
   * 
   * @return a depth first stream
   */
  @NonNull
  Stream<Node> getNodesAsStream();

  @NonNull
  default List<InsertAnchorNode> getInserts() {
    return getInserts(insert -> true);
  }

  /**
   * Retrieve all insert statements that are contained within this markup text that match the provided
   * filter.
   * 
   * @param filter
   *          a filter used to identify matching insert statements
   * @return the matching insert statements
   */
  @NonNull
  List<InsertAnchorNode> getInserts(
      @NonNull Predicate<InsertAnchorNode> filter);
}
