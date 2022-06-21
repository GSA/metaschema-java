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

import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MockItemFactory {

  @NotNull
  private final Mockery context;

  public MockItemFactory(@NotNull Mockery ctx) {
    this.context = ctx;
  }

  protected Mockery getContext() {
    return context;
  }

  @SuppressWarnings("null")
  @NotNull
  protected <T extends INodeItem> T newMock(@NotNull Class<T> clazz, @NotNull String name) {
    String mockName = new StringBuilder()
        .append(clazz.getSimpleName())
        .append('-')
        .append(name)
        .append('-')
        .append(UUID.randomUUID().toString())
        .toString();
    return getContext().mock(clazz, mockName);
  }

  public IDocumentNodeItem document(@NotNull URI documentURI, @NotNull String name,
      @NotNull List<@NotNull IFlagNodeItem> flags,
      @NotNull List<@NotNull IModelNodeItem> modelItems) {
    IDocumentNodeItem document = newMock(IDocumentNodeItem.class, name);
    IRootAssemblyNodeItem root = newMock(IRootAssemblyNodeItem.class, name);

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(document).getRootAssemblyNodeItem();
        will(returnValue(root));
        allowing(document).getDocumentUri();
        will(returnValue(documentURI));
        allowing(document).getContextNodeItem();
        will(returnValue(document));
        allowing(document).getParentNodeItem();
        will(returnValue(null));

        allowing(root).getName();
        will(returnValue(name));
        allowing(root).getContextNodeItem();
        will(returnValue(root));
        allowing(root).getDocumentNodeItem();
        will(returnValue(document));
      }
    });

    handleChildren(document, CollectionUtil.emptyList(), CollectionUtil.singletonList(root));
    handleChildren(root, flags, modelItems);

    return document;
  }

  @SuppressWarnings("null")
  protected <T extends INodeItem> void handleChildren(
      @NotNull T item,
      @NotNull List<@NotNull IFlagNodeItem> flags,
      @NotNull List<@NotNull IModelNodeItem> modelItems) {
    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(item).getFlags();
        will(returnValue(flags));
        flags.forEach(flag -> {
          // handle each flag child
          allowing(item).getFlagByName(with(equal(flag.getName())));
          will(returnValue(flag));
          // link parent
          allowing(flag).getParentNodeItem();
          will(returnValue(item));
        });
        
        Map<@NotNull String, List<IModelNodeItem>> modelItemsMap = toModelItemsMap(modelItems);
        allowing(item).getModelItems();
        will(returnValue(modelItemsMap.values()));
        modelItemsMap.entrySet().forEach(entry -> {
          allowing(item).getModelItemsByName(with(equal(entry.getKey())));
          will(returnValue(entry.getValue()));

          AtomicInteger position = new AtomicInteger(1);
          entry.getValue().forEach(modelItem -> {
            // handle each model item child
            // link parent
            allowing(modelItem).getParentNodeItem();
            will(returnValue(item));

            // establish position
            allowing(modelItem).getPosition();
            will(returnValue(position.getAndIncrement()));
          });
        });

        allowing(item).children();
        will(new Action() {

          @Override
          public void describeTo(Description description) {
            description.appendText("returns stream");
          }

          @Override
          public Object invoke(Invocation invocation) throws Throwable {
            return modelItemsMap.values().stream()
                .flatMap(children -> children.stream());
          }
        });
      }
    });
  }

  @NotNull
  private Map<@NotNull String, List<@NotNull IModelNodeItem>>
      toModelItemsMap(@NotNull List<@NotNull IModelNodeItem> modelItems) {

    Map<@NotNull String, List<@NotNull IModelNodeItem>> retval = new LinkedHashMap<>();
    for (IModelNodeItem item : modelItems) {
      String name = item.getName();
      List<@NotNull IModelNodeItem> namedItems = retval.get(name);
      if (namedItems == null) {
        namedItems = new LinkedList<>();
        retval.put(name, namedItems);
      }
      namedItems.add(item);
    }
    return CollectionUtil.unmodifiableMap(retval);
  }

  @NotNull
  public IFlagNodeItem flag(@NotNull String name, @NotNull IAnyAtomicItem value) {
    IFlagNodeItem retval = newMock(IFlagNodeItem.class, name);

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getName();
        will(returnValue(name));

        allowing(retval).toAtomicItem();
        will(returnValue(value));

        allowing(retval).getContextNodeItem();
        will(returnValue(retval));
      }
    });

    handleChildren(retval, CollectionUtil.emptyList(), CollectionUtil.emptyList());

    return retval;
  }

  @NotNull
  public IFieldNodeItem field(@NotNull String name, @NotNull IAnyAtomicItem value) {
    return field(name, value, CollectionUtil.emptyList());
  }

  @NotNull
  public IFieldNodeItem field(@NotNull String name, @NotNull IAnyAtomicItem value,
      @NotNull List<@NotNull IFlagNodeItem> flags) {
    IFieldNodeItem retval = newMock(IFieldNodeItem.class, name);

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getName();
        will(returnValue(name));

        allowing(retval).toAtomicItem();
        will(returnValue(value));

        allowing(retval).getContextNodeItem();
        will(returnValue(retval));
      }
    });

    handleChildren(retval, flags, CollectionUtil.emptyList());
    return retval;
  }

  @NotNull
  public IAssemblyNodeItem assembly(@NotNull String name, @NotNull List<@NotNull IFlagNodeItem> flags,
      @NotNull List<@NotNull IModelNodeItem> modelItems) {
    IAssemblyNodeItem retval = newMock(IAssemblyNodeItem.class, name);

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getName();
        will(returnValue(name));

        allowing(retval).getContextNodeItem();
        will(returnValue(retval));
      }
    });

    handleChildren(retval, flags, modelItems);

    return retval;
  }

}