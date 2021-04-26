// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.testng.Reporter;

/**
 * This module defines an appender for TestNG.
 */

@Plugin(name = "TestNGReporter", category = "Core", elementType = "appender", printObject = true)
public class TestngAppender extends AbstractAppender {

  protected TestngAppender(String name, Layout<?> layout) {
    super(name, null, layout, false);
  }

  @PluginFactory
  public static TestngAppender createAppender(
      @PluginAttribute("name") @Required(message =
          "A name for the Appender must be specified") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout) {
    return new TestngAppender(name, layout);
  }

  @Override
  public void append(final LogEvent event) {
    final Layout<? extends Serializable> layout = getLayout();
    if (layout != null && layout instanceof AbstractStringLayout) {
      Reporter.log((String)(layout).toSerializable(event));
    } else {
      Reporter.log(event.getMessage().getFormattedMessage());
    }
  }
}
