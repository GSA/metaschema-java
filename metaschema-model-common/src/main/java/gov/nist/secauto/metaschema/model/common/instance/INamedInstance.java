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

package gov.nist.secauto.metaschema.model.common.instance;

import gov.nist.secauto.metaschema.model.common.INamedModelElement;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;

import org.jetbrains.annotations.NotNull;

/**
 * This marker interface indicates that the instance has a flag, field, or assembly name associated
 * with it which will be used in JSON/YAML or XML to identify the data.
 *
 */
public interface INamedInstance extends IInstance, INamedModelElement {
  /**
   * Retrieve the definition of this instance.
   * 
   * @return the corresponding definition
   */
  @NotNull
  INamedDefinition getDefinition();

  /**
   * Generates a "coordinate" string for the provided information element instance.
   * 
   * A coordinate consists of the element's:
   * <ul>
   * <li>containing Metaschema's short name</li>
   * <li>model type</li>
   * <li>name</li>
   * <li>hash code</li>
   * <li>the hash code of the definition</li>
   * </ul>
   * 
   * @return the coordinate
   */
  @SuppressWarnings("null")
  @Override
  default String toCoordinates() {
    IDefinition definition = getDefinition();

    IDefinition containingDefinition = getContainingDefinition();
    String retval;
    if (containingDefinition == null) {
      retval = String.format("%s:%s@%d(%d)", getModelType(), definition != null ? definition.getName() : "N/A",
          hashCode(), definition != null && definition.isGlobal() ? definition.hashCode() : 0);
    } else {
      retval = String.format("%s:%s:%s@%d(%d)", containingDefinition.getContainingMetaschema().getShortName(),
          getModelType(), definition != null ? definition.getName() : "N/A", hashCode(),
          definition != null && definition.isGlobal() ? definition.hashCode() : 0);
    }

    return retval;
  }
}