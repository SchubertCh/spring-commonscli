/*
 * Copyright 2023 wad'n dad'n
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.wadndadn.springcommonscli;

import lombok.SneakyThrows;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.PropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.core.env.CommandLinePropertySource.DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME;

class CommonsCliCommandLinePropertySourceTests {

    private static final String[] EMPTY_ARGUMENTS = {};

    private static final String OPTION_1 = "o1";
    private static final String OPTION_1_ARGUMENT = "v1";
    private static final String OPTION_2 = "o2";
    private static final String OPTION_3 = "o3";

    // --o1=v1 --o2
    private static final String[] OPTION_ARGUMENTS = {
            "--" + OPTION_1 + "=" + OPTION_1_ARGUMENT,
            "--" + OPTION_2};

    private static final String OPTION_2_ARGUMENT = "v2";
    private static final String NON_OPTION_ARGUMENT_1 = "/path/to/file1";
    private static final String NON_OPTION_ARGUMENT_2 = "/path/to/file2";

    // --o1=v1 --o2=v2 /path/to/file1 /path/to/file2
    private static final String[] NON_OPTION_ARGUMENTS = {
            "--" + OPTION_1 + "=" + OPTION_1_ARGUMENT,
            "--" + OPTION_2 + "=" + OPTION_2_ARGUMENT,
            NON_OPTION_ARGUMENT_1,
            NON_OPTION_ARGUMENT_2};

    private static final String NON_DEFAULT_PROPERTY_SOURCE_NAME = "nonDefaultPropertySourceName";

    private static final String NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME = "nonDefaultNonOptionArgsPropertyName";

    @Test
    void testDefaultName() {
        final CommandLine source = createCommandLineWithEmptyArgs();
        final PropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);

        assertEquals(CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME, propertySourceUnderTest.getName());
    }

    @SneakyThrows
    private static CommandLine createCommandLineWithEmptyArgs() {
        final Options options = new Options();
        final CommandLineParser commandLineParser = new DefaultParser();

        return commandLineParser.parse(options, EMPTY_ARGUMENTS);
    }

    @Test
    void testName() {
        final CommandLine source = createCommandLineWithEmptyArgs();
        final PropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(NON_DEFAULT_PROPERTY_SOURCE_NAME, source);

        assertNotEquals(CommonsCliCommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME, propertySourceUnderTest.getName());
        assertEquals(NON_DEFAULT_PROPERTY_SOURCE_NAME, propertySourceUnderTest.getName());
    }

    @Test
    void testSource() {
        final CommandLine source = createCommandLineWithEmptyArgs();
        final PropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);

        assertNotNull(propertySourceUnderTest.getSource());
        assertInstanceOf(CommandLine.class, propertySourceUnderTest.getSource());
    }

    /**
     * Working with option arguments description from {@link org.springframework.core.env.CommandLinePropertySource CommandLinePropertySource}:
     *
     * <p>For example, given the following command line:
     * <pre class="code">--o1=v1 --o2</pre>
     *
     * 'o1' and 'o2' are treated as "option arguments". As such, the following assertions will evaluate <pre class="code">true</pre>:
     * <pre class="code">
     * CommandLinePropertySource&lt;?&gt; ps = ...
     * assert ps.containsProperty("o1") == true;
     * assert ps.containsProperty("o2") == true;
     * assert ps.containsProperty("o3") == false;
     * assert ps.getProperty("o1").equals("v1");
     * assert ps.getProperty("o2").equals("");
     * assert ps.getProperty("o3") == null;
     * </pre>
     */
    @Test
    void testOptionArgumentsOnly() {
        final CommandLine source = createCommandLineWithOptionArgsOnly();
        final PropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);

        // assert ps.containsProperty("o1") == true;
        assertTrue(propertySourceUnderTest.containsProperty(OPTION_1));
        // assert ps.containsProperty("o2") == true;
        assertTrue(propertySourceUnderTest.containsProperty(OPTION_2));
        // assert ps.containsProperty("o3") == false;
        assertFalse(propertySourceUnderTest.containsProperty(OPTION_3));

        // assert ps.getProperty("o1").equals("v1");
        assertEquals(OPTION_1_ARGUMENT, propertySourceUnderTest.getProperty(OPTION_1));
        // assert ps.getProperty("o2").equals("");
        assertEquals("", propertySourceUnderTest.getProperty(OPTION_2));
        // assert ps.getProperty("o3") == null;
        assertNull(propertySourceUnderTest.getProperty(OPTION_3));

        assertFalse(propertySourceUnderTest.containsProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertNull(propertySourceUnderTest.getProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
    }

    @SneakyThrows
    private static CommandLine createCommandLineWithOptionArgsOnly() {
        final Options options = new Options();
        options.addOption("1", OPTION_1,true, OPTION_1 + " description");
        options.addOption("2", OPTION_2, false, OPTION_2 + " description");

        final CommandLineParser commandLineParser = new DefaultParser();

        return commandLineParser.parse(options, OPTION_ARGUMENTS);
    }

    /**
     * Working with non-option arguments description from {@link org.springframework.core.env.CommandLinePropertySource CommandLinePropertySource}:
     *
     * <p>For example, given the following command line:
     * <pre class="code">--o1=v1 --o2=v2 /path/to/file1 /path/to/file2</pre>
     *
     * 'o1' and 'o2' are treated as "option arguments", while the two filesystem paths qualify as
     * "non-option arguments". As such, the following assertions will evaluate <pre class="code">true</pre>:
     * <pre class="code">
     * CommandLinePropertySource&lt;?&gt; ps = ...
     * assert ps.containsProperty("o1") == true;
     * assert ps.containsProperty("o2") == true;
     * assert ps.containsProperty("nonOptionArgs") == true;
     * assert ps.getProperty("o1").equals("v1");
     * assert ps.getProperty("o2").equals("v2");
     * assert ps.getProperty("nonOptionArgs").equals("/path/to/file1,/path/to/file2");
     * </pre>
     */
    @Test
    void testNonOptionArguments() {
        final CommandLine source = createCommandLineWithNonOptionArgs();
        final PropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);

        // assert ps.containsProperty("o1") == true;
        assertTrue(propertySourceUnderTest.containsProperty(OPTION_1));
        // assert ps.containsProperty("o2") == true;
        assertTrue(propertySourceUnderTest.containsProperty(OPTION_2));
        // assert ps.containsProperty("nonOptionArgs") == true;
        assertTrue(propertySourceUnderTest.containsProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));

        // assert ps.getProperty("o1").equals("v1");
        assertEquals(OPTION_1_ARGUMENT, propertySourceUnderTest.getProperty(OPTION_1));
        // assert ps.getProperty("o2").equals("v2");
        assertEquals(OPTION_2_ARGUMENT, propertySourceUnderTest.getProperty(OPTION_2));
        // assert ps.getProperty("nonOptionArgs").equals("/path/to/file1,/path/to/file2");
        assertEquals(NON_OPTION_ARGUMENT_1 + "," + NON_OPTION_ARGUMENT_2,
                propertySourceUnderTest.getProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));

        assertFalse(propertySourceUnderTest.containsProperty(OPTION_3));
        assertNull(propertySourceUnderTest.getProperty(OPTION_3));
    }

    @SneakyThrows
    private static CommandLine createCommandLineWithNonOptionArgs() {
        final Options options = new Options();
        options.addOption("1", OPTION_1,true, OPTION_1 + " description");
        options.addOption("2", OPTION_2, true, OPTION_2 + " description");

        final CommandLineParser commandLineParser = new DefaultParser();

        return commandLineParser.parse(options, NON_OPTION_ARGUMENTS);
    }

    @Test
    void testDefaultNonOptionArgsPropertyNameWithOptionArgsOnly() {
        final CommandLine source = createCommandLineWithOptionArgsOnly();
        final CommandLinePropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);

        assertFalse(propertySourceUnderTest.containsProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertFalse(propertySourceUnderTest.containsProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));

        assertNull(propertySourceUnderTest.getProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertNull(propertySourceUnderTest.getProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
    }

    @Test
    void testNonDefaultNonOptionArgsPropertyNameWithOptionArgsOnly() {
        final CommandLine source = createCommandLineWithOptionArgsOnly();
        final CommandLinePropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);
        propertySourceUnderTest.setNonOptionArgsPropertyName(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME);

        assertFalse(propertySourceUnderTest.containsProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertFalse(propertySourceUnderTest.containsProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));

        assertNull(propertySourceUnderTest.getProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertNull(propertySourceUnderTest.getProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
    }

    @Test
    void testDefaultNonOptionArgsPropertyNameWithNonOptionArgs() {
        final CommandLine source = createCommandLineWithNonOptionArgs();
        final CommandLinePropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);

        assertTrue(propertySourceUnderTest.containsProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertFalse(propertySourceUnderTest.containsProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));

        assertEquals(NON_OPTION_ARGUMENT_1 + "," + NON_OPTION_ARGUMENT_2,
                propertySourceUnderTest.getProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertNull(propertySourceUnderTest.getProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
    }

    @Test
    void testNonDefaultNonOptionArgsPropertyNameWithNonOptionArgs() {
        final CommandLine source = createCommandLineWithNonOptionArgs();
        final CommandLinePropertySource<?> propertySourceUnderTest = new CommonsCliCommandLinePropertySource(source);
        propertySourceUnderTest.setNonOptionArgsPropertyName(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME);

        assertFalse(propertySourceUnderTest.containsProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertTrue(propertySourceUnderTest.containsProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));

        assertNull(propertySourceUnderTest.getProperty(DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
        assertEquals(NON_OPTION_ARGUMENT_1 + "," + NON_OPTION_ARGUMENT_2,
                propertySourceUnderTest.getProperty(NON_DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME));
    }
}
