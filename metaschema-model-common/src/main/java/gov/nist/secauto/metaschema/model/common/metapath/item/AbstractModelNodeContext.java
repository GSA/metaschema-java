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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractModelNodeContext<F extends IFlagNodeItem, M extends IModelNodeItem, L extends AbstractModelNodeContext.Model<
    F, M>>
    extends AbstractNodeContext<F, L> {

  /**
   * Construct a new assembly node item.
   * 
   * @param factory
   *          the factory to use to instantiate new node items
   */
  protected AbstractModelNodeContext(@NotNull INodeItemFactory factory) {
    super(factory);
  }

  @Override
  protected abstract Supplier<L> newModelSupplier(@NotNull INodeItemFactory factory);

  @Override
  public Collection<@NotNull ? extends List<@NotNull ? extends M>> getModelItems() {
    return getModel().getModelItems();
  }

  @SuppressWarnings("null")
  @Override
  public List<@NotNull ? extends M> getModelItemsByName(String name) {
    return getModel().getModelItemsByName(name);
  }

  /**
   * Provides an abstract implementation of a lazy loaded model.
   * 
   * @param <F>
   *          the type of the child flag items
   * @param <M>
   *          the type of the child model items
   */
  protected static class Model<F extends IFlagNodeItem, M extends IModelNodeItem>
      extends Flags<F> {
    private final Map<@NotNull String, List<@NotNull M>> modelItems;

    /**
     * Creates a new collection of flags and model items.
     * 
     * @param flags
     *          a mapping of flag name to a flag item
     * @param modelItems
     *          a mapping of model item name to a list of model items
     */
    protected Model(
        @NotNull Map<@NotNull String, F> flags,
        @NotNull Map<@NotNull String, List<@NotNull M>> modelItems) {
      super(flags);
      this.modelItems = modelItems;
    }

    /**
     * Get the matching list of model items having the provided name.
     * 
     * @param name
     *          the name of the model items to retrieve
     * @return a lisy of matching model items or {@code null} if no match was found
     */
    @NotNull
    public List<@NotNull M> getModelItemsByName(@NotNull String name) {
      List<@NotNull M> result = modelItems.get(name);
      return result == null ? CollectionUtil.emptyList() : result;
    }

    /**
     * Get all model items grouped by model item name.
     * 
     * @return a collection of lists containg model items grouped by names
     */
    @SuppressWarnings("null")
    @NotNull
    public Collection<@NotNull List<@NotNull M>> getModelItems() {
      return modelItems.values();
    }

  }
}