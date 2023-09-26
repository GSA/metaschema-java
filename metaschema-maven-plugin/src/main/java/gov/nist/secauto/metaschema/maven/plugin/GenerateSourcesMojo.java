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

package gov.nist.secauto.metaschema.maven.plugin;

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.xml.ModuleLoader;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.JavaGenerator;
import gov.nist.secauto.metaschema.databind.codegen.config.DefaultBindingConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Goal which generates Java source files for a given set of Module definitions.
 */
@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateSourcesMojo
    extends AbstractMetaschemaMojo {
  private static final String STALE_FILE_NAME = "generateSourcesStaleFile";

  /**
   * A set of binding configurations.
   */
  @Parameter
  protected File[] configs;

  /**
   * <p>
   * Gets the last part of the stale filename.
   * </p>
   * <p>
   * The full stale filename will be generated by pre-pending
   * {@code "." + getExecution().getExecutionId()} to this staleFileName.
   *
   * @return the stale filename postfix
   */
  @Override
  protected String getStaleFileName() {
    return STALE_FILE_NAME;
  }

  /**
   * Retrieve a list of binding configurations.
   *
   * @return the collection of binding configurations
   */
  protected List<File> getConfigs() {
    List<File> retval;
    if (configs == null) {
      retval = Collections.emptyList();
    } else {
      retval = Arrays.asList(configs);
    }
    return retval;
  }

  /**
   * Generate the Java source files for the provided Metaschemas.
   *
   * @param modules
   *          the collection of Metaschema modules to generate sources for
   * @throws MojoExecutionException
   *           if an error occurred while generating sources
   */
  protected void generate(@NonNull Set<IModule> modules) throws MojoExecutionException {
    DefaultBindingConfiguration bindingConfiguration = new DefaultBindingConfiguration();
    for (File config : getConfigs()) {
      try {
        getLog().info("Loading binding configuration: " + config.getPath());
        bindingConfiguration.load(config);
      } catch (IOException ex) {
        throw new MojoExecutionException(
            String.format("Unable to load binding configuration from '%s'.", config.getPath()), ex);
      }
    }

    try {
      getLog().info("Generating Java classes in: " + getOutputDirectory().getPath());
      JavaGenerator.generate(modules, ObjectUtils.notNull(getOutputDirectory().toPath()),
          bindingConfiguration);
    } catch (IOException ex) {
      throw new MojoExecutionException("Creation of Java classes failed.", ex);
    }
  }

  @Override
  public void execute() throws MojoExecutionException {
    File staleFile = getStaleFile();
    try {
      staleFile = staleFile.getCanonicalFile();
    } catch (IOException ex) {
      getLog().warn("Unable to resolve canonical path to stale file. Treating it as not existing.", ex);
    }

    boolean generate;
    if (shouldExecutionBeSkipped()) {
      getLog().debug(String.format("Source file generation is configured to be skipped. Skipping."));
      generate = false;
    } else if (!staleFile.exists()) {
      getLog().info(String.format("Stale file '%s' doesn't exist! Generating source files.", staleFile.getPath()));
      generate = true;
    } else {
      generate = isGenerationRequired();
    }

    if (generate) {

      File outputDir = getOutputDirectory();
      getLog().debug(String.format("Using outputDirectory: %s", outputDir.getPath()));

      if (!outputDir.exists()) {
        if (!outputDir.mkdirs()) {
          throw new MojoExecutionException("Unable to create output directory: " + outputDir);
        }
      }

      // generate Java sources based on provided metaschema sources
      final ModuleLoader loader = new ModuleLoader();
      loader.allowEntityResolution();
      final Set<IModule> modules = new HashSet<>();
      for (File source : getSources().collect(Collectors.toList())) {
        getLog().info("Using metaschema source: " + source.getPath());
        IModule module;
        try {
          module = loader.load(source);
        } catch (MetaschemaException | IOException ex) {
          throw new MojoExecutionException("Loading of metaschema failed", ex);
        }
        modules.add(module);
      }

      generate(modules);

      // create the stale file
      if (!staleFileDirectory.exists()) {
        if (!staleFileDirectory.mkdirs()) {
          throw new MojoExecutionException("Unable to create output directory: " + staleFileDirectory);
        }
      }
      try (OutputStream os
          = Files.newOutputStream(staleFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
              StandardOpenOption.TRUNCATE_EXISTING)) {
        os.close();
        getLog().info("Created stale file: " + staleFile);
      } catch (IOException ex) {
        throw new MojoExecutionException("Failed to write stale file: " + staleFile.getPath(), ex);
      }

      // for m2e
      getBuildContext().refresh(getOutputDirectory());
    }

    // add generated sources to Maven
    try {
      getMavenProject().addCompileSourceRoot(getOutputDirectory().getCanonicalFile().getPath());
    } catch (IOException ex) {
      throw new MojoExecutionException("Unable to add output directory to maven sources.", ex);
    }
  }
}
