/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.maven;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

// Maven and Plexus dependencies
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;


// Note: javadoc in class and fields descriptions must be XHTML.
/**
 * Copies <code>.jar</code> files in a single directory. Dependencies are copied as well,
 * except if already presents.
 * 
 * @goal collect
 * @phase package
 * 
 * @author Martin Desruisseaux
 */
public class JarCollector extends AbstractMojo {
    /**
     * The directory where JARs are collected.
     */
    private String collectDirectory;

    /**
     * Directory containing the generated JAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * Name of the generated JAR.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String jarName;

    /**
     * Project dependencies.
     *
     * @parameter expression="${project.artifacts}"
     * @required
     */
    private Set/*<Artifact>*/ dependencies;

    /**
     * The Maven project running this plugin.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Copies the {@code .jar} files to the collect directory.
     *
     * @throws MojoExecutionException if the plugin execution failed.
     */
    public void execute() throws MojoExecutionException {
        /*
         * Get the parent output directory.
         */
        MavenProject parent = project;
        while (parent.hasParent()) {
            parent = parent.getParent();
        }
        collectDirectory = parent.getBuild().getDirectory();
        /*
         * Now collects the JARs.
         */
        try {
            collect();
        } catch (IOException e) {
            throw new MojoExecutionException("Error collecting the JAR file.", e);
        }
    }

    /**
     * Implementation of the {@link #execute} method.
     */
    private void collect() throws MojoExecutionException, IOException {
        final File jarFile = new File(outputDirectory, jarName + ".jar");
        if (!jarFile.isFile()) {
            // Need to check for existing files, since pom packaging do not have any JAR.
            return;
        }
        final File collect = new File(collectDirectory);
        if (!collect.exists()) {
            if (!collect.mkdir()) {
                throw new MojoExecutionException("Failed to create target directory.");
            }
        }
        if (collect.getCanonicalFile().equals(jarFile.getParentFile().getCanonicalFile())) {
            /*
             * The parent's directory is the same one than this module's directory.
             * In other words, this plugin is not executed from the parent POM. Do
             * not copy anything, since this is not the place where we want to
             * collect the JAR files.
             */
            return;
        }
        int count = 1;
        FileUtils.copyFileToDirectory(jarFile, collect);
        if (dependencies != null) {
            for (final Iterator it=dependencies.iterator(); it.hasNext();) {
                final Artifact artifact = (Artifact) it.next();
                final String scope = artifact.getScope();
                if (scope.equalsIgnoreCase(Artifact.SCOPE_COMPILE) ||
                    scope.equalsIgnoreCase(Artifact.SCOPE_RUNTIME))
                {
                    final File file = artifact.getFile();
                    final File copy = new File(collect, file.getName());
                    if (!copy.exists()) {
                        /*
                         * Copies the dependency only if it was not already copied. Note that
                         * the module's JAR was copied inconditionnaly above (because it may
                         * be the result of a new compilation). If a Geotools JAR from the
                         * dependencies list changed, it will be copied inconditionnaly when
                         * the module for this JAR will be processed by Maven.
                         */
                        FileUtils.copyFileToDirectory(file, collect);
                        count++;
                    }
                }
            }
        }
        getLog().info("Copied "+count+" JAR to parent directory.");
    }
}
