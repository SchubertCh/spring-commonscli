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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CommonsCliCommandLinePropertySource extends CommandLinePropertySource<CommandLine> {

    /**
     * Create a new {@code CommonsCliCommandLinePropertySource} having the default name and backed by the given {@code CommandLine}.
     *
     * @see CommandLinePropertySource#CommandLinePropertySource(Object)
     */
    public CommonsCliCommandLinePropertySource(final CommandLine commandLine) {
        super(commandLine);
    }

    /**
     * Create a new {@code CommonsCliCommandLinePropertySource} having the given name and backed by the given {@code CommandLine}.
     *
     * @see CommandLinePropertySource#CommandLinePropertySource(String, Object)
     */
    public CommonsCliCommandLinePropertySource(final String name, final CommandLine commandLine) {
        super(name, commandLine);
    }

    @Override
    protected boolean containsOption(final String name) {
        return this.source.hasOption(name);
    }

    @Override
    @Nullable
    public List<String> getOptionValues(final String name) {
        if (this.source.hasOption(name)) {
            if (log.isTraceEnabled()) {
                log.trace("An option with name '{}' is present", name);
            }
            // if the option is present ...
            final String[] sourceOptionValues = this.source.getOptionValues(name);

            if (sourceOptionValues == null) {
                if (log.isTraceEnabled()) {
                    log.trace("The option with name '{}' has no arguments", name);
                }
                // ... and has no argument (e.g.: "--foo"),
                // return an empty collection ([])
                return Collections.emptyList();
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("The option with name '{}' has arguments: '{}'", name, (Object) sourceOptionValues);
                }
                // ... and has a single value (e.g. "--foo=bar"),
                // return a collection having one element (["bar"])

                // ... and the underlying command line parsing library supports multiple arguments (e.g. "--foo=bar --foo=baz"),
                // return a collection having elements for each value (["bar", "baz"])
                return Arrays.asList(sourceOptionValues);
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("No option with name '{}' is present", name);
            }
            // if the option is not present,
            // return null
            return null;
        }
    }

    @Override
    protected List<String> getNonOptionArgs() {
        return this.source.getArgList();
    }

    @Override
    public String[] getPropertyNames() {
        if (log.isTraceEnabled()) {
            log.trace("Create an empty list of property names");
        }
        final List<String> propertyNameList = new ArrayList<>();

        final Option[] sourceOptions = this.source.getOptions();
        for (Option sourceOption : sourceOptions) {
            final String sourceOptionLongName = sourceOption.getLongOpt();
            if (StringUtils.hasText(sourceOptionLongName)) {
                if (log.isTraceEnabled()) {
                    log.trace("Add the long option name '{}' of option '{}' to the list of property names.", sourceOptionLongName, sourceOption);
                }
                propertyNameList.add(sourceOptionLongName);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Add the option name '{}' of option '{}' to the list of property names.", sourceOption.getOpt(), sourceOption);
                }
                propertyNameList.add(sourceOption.getOpt());
            }
        }

        final String[] propertyNames = StringUtils.toStringArray(propertyNameList);
        if (log.isTraceEnabled()) {
            log.trace("Return the property name as array '{}'", (Object) propertyNames);
        }
        return propertyNames;
    }
}
